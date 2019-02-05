const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

__kernel void bilinear_interpolation(
    __read_only  image2d_t input,
    __write_only image2d_t output)


{
    int input_width = get_image_width(input);
    int input_height = get_image_height(input);
    int output_width = get_image_width(output);
    int output_height = get_image_height(output);
    //http://supercomputingblog.com/graphics/coding-bilinear-interpolation/
    int outX = get_global_id(0);
    int outY = get_global_id(1);
    int2 pos = {outX, outY};
    
    float scale_height = input_height/output_height;
    float scale_width = input_width/output_width;
    //Nearest neighbor
    //Schnellster + fügt keine neuen Farben ein
    
    float inX = floor(outX * scale_width);
    float inY = floor(outY * scale_height);
    
    uint4 q00 = read_imageui(input, sampler, (float2) (inX, inY));    
    uint4 q01 = read_imageui(input, sampler, (float2) (inX, inY+1));    
    uint4 q10 = read_imageui(input, sampler, (float2) (inX+1, inY));    
    uint4 q11 = read_imageui(input, sampler, (float2) (inX+1, inY+1));
    
    uint4 r1 = {0,0,0,0};
    uint4 r2 = {0,0,0,0};
    float x = (outX * scale_width)-inX;
    r1.x = q00.x * x +  q01.x * (1 - x);
    r2.x = q10.x * x +  q11.x * (1 - x);
    r1.y = q00.y * x +  q01.y * (1 - x);
    r2.y = q10.y * x +  q11.y * (1 - x);
    r1.z = q00.z * x +  q01.z * (1 - x);
    r2.z = q10.z * x +  q11.z * (1 - x);
    r1.w = q00.w * x +  q01.w * (1 - x);
    r2.w = q10.w * x +  q11.w * (1 - x);
    
    float y = (outY * scale_height)-inY;
    uint4 out_pxl = {0,0,0,0};
    out_pxl.x = r1.x * y + r2.x * (1 - y); 
    out_pxl.y = r1.y * y + r2.y * (1 - y); 
    out_pxl.z = r1.z * y + r2.z * (1 - y); 
    out_pxl.w = r1.w * y + r2.w * (1 - y); 
    
    write_imageui(output, pos, out_pxl);
}