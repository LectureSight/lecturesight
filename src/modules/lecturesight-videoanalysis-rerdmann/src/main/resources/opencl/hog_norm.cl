#define HISTOGRAM_BINS 9
#define CELL_WIDTH 8
#define CELL_HEIGHT 8
#define BLOCK_WIDTH 16
#define BLOCK_HEIGHT 16

#define CELLS_IN_BLOCK  ((CELL_WIDTH/CELL_WIDTH) * (CELL_HEIGHT/CELL_HEIGHT))
#define DESCRIPTOR_SIZE (CELLS_IN_BLOCK * HISTOGRAM_BINS)

const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_FILTER_NEAREST | CLK_ADDRESS_CLAMP_TO_EDGE;

//Kernel zur Normalisierung
//Dim = Azahl Zellen Pro Zeile x Anzahl Zellen pro Reihe
//1 Thread für 1 Histogramm (9 Zellen des feature Vektor)
//Werte quadrieren und aufsummieren
//Wurzel ziehen
//einzelne Zellen verrechnen und im Deskriptor speichern
__kernel void compute_hog_norm(
    __read_only __global float *features,
    __write_only __global float *descriptor)
{
    int2 pos = (int2)(get_global_id(0), get_global_id(1));
    int width = get_global_size(0);
    int height = get_global_size(1); 
    float sum = 0;
    __local float tmp[4*HISTOGRAM_BINS];
    /*
    Histogramgrid
    -----------------
    |0  |1  |2  |3  |
    -----------------
    |4  |5  |6  |7  |
    -----------------
    |8  |9  |10 |11 |
    -----------------
    */
    
    //Hist 0+1    
    int index = pos.y* HISTOGRAM_BINS * width + pos.x*HISTOGRAM_BINS;
    for( int i= 0;i<2*HISTOGRAM_BINS;i++)
    {
        tmp[i]= features[index+i];
        sum += features[index+i] * features[index+i];
    }
    
    //Hist 4+5    
    index = pos.y+1* HISTOGRAM_BINS * width + pos.x*HISTOGRAM_BINS;
    for( int i= 0;i<2*HISTOGRAM_BINS;i++)
    {
        tmp[i+18]= features[index+i];
        sum += features[index+i] * features[index+i];
    }
    
    
    
    //Berechnung Normalisationsfaktor
    float norm = sqrt(sum);
    barrier(CLK_LOCAL_MEM_FENCE);
    //schreiben des descriptors       
    index = (pos.y*width+pos.x)*(4*HISTOGRAM_BINS);
    for( int i= 0;i<4*HISTOGRAM_BINS;i++)
    {
        descriptor[index+i] = tmp[i] / norm;
    }
}