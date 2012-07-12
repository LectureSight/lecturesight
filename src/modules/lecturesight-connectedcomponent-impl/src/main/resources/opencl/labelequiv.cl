const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics: enable
#pragma OPENCL EXTENSION cl_khr_global_int32_extended_atomics : enable

#define CLAMP_POS(pos) if (pos.x > width || pos.y > height) return
#define ENCODE_INDEX(pos) pos.x + pos.y * (width+2)
#define DECODE_INDEX(idx) (int2)(idx % (width+2), idx / (width+2))

#define MAXINT 2147483647

__kernel void assign_indices
(
	__read_only image2d_t input,                                // input image
	__global    int*      work,                                 // label array
	__global    int*      sizes,                                // size array
	            int       width,                                // image width
              int       height                                // image height
)
{
	int2 pos = (int2)( get_global_id(0),                        // get thread grid position
                     get_global_id(1) );                      // = position in label buffer
  int2 pos_img = (int2)( pos.x - 1, pos.y - 1 );              // position in input image
	if (pos.x >= width+2 || pos.y >= height+2) return;          // clamp pos

	int val = 0;                                                // default values
	int size = 0;
  int idx = pos.x + (pos.y * (width+2));

	if ( pos.x > 0 && pos.x <= width &&                         // element inside working area?
       pos.y > 0 && pos.y <= height ) 
	{   
		uint4 pxl = read_imageui(input, sampler, pos_img);        // read pixel from input
		if (pxl.x != 0)                                           // non-zero pixel?
    {
	    val = idx;                                              // compute element 1D index
			size = 1;                                               // pixel has size
		}
	}

	barrier( CLK_GLOBAL_MEM_FENCE );                            // force threads to join
	work[idx] = val;                                            // store label
	sizes[idx] = size;                                          // store size
}


__kernel void min_neighbour
(
	__global int* work,                                         // label array
	__global int* changed,                                      // change buffer
	         int  width,                                        // image width
           int  height
)
{
	int2 pos = (int2)( get_global_id(0)+1,                      // get thread group position
                     get_global_id(1)+1 );                    // +1 : ommit padding
	CLAMP_POS(pos);                                             // clamp position

	int adr = ENCODE_INDEX(pos);	                              // compute 1D index
 	int idx = work[adr];                                        // read label of element
	if (idx != 0)                                               // element corresponds to non-zero pixel?
	{	
		int min_idx = MAXINT;                                     // init min_idx with CL_INT_MAX
  
		int idx_w = work[adr-1];	                                // read west element
		if (idx_w > 0) min_idx = idx_w;                           // non-zero? -> automatically smaller

		int idx_e = work[adr+1];                                  // read east element 
		if (idx_e > 0 && idx_e < min_idx) min_idx = idx_e;        // store if new minimum

		int idx_n = work[adr-width-2];                            // read north element
		if (idx_n > 0 && idx_n < min_idx) min_idx = idx_n;        // store if new minimum

		int idx_s = work[adr+width+2];                            // read south element
		if (idx_s > 0 && idx_s < min_idx) min_idx = idx_s;        // store if new minimum

		if (min_idx < idx)                                        // smaller neibhor found?
		{
			int idx_lst = work[idx];                                // update element label
			work[idx] = min(idx_lst, min_idx);
			changed[0] = 1;                                         // indicate element update
		}
	}
}


__kernel void update_indices
(
	__global int* work,
	__global int* sizes,
	         int  width, 
           int  height
)
{
	int2 pos = (int2)( get_global_id(0)+1,                      // get thread group position
                     get_global_id(1)+1 );
	CLAMP_POS(pos);                                             // clamp pos
	
	int adr = ENCODE_INDEX(pos);                                // compute 1D index
	int idx = work[adr];                                        // read element label
	if (idx != 0)                                               // element corresponds to non-zero pixel?
	{
		int idx_tmp = work[idx];                                  // read label of referenced element
		while (idx != idx_tmp)                                    // while referenced element not a root element
		{
			idx     = work[idx_tmp];                                // run through references chain
			idx_tmp = work[idx]; 
		}                                                         // TODO force threads to join here?!
		work[adr] = idx;                                          // store root label for element

		uint size = sizes[adr];                                   // read element size
		if (size && idx != adr) {                                 // non-zero size && element is not a root?
			atom_add(sizes + idx, size);                            // add size to root element
			sizes[adr] = 0;                                         // set to zero
		}
	}	
}


__kernel void make_blob_ids
(
	__global int* work,                                         // label array
	__global int* ids,                                          // label output array
	__global int* sizes,                                        // size array
	__global int* sizes_out,                                    // size output array
	         int  width,                                        // image width
           int  height,                                       // image height
	         int  min_size,                                     // minimum blob size
           int  max_size,                                     // maximum blob size
	         int  max_boxes                                     // limit of output arrays
)
{
	int2 pos = (int2)( get_global_id(0)+1,                      // get thread group position
                     get_global_id(1)+1 );                    
	CLAMP_POS(pos);                                             // clamp position
	
	int adr = ENCODE_INDEX(pos);                                // compute 1D index
	int idx = work[adr];                                        // get element label
	if (idx == adr)                                             // element is root element?
	{
		int size = sizes[adr];                                    // get size of blob
		if (min_size < size && size < max_size)                   // blob has right size?
		{                                                         // TODO use shared memory for counter
      int num = atom_inc(ids)+1;                              // get next ID         
			if (num < max_boxes)                                    // still fits into output arrays?
      {
				ids[num] = adr;                                       // store root label in output array 
				work[adr] = -1 * num;                                 // set root value to negative blob ID
				sizes_out[num-1] = size;                              // store blob size in output array
			}
		}
	}
}

__kernel void update_blob_ids
(
	__global int* labels,
	         int  width,
		       int  height
)	
{
	int2 pos = (int2)( get_global_id(0)+1,            // get thread group position
                     get_global_id(1)+1 );
	CLAMP_POS(pos);                                   // clamp position

	int adr = ENCODE_INDEX(pos);                      // compute 1D index
	int idx = labels[adr];                            // read elements value
	int num = 1;                                      // init num positive

	if (idx > 0)                                      // element references?
	{
		num = labels[idx];                              // read referenced elements value
	} 
	else if (idx < 0)	                                // ref element root of valid blob?
	{
		num = idx;                                      // element is root of valid blob
	}

	if (num < 0)                                      // negative value -> root of valid blob found
	{
    labels[adr] = num;                              // assign element with blob ID
	}
}


__kernel void reset_change
(
	__global int* change
)
{
	change[0] = 0;
}

// debug function: generate color image, mark root elements light blue
__kernel void work2rgba
(
	__global     int*      work,
	__write_only image2d_t output,
	int width, int height
)
{
	int2 pos = (int2)( get_global_id(0)+1, get_global_id(1)+1 );
	CLAMP_POS(pos); 

	int adr = ENCODE_INDEX(pos);
	int idx = work[adr];
	uint4 out_pxl;
	if (idx != 0)
	{
		int2 idx_pos = DECODE_INDEX(idx);
		if (idx < 0) 
		{
			out_pxl = (uint4)(0,20,255,255);
		} 
		else
		{
			out_pxl = (uint4)(((float)idx_pos.x/width)*255, 
                        ((float)idx_pos.y/height)*255, 
                        0, 255);
		}
	}
	else
	{
		out_pxl = (uint4)(0,0,0,255);
	}
  pos = (int2)(pos.x-1, pos.y-1);
	barrier( CLK_GLOBAL_MEM_FENCE );
	write_imageui(output, pos, out_pxl);
}
