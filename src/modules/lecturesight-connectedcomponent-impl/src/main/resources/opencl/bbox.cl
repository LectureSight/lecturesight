#pragma OPENCL EXTENSION cl_khr_global_int32_extended_atomics: enable
#define CLAMP_POS(pos) if (pos.x > width || pos.y > height) return
#define ENCODE_INDEX(pos) pos.x + pos.y * (width+2)
#define MAXINT 2147483647

__kernel void reset_bbox_buffer
(
	__global int* box_buffer
)
{
	int idx = get_global_id(0)*4;                     // TODO use vstore4 to increase performance
	box_buffer[idx++] = 0;                            // init 
	box_buffer[idx++] = 0;                            
	box_buffer[idx++] = MAXINT;
	box_buffer[idx] = MAXINT;
}

__kernel void compute_bboxes
(
	__global int* labels,
	__global int* boxes,
	         int  width,
		       int  height
)	
{
	int2 pos = (int2)( get_global_id(0)+1,            // get thread group position
                     get_global_id(1)+1 );
  int2 pos_img = (int2)( pos.x-1, pos.y-1 );        // position in image
	CLAMP_POS(pos);                                   // clamp position

	int adr = ENCODE_INDEX(pos);                      // compute 1D index
	int box_num = -1 * labels[adr];                   // read elements value
	if (0 < box_num)                                  // positive value -> root of valid blob found
	{
    int box_adr = (box_num-1) * 4;                  // compute address in output array
		atom_max( boxes + box_adr++, pos_img.x );       // make max coordiantes
		atom_max( boxes + box_adr++, pos_img.y );	
		atom_min( boxes + box_adr++, pos_img.x );       // make min coordinates
		atom_min( boxes + box_adr, pos_img.y );
	}
}
