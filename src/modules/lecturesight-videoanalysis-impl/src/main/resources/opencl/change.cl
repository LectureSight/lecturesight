const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

int read_val
(
	__read_only image2d_t input,
	int x, int y
)
{
	int2 pos = (int2)(x, y);
	uint4 pixel = read_imageui(input, sampler, pos);
	return pixel.s0;
}

__kernel void abs_diff_thresh
(
	__read_only  image2d_t current,
	__read_only  image2d_t last,
	__write_only image2d_t output,
	int threshold
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	uint4 current_pxl = read_imageui(current, sampler, pos);
	uint4 last_pxl = read_imageui(last, sampler, pos);
	uint4 diffp = abs_diff(current_pxl, last_pxl);
	uint diffs = (diffp.s0 + diffp.s1 + diffp.s2);
	uint4 out_pxl = 0;
	if (diffs >= threshold) 
	{
		out_pxl = 255;
	}
	barrier(CLK_LOCAL_MEM_FENCE);
	write_imageui(output, pos, out_pxl);
}

__kernel void image_erode4
(
	__read_only  image2d_t input,
	__write_only image2d_t output	
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	uint4 out_pxl = 255;
	
	int sum = read_val(input, pos.x, pos.y);
	
	sum += read_val(input, pos.x, pos.y-1);
	sum += read_val(input, pos.x-1, pos.y);
	sum += read_val(input, pos.x+1, pos.y);
	sum += read_val(input, pos.x, pos.y+1);

	if (sum < 1275)
	{
		out_pxl = 0;
	}
	barrier( CLK_GLOBAL_MEM_FENCE );
	write_imageui(output, pos, out_pxl);
}

__kernel void image_dilate8
(
	__read_only  image2d_t input,
	__write_only image2d_t output	
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	uint out_pxl = 0;
	
	int sum = read_val(input, pos.x, pos.y);

	sum += read_val(input, pos.x-1, pos.y-1);
	sum += read_val(input, pos.x,   pos.y-1);
	sum += read_val(input, pos.x+1, pos.y-1);

	sum += read_val(input, pos.x-1, pos.y);
	sum += read_val(input, pos.x+1, pos.y);

	sum += read_val(input, pos.x-1, pos.y+1);
	sum += read_val(input, pos.x,   pos.y+1);
	sum += read_val(input, pos.x+1, pos.y+1);

	if (sum > 0)
	{
		out_pxl = 255;
	}
	write_imageui(output, pos, out_pxl);
}


