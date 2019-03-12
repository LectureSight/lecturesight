package cv.lecturesight.videoanalysis.rerdmann;

import cv.lecturesight.display.DisplayService;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.OpenCLService.Format;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.conf.ConfigurationListener;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import java.nio.ByteBuffer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.nio.FloatBuffer;

import static ImportFromFile.ImportCNN.*;
import com.nativelibs4java.opencl.CLImage3D;
import com.nativelibs4java.opencl.CLImageFormat;
import com.nativelibs4java.util.NIOUtils;
import java.util.Arrays;

//OSGI Definitionen
@Component(name = "lecturesight.videoanalysis", immediate = true)
@Service
@Properties({
    @Property(name = "osgi.command.scope", value = "va")
    ,
  @Property(name = "osgi.command.function", value = {"reset"})//es gibt die klasse = funktion für konsole
})
public class VideoAnalysisRErdmann implements ObjectTracker, ConfigurationListener {

    final int MAX_REGIONS = 36; // maximum number of foreground regions analysed by this Tracker
    final int MAX_TARGETS = 6;  // maximum number of targets this Tracker can track
    final int TARGET_SIZE = 32; // average size of a target in scene
    final int image_height = 64;
    final int image_width = 64;
    //HistogramBlocks
    final int block_height = 16;
    final int block_width = 16;
    //NormalisationBlocks
    final int cell_height = 8;
    final int cell_width = 8;
    final int histogram_bins = 9;
    
    //Declarations CNN (c = channel, f = filter, i = inputsize per channel, p = pool, w = weights, kernelsize=3x3) for quadratic images
            
    int features_size = (image_height / cell_height) * (image_width / cell_width) * histogram_bins;
    int descriptor_size = (2 * image_width / block_width - 1) * (2 * image_height / block_height - 1) * (block_width * block_height) / (cell_width * cell_height) * histogram_bins;
    
    //Format for 3d images
    CLImageFormat format = new CLImageFormat(CLImageFormat.ChannelOrder.RGBA, CLImageFormat.ChannelDataType.Float);
    // configuration properties for this service (resources/conf/default.properties)--
    private final static String PROPKEY_TESTPARAM = "param1"; //siehe Other Sources conf

    // variables holding the values of the configuration properties (updated by configurationChanged())
    int param1; //wird in der Konfiguation von Lecturesight angezeigt

    //OSGI Service
    @Reference
    Configuration config;       // configuration parameters (z.B. param1

    @Reference
    OpenCLService ocl;          // OpenCL service
    //OCL Signale=Kommunikation Graka CPU
    OCLSignal sig_START;        // signal triggering processing of new frame
    OCLSignal sig_VA_DONE;      // signal indicating that VideoAnalysis is done

    @Reference
    DisplayService dsps;        // display service

    @Reference
    FrameSourceProvider fsp;    // service providing the input FrameSource
    FrameSource fsrc;           // input FrameSource

    //Ausführung OpenCL in Java (2 Teile: Start (launch) z.B. Farbkonvertierung, Ende(land) OCLSignal ausgeben + Ergebnisse auslesen) siehe: 
    ComputationRun ocl_test_run;    // example of a ComputationRun

    // -- OpenCL Kernels --
    //CLKernel test_kernel;           // example of kernel used in ocl_test_run
    CLKernel sobel_kernel;
    CLKernel grayscale_kernel;
    CLKernel hog_kernel_gradient;
    CLKernel hog_kernel_normalization;

    CLKernel nn_kernel;
    CLKernel bi_kernel;
    CLKernel crop_kernel;
    
    
    CLKernel input_layer_kernel;
    CLKernel conv_layer_kernel;
    CLKernel pooling_layer_kernel;
    CLKernel dense_layer_kernel;
    CLKernel print_3d_kernel;
    CLKernel max_pooling_kernel;

    // -- GPU Buffers -- (siehe openCL-Klasse)
    CLImage2D input_rgb;            // input image GPU buffer
    CLImage2D test_result_image;    // GPU buffer for result of ocl_test_run
    CLImage2D io_grayscale_image;
    CLImage2D hog_gradient_image;
    CLImage2D cropped_image;
    CLImage2D input_layer_image;

    CLImage2D nn_image;
    CLImage2D bi_image;

    // GPU buffers for object data
    CLBuffer<Integer> centroids_gpu;      // GPU array of centroids of tracked objects [x1,y1,x2,y2,...,xN,yN]
    CLBuffer<Integer> bboxes_gpu;         // GPU array of bounding boxes of tracked objects [xa1,ya1,xb1,yb1,...,xaN,yaN,xbN,ybN]
    CLBuffer<Integer> head_pos_gpu;       // GPU array of head positions [x1,y1,x2,y2,...,xN,yN]

    // GPU buffers for HOG
    CLBuffer<Float> features_gpu;
    CLBuffer<Float> features_empty_gpu;
    CLBuffer<Float> descriptor_gpu;
    CLBuffer<Float> norm_gpu;
    CLBuffer<Float> norm_factor_gpu;
    CLBuffer<Float> svm_y_gpu;
    CLBuffer<Float> prediction_gpu;

    //GPU buffers for CNN  
        
    CLImage3D conv2d_1_gpu;
    CLBuffer<Float> conv2d_1_weights_gpu;
    CLBuffer<Float> conv2d_1_bias_gpu;
    CLImage3D pool_1_gpu;
    CLImage3D conv2d_2_gpu;
    CLBuffer<Float> conv2d_2_weights_gpu;
    CLBuffer<Float> conv2d_2_bias_gpu;
    CLImage3D pool_2_gpu;
    CLImage3D conv2d_3_gpu;
    CLBuffer<Float> conv2d_3_weights_gpu;
    CLBuffer<Float> conv2d_3_bias_gpu;
    CLImage3D pool_3_gpu;
    CLImage2D dense_1_gpu;
    CLImage2D dense_2_gpu;
    CLBuffer<Float> test_gpu;
   
    // Host arrays for object data (Klone der GPU Buffer zum speichern der Ergebnisse)
    int[] region_centroids;               // recieves values from centroids_gpu
    int[] region_bboxes;                  // recieves values from bboxes_gpu
    int[] region_headpos;                 // recieves values from head_pos_gpu
    int numRegions;                       // count of foreground regions

    int numTargets = 0;                   // OUTPUT: current number of targets
    Target[] targets;                     // OUTPUT ARRAY: Targets in this array are displayed by the Tracker UI

    // dimensions of input images
    int[] imageWorkDim;         // work group size of per-pixel kernels
    int[] workingDim = new int[]{image_height, image_width};
    int[] cropDim = new int[]{480, 480};
    // GPU max workgroup size
    long gpu_maxworkgroupsize;

    //Buffer to reset feature_Buffer (features_gpu)
    ByteBuffer buf = ByteBuffer.allocate(1000);
    // initially loads configuration property values (hold die defaultProperties in den Code z.B. macimale Anz an Ziele)

    private void mapParameters() {
        param1 = config.getInt(PROPKEY_TESTPARAM);
    }

    // get OpenCL kernels
    private void getKernels() {
        //test_kernel = ocl.programs().getKernel("test", "test_processing"); //Kerneldatei, Kernelname
        sobel_kernel = ocl.programs().getKernel("sobel", "SobelDetector"); //Kerneldatei, Kernelname
        grayscale_kernel = ocl.programs().getKernel("grayscale", "grayscale_processing"); //Kerneldatei, Kernelname
        hog_kernel_gradient = ocl.programs().getKernel("hog", "compute_hog_gradient"); //Kerneldatei, Kernelname    
        hog_kernel_normalization = ocl.programs().getKernel("hog_norm", "compute_hog_norm"); //Kerneldatei, Kernelname

        nn_kernel = ocl.programs().getKernel("nearestNeighbor", "nearest_neighbor"); //Kerneldatei, Kernelname
        bi_kernel = ocl.programs().getKernel("billinear", "bilinear_interpolation"); //Kerneldatei, Kernelname
        crop_kernel = ocl.programs().getKernel("cropKernel", "crop"); //Kerneldatei, Kernelname
        
        input_layer_kernel = ocl.programs().getKernel("input_layer", "compute_input_layer"); //Kerneldatei, Kernelname
        conv_layer_kernel = ocl.programs().getKernel("ConvLayer", "compute_conv_layer"); //Kerneldatei, Kernelname
        print_3d_kernel = ocl.programs().getKernel("print3DImage", "compute_print_3d_image"); //Kerneldatei, Kernelname
        max_pooling_kernel = ocl.programs().getKernel("PoolingLayer", "compute_max_pooling"); //Kerneldatei, Kernelname
    }

    // allocates an OpenCL Image buffer (Allokiert Speicherplatz für Bilder hier für das Resultatbild) Sinnvoll für z.B. nn
    private CLImage2D allocImage2D(Format format, int[] dim) {
        return ocl.context().createImage2D(CLMem.Usage.InputOutput, format.getCLImageFormat(), dim[0], dim[1]);
    }

    // initialize GPU buffers (e.g. setting image buffers to black)
    private void initBuffers() {
        ocl.utils().setValues(0, 0, imageWorkDim[0], imageWorkDim[1], test_result_image, 0, 0, 0, 255);
        
    }

    // registers displays that show up in the UI
    private void registerDisplays() {
        dsps.registerDisplay("test_output", test_result_image, sig_VA_DONE); //Titel, GPU-Buffer, der dargestellt werden soll, Signal dass Ausgabe trigert)
        dsps.registerDisplay("hog_gradient_output", hog_gradient_image, sig_VA_DONE); //Titel, GPU-Buffer, der dargestellt werden soll, Signal dass Ausgabe trigert)
        dsps.registerDisplay("grayscale_output", io_grayscale_image, sig_VA_DONE); //Titel, GPU-Buffer, der dargestellt werden soll, Signal dass Ausgabe trigert)

        dsps.registerDisplay("nn_output", nn_image, sig_VA_DONE); //Titel, GPU-Buffer, der dargestellt werden soll, Signal dass Ausgabe trigert)
        dsps.registerDisplay("bi_output", bi_image, sig_VA_DONE); //Titel, GPU-Buffer, der dargestellt werden soll, Signal dass Ausgabe trigert)
        dsps.registerDisplay("cropped_image", cropped_image, sig_VA_DONE); //Titel, GPU-Buffer, der dargestellt werden soll, Signal dass Ausgabe trigert)
        dsps.registerDisplay("input_layer_image", input_layer_image, sig_VA_DONE); //Titel, GPU-Buffer, der dargestellt werden soll, Signal dass Ausgabe trigert)
    }

    // allocate GPU buffers and host arrays
    private void allocateBuffers() throws IllegalStateException {
        // obtain input image buffer from FrameSource
        input_rgb = fsrc.getImage();

        // allocate working buffers
        test_result_image = allocImage2D(Format.RGBA_UINT8, imageWorkDim);
        io_grayscale_image = allocImage2D(Format.RGBA_UINT8, workingDim);
        hog_gradient_image = allocImage2D(Format.RGBA_UINT8, workingDim);
        
        cropped_image = allocImage2D(Format.RGBA_UINT8, cropDim);
        input_layer_image = allocImage2D(Format.RGBA_UINT8, workingDim);
        nn_image = allocImage2D(Format.RGBA_UINT8, workingDim);
        bi_image = allocImage2D(Format.RGBA_UINT8, workingDim);

        // allocate buffers and arrays for object data
        centroids_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS * 2);
        bboxes_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS * 4);
        head_pos_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS * 2);
        
        //allocate buffers and arrays for HOG
        features_gpu = ocl.context().createFloatBuffer(CLMem.Usage.InputOutput, features_size);
        features_empty_gpu = ocl.context().createFloatBuffer(CLMem.Usage.InputOutput, features_size);
        descriptor_gpu = ocl.context().createFloatBuffer(CLMem.Usage.InputOutput, descriptor_size);

        //allocate buffers and arrays for CNN
        conv2d_1_gpu = ocl.context().createImage3D(CLMem.Usage.InputOutput, format,64 ,64 ,32 ); // Siehe Heterogenous computing opencl P.80 //old: new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelDataType.Float)
        pool_1_gpu = ocl.context().createImage3D(CLMem.Usage.InputOutput, format,13 ,13 ,64 );
         
        /*conv2d_2_gpu = ocl.context().createImage3D(CLMem.Usage.InputOutput, new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelDataType.Float),conv2d_2_i ,conv2d_2_i ,conv2d_2_c ); // Siehe Heterogenous computing opencl P.80
        conv2d_3_gpu = ocl.context().createImage3D(CLMem.Usage.InputOutput, new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelDataType.Float),conv2d_3_i ,conv2d_3_i ,conv2d_3_c ); // Siehe Heterogenous computing opencl P.80
        dense_1_gpu = ocl.context().createImage2D(CLMem.Usage.InputOutput, new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelDataType.Float),dense_1_c ,dense_1_f); // Siehe Heterogenous computing opencl P.80
        dense_2_gpu = ocl.context().createImage2D(CLMem.Usage.InputOutput, new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelDataType.Float),dense_1_c ,dense_1_f); // Siehe Heterogenous computing opencl P.80
        cnn_out_gpu = ocl.context().createFloatBuffer(CLMem.Usage.InputOutput, 2);*/
        test_gpu = ocl.context().createFloatBuffer(CLMem.Usage.InputOutput, 169);
        // init host arrays for object data
        region_centroids = new int[MAX_REGIONS * 2];
        region_bboxes = new int[MAX_REGIONS * 4];
        region_headpos = new int[MAX_REGIONS * 2];

        // init Targets array
        targets = new Target[MAX_TARGETS];

    }
    
    protected void activate(ComponentContext cc) {
 
        fsrc = fsp.getFrameSource();              // obtain frame source

        sig_START = fsrc.getSignal();             // obtain start signal
        sig_VA_DONE = ocl.getSignal("VA_DONE");   // create finish signal

        // determine input image dimensions (=work group size for image processing kernel)
        imageWorkDim = new int[]{fsrc.getWidth(), fsrc.getHeight()};

        // do the usual init steps
        mapParameters();      // get computation parameters from configuration
        getKernels();         // get kernels
        allocateBuffers();    // allocate GPU buffers
        initBuffers();        // initialize GPU buffers
        registerDisplays();   // register displays

        // set up and register ComputationRuns
        ocl_test_run = new ExampleComputationRun();
        ocl.registerLaunch(sig_START, ocl_test_run); //Ausführung des Computationruns wenn sig_START geworfen wird

        // set up and register Tracker UI
        UserInterface trackerUI = new TrackerUI(this);
        cc.getBundleContext().registerService(UserInterface.class.getName(), trackerUI, null);

        Logger.info("Activated.");

        //Import CNN Weights and Bias
        float[][][] conv2d_1 = readWeights("/home/rerdmann/Desktop/MasterThesis/INRIAPerson/weights/Conv2d_1.txt", 3, 64, 9);
        float[][][] conv2d_1_weights = extractWeights(conv2d_1);
        float[] conv2d_1_bias = extractBias(conv2d_1);
        conv2d_1_weights_gpu = makeFloatBuffer(ocl, getFlatWeights(conv2d_1_weights)); 
        conv2d_1_bias_gpu = makeFloatBuffer(ocl, conv2d_1_bias);
        System.out.println("Imported Weights of Convolutional Layer as: Conv2d_1");

        float[][][] conv2d_2 = readWeights("/home/rerdmann/Desktop/MasterThesis/INRIAPerson/weights/Conv2d_2.txt", 64, 32, 9);
        float[][][] conv2d_2_weights = extractWeights(conv2d_2);
        float[] conv2d_2_bias = extractBias(conv2d_2);
        conv2d_1_weights_gpu = makeFloatBuffer(ocl, getFlatWeights(conv2d_2_weights)); 
        conv2d_1_bias_gpu = makeFloatBuffer(ocl, conv2d_2_bias);
        System.out.println("Imported Weights of Convolutional Layer as: Conv2d_2");

        float[][][] conv2d_3 = readWeights("/home/rerdmann/Desktop/MasterThesis/INRIAPerson/weights/Conv2d_3.txt", 32, 32, 9);
        float[][][] conv2d_3_weights = extractWeights(conv2d_3);
        float[] conv2d_3_bias = extractBias(conv2d_3);
        conv2d_1_weights_gpu = makeFloatBuffer(ocl, getFlatWeights(conv2d_3_weights)); 
        conv2d_1_bias_gpu = makeFloatBuffer(ocl, conv2d_3_bias);
        System.out.println("Imported Weights of Convolutional Layer as: Conv2d_3");
    }

    protected void deactivate(ComponentContext cc) throws Exception {
        Logger.info("Deactivated.");
    }

    class ExampleComputationRun implements ComputationRun {

        @Override
        public void launch(CLQueue queue) {
            // Ignore when shutting down
            if (ocl == null) {
                return;
            }

            features_empty_gpu.copyTo(queue, features_gpu);
            //WICHTIG!!! mal schauen ob der sobel auf rgb und dann grayscale besser ist als sobel mit grayscale
            crop_kernel.setArgs(input_rgb, cropped_image);
            crop_kernel.enqueueNDRange(queue, cropDim);
            nn_kernel.setArgs(cropped_image, nn_image);
            nn_kernel.enqueueNDRange(queue, workingDim);
            bi_kernel.setArgs(cropped_image, bi_image);
            bi_kernel.enqueueNDRange(queue, workingDim);

            input_layer_kernel.setArgs(bi_image, conv2d_1_weights_gpu, conv2d_1_bias_gpu, conv2d_1_gpu,input_layer_image);
            input_layer_kernel.enqueueNDRange(queue, new int[]{64, 64, 32});
            max_pooling_kernel.setArgs(conv2d_1_gpu, pool_1_gpu, 5, 5);
            max_pooling_kernel.enqueueNDRange(queue, new int[]{13, 13, 64});
            
            //Testkernel to check if 3d image is written   
            print_3d_kernel.setArgs(pool_1_gpu,test_gpu);
            print_3d_kernel.enqueueNDRange(queue, new int[]{13, 13}); 
            print_CLBuffer_Float(queue, test_gpu);
            
            
            //grayscale_kernel.setArgs(nn_image, io_grayscale_image);
            //grayscale_kernel.enqueueNDRange(queue, workingDim);    
            //sobel_kernel.setArgs(cropped_image, test_result_image);
            //sobel_kernel.enqueueNDRange(queue, cropDim);
            //hog_kernel_gradient.setArgs(io_grayscale_image,hog_gradient_image,features_gpu);
            //hog_kernel_gradient.enqueueNDRange(queue, workingDim);  
            //hog_kernel_normalization.setArgs(features_gpu, descriptor_gpu);
            //hog_kernel_normalization.enqueueNDRange(queue, new int[]{(image_width*2/block_width)-1,(image_height*2/block_height)-1});  //Dimension als Grid von Histogrammen ohne die äußeren 
        }

        @Override
        public void land() { //Auslesen von Ergebnissen
            // Ignore when shutting down
            if (ocl == null) {
                return;
            }
            ocl.castSignal(sig_VA_DONE);
        }
    }

// cv.lecturesight.util.conf.ConfigurationListener methods  ____________________
//
    @Override //Falls Parameter Live gädert werden
    public void configurationChanged() {
        if (param1 != config.getInt(PROPKEY_TESTPARAM)) {
            param1 = config.getInt(PROPKEY_TESTPARAM);
            Logger.info("Setting param1 to {}", param1);
        }
    }

// cv.lecturesight.objecttracker.ObjectTracker methods _________________________
//
    @Override
    public OCLSignal getSignal() {
        return sig_VA_DONE;
    }

    @Override
    public TrackerObject getObject(int id) {
        for (Target t : targets) {
            if (t != null && t.id == id) {
                return t.to;
            }
        }
        return null;
    }

    @Override
    public boolean isCurrentlyTracked(TrackerObject object) {
        for (Target t : targets) {
            if (t != null && t.id == object.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void discardObject(TrackerObject object) {
        for (Target t : targets) {
            if (t != null && t.id == object.getId()) {
                //discardTarget(t);
            }
        }
    }

    @Override
    public Map<Integer, TrackerObject> getAllObjects() {
        Map m = new HashMap<Integer, TrackerObject>();
        for (Target t : targets) {
            if (t != null) {
                m.put(t.id, t.to);
            }
        }
        return m;
    }

    @Override
    public List<TrackerObject> getCurrentlyTracked() {
        long seen_before = System.currentTimeMillis();
        List l = new LinkedList<TrackerObject>();
        for (Target t : targets) {
            if ((t != null) && (t.first_seen < seen_before)) {
                l.add(t.to);
            }
        }
        return l;
    }

    // Console Commands __________________________________________________________
    public void reset(String[] args) {
        for (int i = 0; i < MAX_TARGETS; i++) {
            targets[i] = null;
        }
        numTargets = 0;
        Logger.info("Clearing target list.");
    }
}
