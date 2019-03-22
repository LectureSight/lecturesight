const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP;

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void compute_test_kernel(    
    __read_only image2d_t input,  
    __global float *outputBuffer)
{    
    uint4 test_pxl = read_imageui(input, sampler, (int2)(0,0));         
    outputBuffer[0] = test_pxl.x;
    outputBuffer[1] = test_pxl.y;
    outputBuffer[2] = test_pxl.z;
    outputBuffer[3] = test_pxl.w;
}