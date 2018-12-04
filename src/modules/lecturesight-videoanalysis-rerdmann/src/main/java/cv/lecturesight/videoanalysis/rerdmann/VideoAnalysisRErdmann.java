package cv.lecturesight.videoanalysis.rerdmann;

import cv.lecturesight.videoanalysis.rerdmann.Target;
import cv.lecturesight.videoanalysis.rerdmann.Box;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.cca.ConnectedComponentService;
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
import cv.lecturesight.util.geometry.Position;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component(name = "lecturesight.videoanalysis", immediate = true)
@Service
@Properties({
  @Property(name = "osgi.command.scope", value = "va"),
  @Property(name = "osgi.command.function", value = {"reset"})
})
public class VideoAnalysisRErdmann implements ObjectTracker, ConfigurationListener {

  final int MAX_REGIONS = 36; // maximum number of foreground regions analysed by this Tracker
  final int MAX_TARGETS = 6;  // maximum number of targets this Tracker can track
  final int TARGET_SIZE = 32; // average size of a target in scene

  // configuration properties for this service (resources/conf/default.properties)--
  private final static String PROPKEY_TESTPARAM = "param1";

  // variables holding the values of the configuration properties (updated by configurationChanged())
  int param1;

  @Reference
  Configuration config;       // configuration parameters

  @Reference
  OpenCLService ocl;          // OpenCL service
  OCLSignal sig_START;        // signal triggering processing of new frame
  OCLSignal sig_VA_DONE;      // signal indicating that VideoAnalysis is done

  @Reference
  DisplayService dsps;        // display service

  @Reference
  FrameSourceProvider fsp;    // service providing the input FrameSource
  FrameSource fsrc;           // input FrameSource

  ComputationRun ocl_test_run;    // example of a ComputationRun

  // -- OpenCL Kernels --
  CLKernel test_kernel;           // example of kernel used in ocl_test_run

  // -- GPU Buffers --
  CLImage2D input_rgb;            // input image GPU buffer
  CLImage2D test_result_image;    // GPU buffer for result of ocl_test_run

  // GPU buffers for object data
  CLBuffer<Integer> centroids_gpu;      // GPU array of centroids of tracked objects [x1,y1,x2,y2,...,xN,yN]
  CLBuffer<Integer> bboxes_gpu;         // GPU array of bounding boxes of tracked objects [xa1,ya1,xb1,yb1,...,xaN,yaN,xbN,ybN]
  CLBuffer<Integer> head_pos_gpu;       // GPU array of head positions [x1,y1,x2,y2,...,xN,yN]

  // Host arrays for object data
  int[] region_centroids;               // recieves values from centroids_gpu
  int[] region_bboxes;                  // recieves values from bboxes_gpu
  int[] region_headpos;                 // recieves values from head_pos_gpu
  int numRegions;                       // count of foreground regions

  int numTargets = 0;                   // OUTPUT: current number of targets
  Target[] targets;                     // OUTPUT ARRAY: Targets in this array are displayed by the Tracker UI

  // dimensions of input images
  int[] imageWorkDim;         // work group size of per-pixel kernels

  // GPU max workgroup size
  long gpu_maxworkgroupsize;


  // initially loads configuration property values
  private void mapParameters() {
    param1 = config.getInt(PROPKEY_TESTPARAM);
  }

  // get OpenCL kernels
  private void getKernels() {
    test_kernel = ocl.programs().getKernel("test", "test_processing");
  }

  // allocates an OpenCL Image buffer
  private CLImage2D allocImage2D(Format format, int[] dim) {
    return ocl.context().createImage2D(CLMem.Usage.InputOutput, format.getCLImageFormat(), dim[0], dim[1]);
  }

  // initialize GPU buffers (e.g. setting image buffers to black)
  private void initBuffers() {
    ocl.utils().setValues(0, 0, imageWorkDim[0], imageWorkDim[1], test_result_image, 0, 0, 0, 255);
  }

  // registers displays that show up in the UI
  private void registerDisplays() {
    dsps.registerDisplay("test_output", test_result_image, sig_VA_DONE);
  }

  // allocate GPU buffers and host arrays
  private void allocateBuffers() throws IllegalStateException {
    // obtain input image buffer from FrameSource
    input_rgb = fsrc.getImage();

    // allocate working buffers
    test_result_image = allocImage2D(Format.RGBA_UINT8, imageWorkDim);

    // allocate buffers and arrays for object data
    centroids_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS * 2);
    bboxes_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS * 4);
    head_pos_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS * 2);

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
    ocl.registerLaunch(sig_START, ocl_test_run);

    // set up and register Tracker UI
    UserInterface trackerUI = new TrackerUI(this);
    cc.getBundleContext().registerService(UserInterface.class.getName(), trackerUI, null);

    Logger.info("Activated.");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    Logger.info("Deactivated.");
  }


  class ExampleComputationRun implements ComputationRun {

    @Override
    public void launch(CLQueue queue) {

      // Ignore when shutting down
      if (ocl == null)
        return;

      test_kernel.setArgs(input_rgb, test_result_image);
      test_kernel.enqueueNDRange(queue, imageWorkDim);
    }

    @Override
    public void land() {
      // Ignore when shutting down
      if (ocl == null)
        return;

      ocl.castSignal(sig_VA_DONE);
    }
  }


// cv.lecturesight.util.conf.ConfigurationListener methods  ____________________
//

  @Override
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
        discardTarget(t);
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
    // return trackable objects which are older than object_min_active milliseconds
    long seen_before = System.currentTimeMillis() - object_min_active;
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
