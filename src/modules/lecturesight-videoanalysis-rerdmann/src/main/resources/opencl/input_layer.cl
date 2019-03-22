const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP;

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void compute_input_layer(    
    __read_only image2d_t input,
    __write_only image3d_t output)
{
    uint4 in_pxl = read_imageui(input, sampler, (int2)(get_global_id(0),get_global_id(1)));
    
    write_imagef(output,(int4)(get_global_id(0),get_global_id(1),0,0),(float4)(in_pxl.x,0,0,0));
    write_imagef(output,(int4)(get_global_id(0),get_global_id(1),1,0),(float4)(in_pxl.y,0,0,0));
    write_imagef(output,(int4)(get_global_id(0),get_global_id(1),2,0),(float4)(in_pxl.z,0,0,0));
    
    barrier( CLK_GLOBAL_MEM_FENCE );//synchro der Threads im Grid => wichtig wegen des ifs. Hier werden alle Threads gleichzeitig ausgeführt    
}