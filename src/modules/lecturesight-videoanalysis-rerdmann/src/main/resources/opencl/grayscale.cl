const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

__kernel void grayscale_processing
(
  __read_only  image2d_t input,
  __write_only image2d_t output
)
{ //global id ist die position im threadgrid
    int2 pos  = (int2)(get_global_id(0), get_global_id(1)); //global id = koordinate des Pixels
    uint4 out_pxl = read_imageui(input, sampler, pos); //auslesen Pixel (outpixel ist ein Vektor)
    
    // convert image to grayscale
    // lightness: max(R, G, B) + min(R, G, B)) / 2
    uint gray_pixel = (max(max(out_pxl.x, out_pxl.y), out_pxl.z) + min(min(out_pxl.x, out_pxl.y), out_pxl.z))/2;
    // average: (R + G + B) / 3
    //uint gray_pixel = (out_pxl.x + out_pxl.y + out_pxl.z)/3;
    write_imageui(output, pos, (uint4)(gray_pixel,gray_pixel,gray_pixel,out_pxl.w));
    
   /* if (pos.x < 64 && pos.y < 128)
    {
        // convert image to grayscale
        // lightness: max(R, G, B) + min(R, G, B)) / 2
        //##############uint gray_pixel = (max(max(out_pxl.x, out_pxl.y), out_pxl.z) + min(min(out_pxl.x, out_pxl.y), out_pxl.z))/2;
        // average: (R + G + B) / 3
        //uint gray_pixel = (out_pxl.x + out_pxl.y + out_pxl.z)/3;
        write_imageui(output, pos, out_pxl);//(uint4)(gray_pixel,gray_pixel,gray_pixel,out_pxl.w));
    }*/
}