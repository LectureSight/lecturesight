const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void test_processing
(
  __read_only  image2d_t input,
  __write_only image2d_t output
)
{ //global id ist die position im threadgrid
    int2 pos  = (int2)(get_global_id(0), get_global_id(1)); //global id = koordinate des Pixels
    uint4 out_pxl = read_imageui(input, sampler, pos); //auslesen Pixel (outpixel ist ein Vektor)

    if (pos.x % 8 == 0 || pos.y % 8 == 0 || pos.x % 8 == 7 || pos.y % 8 == 7 )
    {
            out_pxl = WHITE;//WHITE siehe Zeile 4
    }

    barrier( CLK_GLOBAL_MEM_FENCE );//synchro der Threads im Grid => wichtig wegen des ifs. Hier werden alle Threads gleichzeitig ausgeführt
    write_imageui(output, pos, out_pxl);
}