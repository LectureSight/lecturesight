const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics: enable
#pragma OPENCL EXTENSION cl_khr_global_int32_extended_atomics : enable

#define CLAMP_POS(pos) if (pos.x >= width || pos.y >= height) return
#define ENCODE_INDEX(pos) pos.x + pos.y * (width+2)
#define BLACK (uint4)(0,0,0,255)
#define GREEN (uint4)(0,255,0,255)
#define RED   (uint4)(255,0,0,255)

int read_val
(
	__read_only image2d_t input,
	int x, int y
)
{
	int2 pos = (int2)(x, y);
	uint4 pixel = read_imageui(input, sampler, pos);
	return pixel.s0;
}

__kernel void compute_add_sub_mask
(
	__read_only  image2d_t change,
	__read_only  image2d_t bgdiff,
	__write_only image2d_t update_mask
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	uint4 out_pxl = BLACK;

	uint4 change_pxl = read_imageui(change, sampler, pos);
	if (change_pxl.s0 > 0) 
	{
		out_pxl = RED;

    uint4 center = read_imageui(bgdiff, sampler, pos);
    int sum = center.s1;                          
    if (sum <= 0)                                           
    {
      sum += (uint4)(read_imageui(bgdiff, sampler, (int2)(pos.x-1, pos.y-1))).s1;
      sum += (uint4)(read_imageui(bgdiff, sampler, (int2)(pos.x,   pos.y-1))).s1;
      sum += (uint4)(read_imageui(bgdiff, sampler, (int2)(pos.x+1, pos.y-1))).s1;

      sum += (uint4)(read_imageui(bgdiff, sampler, (int2)(pos.x-1, pos.y))).s1;
      sum += (uint4)(read_imageui(bgdiff, sampler, (int2)(pos.x+1, pos.y))).s1;

      sum += (uint4)(read_imageui(bgdiff, sampler, (int2)(pos.x-1, pos.y+1))).s1;
      sum += (uint4)(read_imageui(bgdiff, sampler, (int2)(pos.x,   pos.y+1))).s1;
      sum += (uint4)(read_imageui(bgdiff, sampler, (int2)(pos.x+1, pos.y+1))).s1;
    }
		if (sum > 0) 
		{
			out_pxl = GREEN;
		}
	}
	
	barrier( CLK_GLOBAL_MEM_FENCE );
	write_imageui(update_mask, pos, out_pxl);
}


__kernel void update_foreground
(
	__read_only  image2d_t update_mask,
	__write_only image2d_t output
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	uint4 mask_pxl = read_imageui(update_mask, sampler, pos);
	if (mask_pxl.s0 == 255)
	{
		write_imageui(output, pos, 0);
	}
	else if (mask_pxl.s1 == 255)
	{
		write_imageui(output, pos, 255);
	}		
}


__kernel void remove_smallblobs
(
	__global     int*      work,
	__write_only image2d_t mask,
	             int       width, 
               int       height
)
{
	int2 pos = (int2)( get_global_id(0)+1, 
                     get_global_id(1)+1 );
  int2 pos_img = (int2)( pos.x-1, pos.y-1 );
	CLAMP_POS(pos);

	int adr = ENCODE_INDEX(pos);
	int idx = work[adr];
	if (idx > 0) 
	{
		write_imageui(mask, pos_img, 0);
	}
}

__kernel void image_dilate8
(
	__read_only  image2d_t input,
	__write_only image2d_t output	
)
{
	int2 pos = (int2)(get_global_id(0), get_global_id(1));
	uint out_pxl = 0;
	
	int sum = read_val(input, pos.x, pos.y);

	sum += read_val(input, pos.x-1, pos.y-1);
	sum += read_val(input, pos.x,   pos.y-1);
	sum += read_val(input, pos.x+1, pos.y-1);

	sum += read_val(input, pos.x-1, pos.y);
	sum += read_val(input, pos.x+1, pos.y);

	sum += read_val(input, pos.x-1, pos.y+1);
	sum += read_val(input, pos.x,   pos.y+1);
	sum += read_val(input, pos.x+1, pos.y+1);

	if (sum > 0)
	{
		out_pxl = 255;
	}
	write_imageui(output, pos, out_pxl);
}

__kernel void reset_buffer
(
	__global int* buffer,
	int value
)
{
	int pos = get_global_id(0);
	buffer[pos] = value;
}


__kernel void refresh_decay
(
  __read_only  image2d_t input,
  __write_only image2d_t current,
  __write_only image2d_t output,
  __global     int* labels,
  __global     int* activity,
               int alpha,
               int width 
)
{
  int2 pos_img = (int2)(get_global_id(0), get_global_id(1));
  int2 pos = (int2)(pos_img.x+1, pos_img.y+1);
  
  int val = 0;
  uint4 in_pxl = read_imageui(input, sampler, pos_img);
  if (in_pxl.s0 != 0) 
  {
    int adr = ENCODE_INDEX(pos);
    int id = -1 * labels[adr];
    if (activity[id] != 0) 
    {
      val = 255;
    }
    else 
    {
      val = in_pxl.s0 - alpha;
      if (val < 0) 
      {
        val=0;
      }
    }
  }
  uint4 out_pxl = (uint4)(val, val, val, 255);
  write_imageui(current, pos_img, out_pxl);
  write_imageui(output, pos_img, out_pxl);
}

__kernel void gather_activity
(
  __global int* labels,
  __global int* activity,
  __read_only image2d_t fg_map,
  __read_only image2d_t update_map,
  int width
)
{
  int2 pos_img = (int2)(get_global_id(0), get_global_id(1));
  int2 pos = (int2)(pos_img.x+1, pos_img.y+1);
  uint4 update_pxl = read_imageui(update_map, sampler, pos_img);
  if (update_pxl.s1 != 0) 
  {
    uint4 fg_pxl = read_imageui(fg_map, sampler, pos_img);
    if (fg_pxl.s0 != 0)
    {
      int adr = ENCODE_INDEX(pos);
      int idx = -1 * labels[adr];
      atom_inc(activity + idx);       // FIXME this line yields CL_INVALID_BINARY on NVIDIA cards
//      activity[idx] = 1;
    }
  }
}

