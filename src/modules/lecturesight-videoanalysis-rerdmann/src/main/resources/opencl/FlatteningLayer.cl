const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void compute_flatten(    
    __read_only image3d_t input,
    __global float *output)
{
    output[((get_global_size(0)*get_global_size(1)*get_global_id(2))+(get_global_size(0)*get_global_id(1)))+get_global_id(0)] = read_imagef(input, sampler, (float4)(get_global_id(0),get_global_id(1),get_global_id(2),0)).x;   
    barrier( CLK_GLOBAL_MEM_FENCE );//synchro der Threads im Grid => wichtig wegen des ifs. Hier werden alle Threads gleichzeitig ausgeführt
}