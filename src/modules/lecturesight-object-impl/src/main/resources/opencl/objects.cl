const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#define ENCODE_INDEX(pos) pos.x + pos.y * (width+2)
#define CLAMP_POS(pos) if (pos.x > width || pos.y > height) return
#define BLACK (uint4)(0,0,0,255)
#define WHITE (uint4)(255,255,255,255)

__kernel void image_and
(
  __read_only  image2d_t srcA,
  __read_only  image2d_t srcB,
  __write_only image2d_t result
)
{
  int2 pos = (int2)(get_global_id(0), get_global_id(1));
  uint4 pxlA = read_imageui(srcA, sampler, pos);
  uint4 pxlB = read_imageui(srcB, sampler, pos);
  uint4 pxlR = BLACK;
  if (pxlA.s0 > 0 && pxlB.s0 > 0) 
  {
    pxlR = WHITE;
  }
  barrier( CLK_GLOBAL_MEM_FENCE );
  write_imageui(result, pos, pxlR);
}

__kernel void gather_label_pairs
(
  __global int* addresses,
  __global int* labels_current,
  __global int* labels_last,
  __global int* pairs,
           int  num_pairs,
           int  max_num_pairs
)
{
  int tidx = get_global_id(0);
  if (tidx < max_num_pairs &&
      tidx < num_pairs)                         // we also have to check if we are in a fill-up thread
  {
    int  adr = addresses[tidx+1];               // addresses[0] = number of blobs -> addresses start at offset 1
    int2 pair = (int2)( -1*labels_current[adr],
                        -1*labels_last[adr] );

    vstore2(pair, tidx, pairs);
  }
}

