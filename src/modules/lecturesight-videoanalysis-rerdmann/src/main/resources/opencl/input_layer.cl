const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void compute_input_layer(    
    __read_only image2d_t input,
    __global float *weights,   
    __global float *bias,    
    __write_only image3d_t output,
    __write_only image2d_t testoutput)
{
    //Kernel for every Pixel of Input-Image
    //3 Channel (RGB)
    int channel = 3;
    int kernel_size = 9;
    //weights 27 = 9x3 per filter (3X3 Kernel X 3 Channel)
    int inX = get_global_id(0); //Input_Image
    int inY = get_global_id(1); //Input_Image  
    int f = get_global_id(2);  //Filter count
    int4 out_pos = (int4)(inX, inY,f,0);
    
    int inWidth = get_global_size(0);
    int inHeight = get_global_size(1); 
    int2 in_pos = (int2)(inX, inY);
    int filter = get_global_id(2); //Filter
    uint4 ui[9];
    float result = 0;
    float w[21];
    //read weights
    for(int i=0; i < kernel_size*channel; i++) {
        w[i] = weights[(f*(kernel_size*channel))+i];
    }

    /*
    0|1|2
    3|4|5
    6|7|8
    */        
    
    ui[0]=read_imageui(input, sampler, (int2)(in_pos.x-1,in_pos.y-1));
    ui[1]=read_imageui(input, sampler, (int2)(in_pos.x,in_pos.y-1));
    ui[2]=read_imageui(input, sampler, (int2)(in_pos.x+1,in_pos.y-1));
    ui[3]=read_imageui(input, sampler, (int2)(in_pos.x-1,in_pos.y));
    ui[4]=read_imageui(input, sampler, (int2)(in_pos.x,in_pos.y));
    ui[5]=read_imageui(input, sampler, (int2)(in_pos.x+1,in_pos.y));
    ui[6]=read_imageui(input, sampler, (int2)(in_pos.x-1,in_pos.y+1));
    ui[7]=read_imageui(input, sampler, (int2)(in_pos.x,in_pos.y+1));
    ui[8]=read_imageui(input, sampler, (int2)(in_pos.x+1,in_pos.y+1));
    
    //padding same (fill corners with zeros
    //left corner
    if(inX == 0)
    {
        ui[0] = (uint4)(0,0,0,0);
        ui[3] = (uint4)(0,0,0,0);
        ui[6] = (uint4)(0,0,0,0);
    }
    //right corner
    if(inX == inWidth)
    {
        ui[2] = (uint4)(0,0,0,0);
        ui[5] = (uint4)(0,0,0,0);
        ui[8] = (uint4)(0,0,0,0);
    }
    //top corner
    if(inY == 0)
    {
        ui[0] = (uint4)(0,0,0,0);
        ui[1] = (uint4)(0,0,0,0);
        ui[2] = (uint4)(0,0,0,0);
    }
    //bottom corner
    if(inY == inHeight)
    {
        ui[6] = (uint4)(0,0,0,0);
        ui[7] = (uint4)(0,0,0,0);
        ui[8] = (uint4)(0,0,0,0);
    }
    
    for(int i=0; i < kernel_size; i++) {
        result = result +(ui[i].x * w[i] + ui[i].y * w[i+kernel_size] + ui[i].z * w[i+(kernel_size*2)]);
    }
    result = bias[f] + result;
    int test_result = result > 0 ? 255 : 0; //relu activation   
    result = result > 0 ? result : 0; //relu activation   
    
    if(get_global_id(2) == 1){
        write_imageui(testoutput, in_pos, (uint4)(test_result,test_result,test_result,255));  
    }
    
    barrier( CLK_GLOBAL_MEM_FENCE );//synchro der Threads im Grid => wichtig wegen des ifs. Hier werden alle Threads gleichzeitig ausgeführt
    write_imagef(output,out_pos,(float4)(result,0,0,0));
}