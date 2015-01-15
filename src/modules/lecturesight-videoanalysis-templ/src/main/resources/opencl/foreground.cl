#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics: enable
#pragma OPENCL EXTENSION cl_khr_global_int32_extended_atomics : enable

#define CELL_SIZE 16
#define CELL_SIZE_8 8

#define MAXINT 2147483647

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

#define CLAMP_POS(pos) if (pos.x > width || pos.y > height) return
#define ENCODE_INDEX(pos) pos.x + pos.y * (width+2)
#define DECODE_INDEX(idx) (int2)(idx % (width+2), idx / (width+2))

const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

__constant int4 bbox_init = (int4)(MAXINT,MAXINT,0,0);

__kernel void reset_bbox_buffer
(
	__global int* box_buffer
)
{
  int idx = get_global_id(0);
  vstore4(bbox_init, idx, box_buffer);
}

__kernel void compute_average
(
    __global int *centroids,
    __global int *weights
)
{
    int idx = get_global_id(0);
    int2 coords = vload2(idx, centroids);
    int weight = weights[idx];
    if (weights > 0) 
    {
      coords /= weight;
      vstore2(coords, idx, centroids);
    }
}

__kernel void avg_headpos
(
    __global int *headdata,
    __global int *headpos
)
{
    int idx  = get_global_id(0);
    int adr  = idx*3;
    int xsum = headdata[adr+1];
    int num  = headdata[adr+2];
    if (num < 1) num = 1;         // make sure we don't div by zero
    headpos[idx] = xsum/num;
}


__kernel __attribute__ (( reqd_work_group_size (8, 8, 1)))
void compute_object_stats_8
(
    __read_only image2d_t fg,
    __read_only image2d_t labels,
    __global    int       *weights,
    __global    int       *centroids,
    __global    int       *headpos,
    __global    int       *bboxes
)
{
    __local int box_num;
    __local int weight_buffer[256];
    __local int sum_x_buffer[256];
    __local int sum_y_buffer[256];
    __local int min_x;
    __local int min_y;
    __local int max_x;
    __local int max_y;
    
    int2 gpos = (int2)(get_global_id(0), get_global_id(1));
    int2 lpos = (int2)(get_local_id(0), get_local_id(1));
    int2 cpos = gpos / CELL_SIZE_8;
    int  thread_idx  = CELL_SIZE_8 * lpos.y + lpos.x;
    
    // zero working buffers
    weight_buffer[thread_idx] = 0;
    sum_x_buffer[thread_idx] = 0;
    sum_y_buffer[thread_idx] = 0;
    
    if (thread_idx == 0) 
    {
      // init min/max varables for bound box
      min_x = MAXINT;
      min_y = MAXINT;
      max_x = 0;
      max_y = 0;
    
      // get index of blob
      uint4 pxl_label = read_imageui(labels, sampler, cpos);
      box_num = pxl_label.s0;
    }
    barrier( CLK_LOCAL_MEM_FENCE );
    
    if (box_num > 0) 
    {
      uint4 pxl_fg = read_imageui(fg, sampler, gpos);
      if (pxl_fg.s0 != 0) 
      {
        weight_buffer[thread_idx] = 1;
        sum_x_buffer[thread_idx] = gpos.x;
        sum_y_buffer[thread_idx] = gpos.y;
        
        // bounding box
        if (gpos.x > 0) atom_min(&min_x, gpos.x);
        if (gpos.y > 0) atom_min(&min_y, gpos.y);
        atom_max(&max_x, gpos.x);
        atom_max(&max_y, gpos.y);
      }
      else 
      {
        weight_buffer[thread_idx] = 0;
        sum_x_buffer[thread_idx] = 0;
        sum_y_buffer[thread_idx] = 0;        
      }
      barrier( CLK_LOCAL_MEM_FENCE );
    
      // reduce
      int num_threads = get_local_size(0) * get_local_size(1);
      for (int offset = num_threads / 2; offset > 1; offset >>= 1)
      {
        if (thread_idx < offset)
        {
          weight_buffer[thread_idx] += weight_buffer[thread_idx+offset];
          sum_x_buffer[thread_idx] += sum_x_buffer[thread_idx+offset];
          sum_y_buffer[thread_idx] += sum_y_buffer[thread_idx+offset];
        }
        barrier( CLK_LOCAL_MEM_FENCE );
      }
    
      //write results to global memory
      if (thread_idx == 0) 
      {
        int obj_idx = box_num-1;
        
        // write partial sum of weight
        atom_add(weights + obj_idx, weight_buffer[0]);
        
        // write partial sum for centroid computation
        int adr = obj_idx * 2;
        atom_add(centroids + adr++, sum_x_buffer[0]);
        atom_add(centroids + adr,   sum_y_buffer[0]);
        
        // write data for head pos estimation
        adr = obj_idx * 3;
        if (cpos.y < headpos[adr])    // cells has less y than observed until now?
        {
          headpos[adr+1] = sum_x_buffer[0];  // reset data
          headpos[adr+2] = weight_buffer[0];
          headpos[adr] = cpos.y;             // save new min y value
        }
        else if (headpos[adr] == cpos.y)   // cell in same row as min y cells?
        {
          atom_add(headpos + adr + 1, sum_x_buffer[0]);
          atom_add(headpos + adr + 2, weight_buffer[0]);
        }
        // ignore any cell data that has y greater than min y
        
        // min/max for bounding box
        adr = obj_idx * 4;
        atom_min(bboxes + adr++, min_x);
        atom_min(bboxes + adr++, min_y);
        atom_max(bboxes + adr++, max_x);
        atom_max(bboxes + adr, max_y);
      }
    }
}
