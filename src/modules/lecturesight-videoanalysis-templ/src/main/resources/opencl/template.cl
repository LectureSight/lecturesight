const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define TARGET_SIZE 32
#define SEARCH_SIZE 42

#define IGNORE (uint4)(0,0,0,0)
#define GREEN  (uint4)(0,255,0,255)

__kernel __attribute__ (( reqd_work_group_size (TARGET_SIZE, TARGET_SIZE, 1)))
void update_templates
(
    __read_only  image2d_t input,
    __read_only  image2d_t cells,
    __write_only image2d_t templates,
    __global     int       *toUpdate
)
{
    __local int  group_id;
    __local int  target_id;
    __local int2 target_read_pos; 
    __local int2 target_write_pos; 
        
    int2 lpos = (int2)(get_local_id(0), get_local_id(1));
    int  thread_idx  = TARGET_SIZE * lpos.y + lpos.x;

    // read target data
    if (thread_idx == 0) 
    {
      group_id = get_group_id(0); 
      int target_idx = group_id * 4;
      target_id = toUpdate[target_idx];
      target_read_pos = (int2)(toUpdate[target_idx+1], toUpdate[target_idx+2]);
      target_read_pos -= TARGET_SIZE / 2;
      target_write_pos = (int2)(0, target_id * TARGET_SIZE);
    }
    barrier(CLK_LOCAL_MEM_FENCE);
    
    // read pixels values from input
    int2 read_pos = target_read_pos + lpos;
    int2 cpos = read_pos / 8;
    uint4 pxl_cell = read_imageui(cells, sampler, cpos);
    
    // copy only pixels from active cells
    uint4 pxl_output = IGNORE;
//    if (pxl_cell.s0 > 0)
//    {
      pxl_output = read_imageui(input, sampler, read_pos);
//    }
    
    // write pixels to template buffer
    int2 write_pos = target_write_pos + lpos;  
    write_imageui(templates, write_pos, pxl_output);
}


__kernel __attribute__ (( reqd_work_group_size (TARGET_SIZE, TARGET_SIZE, 1)))
void match_templates
(
    __read_only image2d_t input,
    __read_only image2d_t templates,
    __global    int       *targets
)
{
    __local float4 template[32][32];
    __local float sum_buffer[1024];
    
    __local int  group_id;
    __local int  target_id;
    __local int2 target_pos;
    __local int2 search_pos;
    __local int2 tmpl_read_pos;
    __local float best_score;
    __local int2  best_pos;
    
    int2 lpos = (int2)(get_local_id(0), get_local_id(1));
    int  thread_idx = TARGET_SIZE * lpos.y + lpos.x;
    int  target_idx;
    
    // read target data
    if (thread_idx == 0) 
    {
      group_id = get_group_id(0); 
      target_idx = group_id * 4;
      target_id = targets[target_idx];
      target_pos = (int2)(targets[target_idx+1], targets[target_idx+2]);
      search_pos = (int2)(target_pos.x - SEARCH_SIZE/2, target_pos.y - SEARCH_SIZE/2);
      tmpl_read_pos = (int2)(0, target_id * TARGET_SIZE);
      best_score = FLT_MAX;
    }
    barrier(CLK_LOCAL_MEM_FENCE);
    
    // read template into local memory
    int2 pos = tmpl_read_pos + lpos;
    template[lpos.x][lpos.y] = convert_float4(read_imageui(templates, sampler, pos)) / 255;   
        
    // scan image
    // used (int2)runner before, but runner.y stayed 0 (?!?)
    for (int y=0; y < SEARCH_SIZE; y++) 
    {
      for (int x=0; x < SEARCH_SIZE; x++)
      {
        int2 rpos = search_pos + (int2)(x,y) + lpos;
        
        // diff^2
        if (template[lpos.x][lpos.y].s3 > 0.0) 
        {
          float4 pxl = convert_float4(read_imageui(input, sampler, rpos)) / 255;
          float4 diff = fabs(template[lpos.x][lpos.y] - pxl);
          sum_buffer[thread_idx] = diff.s0 + diff.s1 + diff.s2;
        }
        else
        {
          sum_buffer[thread_idx] = 0.0;
        }
        
        //barrier(CLK_LOCAL_MEM_FENCE);
        
        // reduce: sum 
        int num_threads = get_local_size(0) * get_local_size(1);
        for (int offset = num_threads / 2; offset > 1; offset >>= 1)
        {
          if (thread_idx < offset)
          {
            sum_buffer[thread_idx] += sum_buffer[thread_idx+offset];
          }
          barrier( CLK_LOCAL_MEM_FENCE );
        }
        
        if (thread_idx == 0)
        {
          if (sum_buffer[0] < best_score) 
          {
            best_score = sum_buffer[0];
            best_pos = (int2)(x,y);
          }
        }
        
        barrier(CLK_LOCAL_MEM_FENCE);
      }
    }
    
    if (thread_idx == 0) 
    {
      //toUpdate[idx]   = convert_int(best_score);            
      best_pos += search_pos + (int2)(TARGET_SIZE/2, TARGET_SIZE/2);
      targets[target_idx+1] = best_pos.x;
      targets[target_idx+2] = best_pos.y;
      targets[target_idx+3] = convert_int(best_score);
    }
}
