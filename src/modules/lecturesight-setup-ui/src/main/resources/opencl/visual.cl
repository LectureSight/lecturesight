const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

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

  uint4 input_pxl = read_imageui(input, sampler, pos);
  uint4 fgmap_pxl = read_imageui(fgmap, sampler, pos);
  uint4 out_pxl = (uint4)(255, input_pxl.s1, input_pxl.s2, 255);  
  
  if (fgmap_pxl.s0 != 0) 
  {
    out_pxl.s0 = input_pxl.s0;
  }

  barrier( CLK_GLOBAL_MEM_FENCE );
  
  write_imageui(visual, pos, out_pxl);
}
