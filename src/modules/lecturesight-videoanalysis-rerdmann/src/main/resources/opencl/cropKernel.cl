const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

__kernel void crop(
    __read_only  image2d_t input,
    __write_only image2d_t output)


{
    int input_width = get_image_width(input);
    int input_height = get_image_height(input);
    int output_width = get_image_width(output);
    int output_height = get_image_height(output);
    int outX = get_global_id(0);
    int outY = get_global_id(1);
    int2 pos = {outX, outY};
    
    
    //Crop horizontal middle of image
    float inX = outX;
    //float inX = ((input_width-output_width)/2)+outX;
    float inY = outY;
    //float inY = ((input_height-output_height)/2)+outY;
    
    
    uint4 out_pxl = read_imageui(input, sampler, (float2) (inX, inY));    
    write_imageui(output, pos, out_pxl);
}