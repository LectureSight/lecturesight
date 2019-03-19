#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics: enable
#pragma OPENCL EXTENSION cl_khr_global_int32_extended_atomics : enable

#define CLAMP_POS(pos) if (pos.x > width || pos.y > height) return
#define ENCODE_INDEX(pos) pos.x + pos.y * (width+2)

__kernel void add_coordinates
(
	__global int*  labels,
	__global uint* coords,
	         int   width, int height
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
		int box_adr = (box_num-1) * 2;
		atom_add( coords + box_adr++, pos_img.x );
		atom_add( coords + box_adr, pos_img.y );
	}
}

__kernel void compute_means
(
	__global uint* coords,
	__global int* sizes
)
{
	int pos = get_global_id(0);
	uint2 sum = vload2(pos, coords);
  int size = sizes[pos];
  uint2 centroid = (uint2)(sum.x / size, sum.y / size);
	vstore2(centroid, pos, coords);
}
