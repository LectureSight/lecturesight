const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define BLACK (uint4)(0,0,0,255)

__kernel void bgdiff_3thresh
(
	read_only  image2d_t bgmodel,
	read_only  image2d_t input,
	write_only image2d_t output,
	int low, int mid, int high
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	
	uint4 pixel1 = read_imageui(bgmodel, sampler, pos);
	uint4 pixel2 = read_imageui(input, sampler, pos);
	
	uint4 diff = abs_diff(pixel1, pixel2);
	uint  sum = diff.s0 + diff.s1 + diff.s2;

	uint4 out_pxl = BLACK;
	if (sum > low)  out_pxl.s0 = 255;
	if (sum > mid)  out_pxl.s1 = 255;
	if (sum > high) out_pxl.s2 = 255;

	write_imageui(output, pos, out_pxl);	
}

__kernel void update_model
(
	read_only  image2d_t input,
	read_only  image2d_t model_in,
	read_only  image2d_t update_mask,
	write_only image2d_t model_out,
  write_only image2d_t model_last,
	           float     alpha
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));

	uint4 out_pxl;
	uint4 mask_pxl = read_imageui(update_mask, sampler, pos);
	
	if (mask_pxl.s0 == 0) 
	{
		float4 model_pxl = convert_float4(read_imageui(model_in, sampler, pos));
		float4 input_pxl = convert_float4(read_imageui(input, sampler, pos));
		out_pxl = convert_uint4( (model_pxl * (1 - alpha)) + (input_pxl * alpha) );
	}
	else
	{
		out_pxl = read_imageui(model_in, sampler, pos);
	}
	
	barrier( CLK_GLOBAL_MEM_FENCE );
	write_imageui(model_out, pos, out_pxl);
	write_imageui(model_last, pos, out_pxl);
}

