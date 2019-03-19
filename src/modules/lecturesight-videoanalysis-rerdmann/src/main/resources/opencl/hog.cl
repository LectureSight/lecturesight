/**
 *  Histogram of oriented gradient on GPU 
 *  by Jakub Vojvoda, vojvoda@swdeveloper.sk
 *  2015  
 *
 *  Implementation based on the original publication of Dalal and Triggs
 *  https://lear.inrialpes.fr/people/triggs/pubs/Dalal-cvpr05.pdf
 *
 *  licence: GNU LGPL v3
 *  file: hog.cl
 */

#define HISTOGRAM_BINS 9
#define CELL_WIDTH 8
#define CELL_HEIGHT 8
#define BLOCK_WIDTH 16
#define BLOCK_HEIGHT 16

#define CELLS_IN_BLOCK  ((CELL_WIDTH/CELL_WIDTH) * (CELL_HEIGHT/CELL_HEIGHT))
#define DESCRIPTOR_SIZE (CELLS_IN_BLOCK * HISTOGRAM_BINS)

const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

__kernel void compute_hog_gradient(
    __read_only image2d_t input,
    __write_only image2d_t output, 
    __write_only __global float *features)
{ 
    // global index
    int2 pos = (int2)(get_global_id(0), get_global_id(1));

    int width = get_global_size(0);
    int height = get_global_size(1); 

    if (pos.x >= width || pos.y >= height) { 
        return;
    }

    barrier(CLK_LOCAL_MEM_FENCE);

    float magnitude = 0; 
    float angle = 0;

    // computation of magnitude and angle of gradient
    if (pos.x > 0 && pos.x < width - 1 && pos.y > 0 && pos.y < height - 1) {     

        int i00=read_imageui(input, sampler, (int2)(pos.x-1,pos.y-1)).x;
        int i10=read_imageui(input, sampler, (int2)(pos.x,pos.y-1)).x;
        int i20=read_imageui(input, sampler, (int2)(pos.x+1,pos.y-1)).x;
        int i01=read_imageui(input, sampler, (int2)(pos.x-1,pos.y)).x;
        //int i11=read_imageui(input, sampler, (int2)(pos.x,pos.y)).x;
        int i21=read_imageui(input, sampler, (int2)(pos.x+1,pos.y)).x;
        int i02=read_imageui(input, sampler, (int2)(pos.x-1,pos.y+1)).x;
        int i12=read_imageui(input, sampler, (int2)(pos.x,pos.y+1)).x;
        int i22=read_imageui(input, sampler, (int2)(pos.x+1,pos.y+1)).x;

        float vert=-i00-2*i10-i20+i02+2*i12+i22;
        float horiz=-i00-2*i01-i02+i20+2*i21+i22;

        magnitude  = sqrt(vert * vert + horiz * horiz);

        float signed_angle = (horiz != 0) ? atan(vert / horiz) * 180.0 / M_PI : 0;
        angle = (signed_angle < 0) ? signed_angle + 180 : signed_angle;
    } 
    barrier( CLK_GLOBAL_MEM_FENCE );//synchro der Threads im Grid => wichtig wegen des ifs. Hier werden alle Threads gleichzeitig ausgeführt
    write_imageui(output, pos, (uint4)(magnitude,magnitude,magnitude,255));

    // unweighted histogram calculation
    // bin and factor calculation
    int bin_addr_1 = floor(angle / 20.0)-1;
    int bin_addr_2 = (bin_addr_1+1)%8;
    int factor_bin_2 = (angle/20)-bin_addr_1-1;
    int factor_bin_1 = 1-factor_bin_2;

    //start-index of feature_vector
    int blocks_per_row = width/CELL_WIDTH;
    int x_index = floor((float)(pos.x/CELL_WIDTH))*HISTOGRAM_BINS;
    int y_index = floor((float)(pos.y/CELL_HEIGHT))*blocks_per_row*HISTOGRAM_BINS;
    int index = x_index + y_index;

    //schreiben der Features
    features[index+bin_addr_1]=features[index+bin_addr_1] + magnitude * factor_bin_1;
    features[index+bin_addr_2]=features[index+bin_addr_2] + magnitude * factor_bin_2;

    barrier(CLK_LOCAL_MEM_FENCE);
}