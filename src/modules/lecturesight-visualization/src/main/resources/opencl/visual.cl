const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define ENCODE_INDEX(pos) pos.x + pos.y * (width+2)
#define CLAMP_POS(pos) if (pos.x > width || pos.y > height) return
#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void copy_red_tint
(
  __read_only  image2d_t input,
  __read_only  image2d_t fgmap,
  __global     int*      labels,
  __write_only image2d_t visual,
               int       width
)
{
  int2 pos = (int2)(get_global_id(0), get_global_id(1));
  int  adr = ENCODE_INDEX((int2)(pos.x+1, pos.y+1)); 

  uint4 input_pxl = read_imageui(input, sampler, pos);
  uint4 fgmap_pxl = read_imageui(fgmap, sampler, pos);
  uint4 out_pxl = (uint4)(input_pxl.s0, input_pxl.s1, 255, 255);
  
  if (fgmap_pxl.s0 != 0) 
  {
    out_pxl.s0 = input_pxl.s0;
  }

  barrier( CLK_GLOBAL_MEM_FENCE );
  
  write_imageui(visual, pos, out_pxl);
}
