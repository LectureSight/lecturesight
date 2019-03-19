const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void compute_max_pooling(    
    __read_only image3d_t input,
    __write_only image3d_t output,
    int pool,
    int stride)
{
    int outX = get_global_id(0); //Width
    int outY = get_global_id(1); //Height  
    int outZ = get_global_id(2); //Depth
    int4 out_pos = (int4)(outX, outY,outZ,0);
    float max = 0;
    float in = 0;
    for(int x=0; x < pool; x++) {
        for(int y=0; y < pool; y++) {
            in = read_imagef(input, sampler, (float4)((outX*stride)+x,(outY*stride)+y,outZ,0)).x;
            max = in > max ? in : max;
        }        
    }
    
    barrier( CLK_GLOBAL_MEM_FENCE );//synchro der Threads im Grid => wichtig wegen des ifs. Hier werden alle Threads gleichzeitig ausgeführt
    write_imagef(output,out_pos,(float4)(max,0,0,0));
}