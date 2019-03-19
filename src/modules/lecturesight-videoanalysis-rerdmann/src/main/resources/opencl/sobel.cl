//******************************************************************
//
// The operator uses two 3 x 3 kernels which are convolved with the
// original image to compute derivatives, one for horizontal changes
// & another for vertical.
//
// Gx, the horizontal derivative, is computed using the following
// 3 x 3 kernel:
//
//      [ -1    0   +1 ]
// Gx = [ -2    0   +2 ]
//      [ -1    0   +1 ]
//
// Gy, the vertical derivative, is computed using the following
// 3 x 3 kernel:
//
//      [ -1    -2  -1 ]
// Gy = [  0     0   0 ]
//      [ +1    +2  +1 ]
//
//
//******************************************************************
const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

__kernel void SobelDetector
(
    __read_only image2d_t input,
    __write_only image2d_t output
)
{
    int2 pos = (int2)(get_global_id(0), get_global_id(1));
    uint width = get_global_size(0);
    uint height = get_global_size(1);
    uint4 val = read_imageui(input, sampler, pos);

    if( pos.x >= 1 && pos.x < (width-1) && pos.y >= 1 && pos.y < (height - 1)){
            uint4 i00=read_imageui(input, sampler, (int2)(pos.x-1,pos.y-1));
            uint4 i10=read_imageui(input, sampler, (int2)(pos.x,pos.y-1));
            uint4 i20=read_imageui(input, sampler, (int2)(pos.x+1,pos.y-1));
            uint4 i01=read_imageui(input, sampler, (int2)(pos.x-1,pos.y));
            //uint4 i11=read_imageui(input, sampler, (int2)(pos.x,pos.y));
            uint4 i21=read_imageui(input, sampler, (int2)(pos.x+1,pos.y));
            uint4 i02=read_imageui(input, sampler, (int2)(pos.x-1,pos.y+1));
            uint4 i12=read_imageui(input, sampler, (int2)(pos.x,pos.y+1));
            uint4 i22=read_imageui(input, sampler, (int2)(pos.x+1,pos.y+1));

            int x1=-i00.x-2*i10.x-i20.x+i02.x+2*i12.x+i22.x;
            int y1=-i00.y-2*i10.y-i20.y+i02.y+2*i12.y+i22.y;
            int z1=-i00.z-2*i10.z-i20.z+i02.z+2*i12.z+i22.z;

            int x2=-i00.x-2*i01.x-i02.x+i20.x+2*i21.x+i22.x;
            int y2=-i00.y-2*i01.y-i02.y+i20.y+2*i21.y+i22.y;
            int z2=-i00.z-2*i01.z-i02.z+i20.z+2*i21.z+i22.z;

            val.x=sqrt((float)(x1*x1+x2*x2));
            val.y=sqrt((float)(y1*y1+y2*y2));
            val.z=sqrt((float)(z1*z1+z2*z2));
    }
    write_imageui(output, pos, val);	
}