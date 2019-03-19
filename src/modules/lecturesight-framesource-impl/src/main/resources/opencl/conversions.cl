/* conversions.cl
 * 
 * CL program providing kernels to convert raw images of different types into CLImage2D data.
 *
 */
#define BLACK (uint4)(0,0,0,255)
const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

__kernel void RGB24_RGBAUint8
(
	int width,
	int height,
	__global uchar* src,
	write_only image2d_t dest
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	ulong src_idx = (pos.y * width * 3) + (pos.x * 3);
	
	uchar red = src[src_idx];		// get pixel data
	uchar green = src[src_idx+1];
	uchar blue = src[src_idx+2];

	uint4 pixel = (uint4)(			// compose BGRA pixel
		(uint)blue,
		(uint)green,
		(uint)red,
		255
	);

	write_imageui(dest, pos, pixel);	// write pixel
}

__kernel void RGB24_RGBAUint8_Inverted
(
	int width,
	int height,
	__global uchar* src,
	write_only image2d_t dest
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	int2 newpos = (int2)(width - 1 - pos.x, height - 1 - pos.y);
	ulong src_idx = (pos.y * width * 3) + (pos.x * 3);

	uchar red = src[src_idx];		// get pixel data
	uchar green = src[src_idx+1];
	uchar blue = src[src_idx+2];

	uint4 pixel = (uint4)(			// compose BGRA pixel
		(uint)blue,
		(uint)green,
		(uint)red,
		255
	);

	write_imageui(dest, newpos, pixel);	// write pixel
}

__kernel void Intensity8_RGBAUint8
(
	int width,
	int height,
	__global uchar* src,
	write_only image2d_t dest
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	ulong src_idx = pos.y * width + pos.x;
	
	uchar val = src[src_idx];		// get pixel data
	
	uint4 pixel = (uint4)(			// compose BGRA pixel
		(uint)val,
		(uint)val,
		(uint)val,
		255
	);

	write_imageui(dest, pos, pixel);	// write pixel
}

__kernel void apply_mask
(
    read_only  image2d_t src,
    read_only  image2d_t mask,
    write_only image2d_t dest
)
{
    int2 pos = (int2)(get_global_id(0), get_global_id(1));
    uint4 src_pxl = read_imageui(src, sampler, pos);
    uint4 mask_pxl = read_imageui(mask, sampler, pos);
    uint4 out_pxl;
    if (mask_pxl.s0 != 0) 
    {
        out_pxl = src_pxl;
    }
    else
    {
        out_pxl = BLACK;
    }
    barrier( CLK_GLOBAL_MEM_FENCE );
    write_imageui(dest, pos, out_pxl);
}
