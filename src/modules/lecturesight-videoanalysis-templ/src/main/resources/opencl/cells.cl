const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define CELL_SIZE 16
#define CELL_SIZE_8 8

#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel __attribute__ (( reqd_work_group_size (8, 8, 1)))
void cells_max_8
(
	__read_only  image2d_t input,
	__write_only image2d_t cells,
               int       thresh
)
{
  __local int buffer[64];    // local thread memory buffer for reduction

  int2 gpos = (int2)(get_global_id(0), get_global_id(1));
  int2 lpos = (int2)(get_local_id(0), get_local_id(1));
  int  idx  = get_local_size(0) * lpos.y + lpos.x;
  buffer[idx] = 0;

  // read image
  uint4 pxl_in = read_imageui(input, sampler, gpos);
  if (pxl_in.s0 > 0) {
    buffer[idx] = 1;
  } else {
    buffer[idx] = 0;
  }

  barrier( CLK_LOCAL_MEM_FENCE );

  // reduce: sum
  int num_threads = get_local_size(0) * get_local_size(1);
  for (int offset = num_threads / 2; offset > 1; offset >>= 1)
  {
    if (idx < offset)
    {
      buffer[idx] = buffer[idx] + buffer[idx+offset];
    }
    barrier( CLK_LOCAL_MEM_FENCE );
  }

  // write output
  if (idx == 0)
  {
    uint4 pxl_out = BLACK;
    if (buffer[idx] > thresh) pxl_out = WHITE;

    int2 cpos = gpos/8;
    write_imageui(cells, cpos, pxl_out);
  }
}


__kernel void viz_cells
(
  __read_only  image2d_t input,
  __read_only  image2d_t cells,
  __read_only  image2d_t change,
  __write_only image2d_t visual
)
{
  int2 pos  = (int2)(get_global_id(0), get_global_id(1));
  int2 cpos = pos / 8;

  uint4 input_pxl = read_imageui(input, sampler, pos);
  uint4 cells_pxl = read_imageui(cells, sampler, cpos);
  uint4 change_pxl = read_imageui(change, sampler, pos);
  uint4 out_pxl   = (uint4)(input_pxl.s0, input_pxl.s1, 255, 255);

  if (cells_pxl.s2 > 0 || (pos.x % 8 == 0 && pos.y % 8 == 0))
  {
    out_pxl = (uint4)(input_pxl.s0+50, input_pxl.s1+50, 255, 255);
  }

  barrier( CLK_GLOBAL_MEM_FENCE );
  write_imageui(visual, pos, out_pxl);
}
