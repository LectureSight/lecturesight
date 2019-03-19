const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

__kernel void nearest_neighbor(
    __read_only  image2d_t input,
    __write_only image2d_t output)
{
    int input_width = get_image_width(input);
    int input_height = get_image_height(input);
    int output_width = get_image_width(output);
    int output_height = get_image_height(output);
    //https://clouard.users.greyc.fr/Pantheon/experiments/rescaling/index-en.html#nearest
    int outX = get_global_id(0);
    int outY = get_global_id(1);
    int2 pos = {outX, outY};
    float scale_width = input_width/output_width;
    float scale_height = input_height/output_height;
    //Nearest neighbor
    //Schnellster + fügt keine neuen Farben ein
    
    float inX = floor(outX * scale_width);
    float inY = floor(outY * scale_height);
    float2 posIn = (float2) (inX, inY);
    
    uint4 out_pxl = read_imageui(input, sampler, posIn);
    write_imageui(output, pos, out_pxl);
}
