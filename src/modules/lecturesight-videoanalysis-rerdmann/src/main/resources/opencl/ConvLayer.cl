const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#ifdef TANH
    #define ACTIVATION_FUNCTION(output) (tanh(output))
#elif defined SCALEDTANH
    #define ACTIVATION_FUNCTION(output) (1.7159f * tanh(0.66667f * output))
#elif SIGMOID
    #define ACTIVATION_FUNCTION(output) (1.0f / (1 + exp(-output)))
#elif defined RELU
    #define ACTIVATION_FUNCTION(output) (output> 0 ? output : 0)
#elif defined ELU
    #define ACTIVATION_FUNCTION(output) (output> 0 ? output : exp(output) - 1)
#elif defined LINEAR
    #define ACTIVATION_FUNCTION(output) (output)
#endif

__kernel void compute_conv_layer(
    __read_only image3d_t input,
    __global float *weights,   
    __global float *bias,    
    __write_only image3d_t output,
    int channel)
{    
    //Kernel for every Pixel of Input-Image
    //3 Channel (RGB)
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
    float fi[9];
    float result = 0;
    //read weights
    
    /*
    0|1|2
    3|4|5
    6|7|8
    */   
    float in = 0;
    for(int c=0; c < channel; c++)
    {
        fi[4]=read_imagef(input, sampler, (float4)(inX,inY,filter,0)).x;
        //padding same (fill corners with zeros
        //left corner
        if(inX == 0)
        {
            fi[0] = 0;
            fi[3] = 0;
            fi[6] = 0;
        }
        else
        {
            fi[0]=read_imagef(input, sampler, (float4)(inX-1,inY-1,filter,0)).x;            
            fi[3]=read_imagef(input, sampler, (float4)(inX-1,inY,filter,0)).x;
            fi[6]=read_imagef(input, sampler, (float4)(inX-1,inY+1,filter,0)).x;
        }
        //right corner
        if(inX == inWidth)
        {
            fi[2] = 0;
            fi[5] = 0;
            fi[8] = 0;
        }
        else
        {
            fi[2]=read_imagef(input, sampler, (float4)(inX+1,inY-1,filter,0)).x;
            fi[5]=read_imagef(input, sampler, (float4)(inX+1,inY,filter,0)).x;
            fi[8]=read_imagef(input, sampler, (float4)(inX+1,inY+1,filter,0)).x; 
        }
        //top corner
        if(inY == 0)
        {
            fi[0] = 0;
            fi[1] = 0;
            fi[2] = 0;
        }
        else
        {
            fi[0]=read_imagef(input, sampler, (float4)(inX-1,inY-1,filter,0)).x;
            fi[1]=read_imagef(input, sampler, (float4)(inX,inY-1,filter,0)).x;
            fi[2]=read_imagef(input, sampler, (float4)(inX+1,inY-1,filter,0)).x;
        }
        //bottom corner
        if(inY == inHeight)
        {
            fi[6] = 0;
            fi[7] = 0;
            fi[8] = 0;
        } 
        else
        {            
            fi[6]=read_imagef(input, sampler, (float4)(inX-1,inY+1,filter,0)).x;
            fi[7]=read_imagef(input, sampler, (float4)(inX,inY+1,filter,0)).x;
            fi[8]=read_imagef(input, sampler, (float4)(inX+1,inY+1,filter,0)).x; 
        }  
        for(int i=0; i < kernel_size; i++) {
            //(f*channel*kernel_size)   : (current Filter * input channels * weights per kernel) => filters before
            //(c*kernel_size)           : (current channel * weights per kernel) => channel before
            //i                         : current weight
            result = result + (fi[i]*weights[1]);
        }
    }    
    result = bias[f] + result;  
    result = result > 0 ? result : 0; //relu activation   
    
    barrier( CLK_GLOBAL_MEM_FENCE );//synchro der Threads im Grid => wichtig wegen des ifs. Hier werden alle Threads gleichzeitig ausgeführt
    write_imagef(output,out_pos,(float4)(result,0,0,0));
}