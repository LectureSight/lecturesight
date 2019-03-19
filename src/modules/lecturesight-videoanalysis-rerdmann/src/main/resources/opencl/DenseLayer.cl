__kernel void compute_dense(    
    __global float *input,
    __global float *weights,
    __global float *bias,
    __global float *output,
    int channel)
{
    int outX = get_global_id(0);
    float result = 0;
    for(int i=0; i < channel; i++)
    {
      result=result+input[i]*weights[(outX*channel)+i];
    }    
    result = result + bias[outX];
    output[outX] = result > 0 ? result : 0; //relu activation  
    barrier( CLK_GLOBAL_MEM_FENCE );//synchro der Threads im Grid => wichtig wegen des ifs. Hier werden alle Threads gleichzeitig ausgeführt
}