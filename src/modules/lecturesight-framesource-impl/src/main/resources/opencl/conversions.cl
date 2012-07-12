/* conversions.cl
 * 
 * CL program providing kernels to convert raw images of different types into CLImage2D data.
 *
 */

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
		(uint)red,
		(uint)green,
		(uint)blue,
		255
	);

	write_imageui(dest, pos, pixel);	// write pixel
}


