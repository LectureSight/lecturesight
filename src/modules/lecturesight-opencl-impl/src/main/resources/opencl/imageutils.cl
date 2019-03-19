const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

__kernel void copy_image
( 
	int src_x, int src_y,
	read_only  image2d_t src1,
	int dest_x, int dest_y,
	write_only image2d_t dest
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	int2 src_pos = (int2)(pos.x + src_x, pos.y + src_y);
	int2 dest_pos = (int2)(pos.x + dest_x, pos.y + dest_y);
	uint4 pixel = read_imageui(src1, sampler, src_pos);
	write_imageui(dest, dest_pos, pixel);	
}

__kernel void set_values4
(
	int x, int y,
	write_only image2d_t dest,
	uint r, uint g, uint b, uint a
)
{
	int2 pos = (int2)(get_global_id(0) + x, get_global_id(1) + y);
	uint4 pxl = (uint4)(r, g, b, a);
	write_imageui(dest, pos, pxl);
}

__kernel void set_values1
(
	int x, int y,
	write_only image2d_t dest,
	uint val
)
{
	int2 pos = (int2)(get_global_id(0) + x, get_global_id(1) + y);
	write_imageui(dest, pos, val);
}

__kernel void set_valuesInt
(
	int start,
	__global int* dest,
	uint val
)
{
	int pos = get_global_id(0) + start;
  dest[pos] = val;
}

