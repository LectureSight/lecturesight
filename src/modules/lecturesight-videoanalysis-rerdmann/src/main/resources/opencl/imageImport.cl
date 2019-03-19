const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_NONE | CLK_FILTER_NEAREST;

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void image_import_processing
(
    __read_only  image2d_t input,
    __write_only image2d_t output
)
{
    int outX = get_global_id(0);
    int outY = get_global_id(1);
    int2 pos = {outX, outY};
    
    float4 out_pxl = read_imagef(input, sampler, pos);  
    uint4 pxl = (uint4)(out_pxl.z * 255, out_pxl.y * 255, out_pxl.x * 255, out_pxl.w * 255);
    write_imageui(output, pos, pxl);

}