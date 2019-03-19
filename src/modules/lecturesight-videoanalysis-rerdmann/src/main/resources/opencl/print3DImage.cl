const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void compute_print_3d_image(    
    __read_only image3d_t input,
    __global float *output)
{    
    float4 out_pxl=read_imagef(input, sampler, (float4)(get_global_id(0),get_global_id(1),61,0));
    output[get_global_id(1) * get_global_size(0) +get_global_id(0)]=out_pxl.x;
}