const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define CELL_SIZE 16
#define CELL_SIZE_8 8

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void abs_diff_thresh
(
	__read_only  image2d_t current,
	__read_only  image2d_t last,
	__write_only image2d_t output,
               int       threshold
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	
  uint4 pxl_current = read_imageui(current, sampler, pos);
	uint4 pxl_last = read_imageui(last, sampler, pos);
	
  uint4 pxl_diff = abs_diff(pxl_current, pxl_last);
	uint diffsum = (pxl_diff.s0 + pxl_diff.s1 + pxl_diff.s2);

  uint4 pxl_out = BLACK;
  if (diffsum > threshold) pxl_out = WHITE;
	
	barrier(CLK_GLOBAL_MEM_FENCE);
	write_imageui(output, pos, pxl_out);
}
