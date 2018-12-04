const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void test_processing
(
  __read_only  image2d_t input,
  __write_only image2d_t output
)
{
  int2 pos  = (int2)(get_global_id(0), get_global_id(1));
	uint4 out_pxl = read_imageui(input, sampler, pos);

	if (pos.x % 8 == 0 && pos.y % 8 == 0)
	{
		out_pxl = WHITE;
	}

	barrier( CLK_GLOBAL_MEM_FENCE );
	write_imageui(output, pos, out_pxl);
}
