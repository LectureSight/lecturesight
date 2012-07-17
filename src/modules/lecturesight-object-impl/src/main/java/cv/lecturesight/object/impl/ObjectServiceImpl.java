package cv.lecturesight.object.impl;

import cv.lecturesight.object.TrackerObject;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLIntBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.cca.BoundingBoxFinder;
import cv.lecturesight.cca.CentroidFinder;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.cca.ConnectedComponentService;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.object.ObjectService;
import cv.lecturesight.opencl.CLImageDoubleBuffer;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.OpenCLService.Format;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.OCLSignalBarrier;
import cv.lecturesight.ui.DisplayService;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 */
@Component(name="lecturesight.objects",immediate=true)
@Service
public class ObjectServiceImpl implements ObjectService {

  // collection of this services signals
  private EnumMap<ObjectService.Signal, OCLSignal> signals =
          new EnumMap<ObjectService.Signal, OCLSignal>(ObjectService.Signal.class);
  
  private Log log = new Log("Object Tracker Service");

  @Reference
  private Configuration config;
  @Reference
  private OpenCLService ocl;
  @Reference
  private DisplayService dsps;
  @Reference
  private ConnectedComponentService ccs;
  @Reference
  private FrameSourceProvider fsp;
  @Reference
  private ForegroundService fgs;
  
  int[] workDim;
  FrameSource fsrc;
  ConnectedComponentLabeler foregroundLabeler, overlapLabeler;
  BoundingBoxFinder boxFinder;
  CentroidFinder centroidFinder;
  OCLSignalBarrier analysisBarrier;
  CLIntBuffer labels_current, labels_last;
  CLIntBuffer label_pairs;
  int num_corrs;
  int[] pairs = new int[Constants.pairsBufferLength];
  CLImage2D overlap;
  CLImageDoubleBuffer fgBuffer;
  int max_objects;
  Map<Integer, TrackerObject> objects = new TreeMap<Integer,TrackerObject>();           // contains every obejct ever created TODO: this must be cleaned!!!
  Map<Integer, TrackerObject> trackedObjects = new TreeMap<Integer,TrackerObject>();

  protected void activate(ComponentContext cc) throws Exception {
    signals.put(Signal.DONE_COMPUTE_OVERLAP, ocl.getSignal(Constants.SIGNAME_DONE_COMPUTE_OVERLAP));
    signals.put(Signal.DONE_CORRELATION, ocl.getSignal(Constants.SIGNAME_DONE_CORRELATION));
    
    fsrc = fsp.getFrameSource();
    
    // get global working range
    workDim = new int[] { (int)fgs.getForegroundMap().getWidth(), 
                          (int)fgs.getForegroundMap().getHeight() };
    max_objects = config.getInt(Constants.PROPKEY_OBJECTS_MAX);

    foregroundLabeler = fgs.getLabeler();
    boxFinder = ccs.createBoundingBoxFinder(foregroundLabeler);
    centroidFinder = ccs.createCentroidFinder(foregroundLabeler);
    analysisBarrier = ocl.createSignalBarrier(new OCLSignal[] {
      fgs.getSignal(ForegroundService.Signal.DONE_CLEANING), 
      boxFinder.getSignal(BoundingBoxFinder.Signal.DONE),
      centroidFinder.getSignal(CentroidFinder.Signal.DONE)
    });
    
    // init label buffer copy 
    labels_current = foregroundLabeler.getLabelBuffer();
    labels_last = ocl.context().createIntBuffer(Usage.InputOutput, labels_current.getElementCount());
    ocl.utils().setValues(0, (int) labels_last.getElementCount() - 1, labels_last, 0);

    // init overlap image
    fgBuffer = fgs.getForegroundWorkingBuffer();
    overlap = ocl.context().createImage2D(Usage.InputOutput, 
            Format.INTENSITY_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    
    // init buffer for label pairs
    label_pairs = ocl.context().createIntBuffer(Usage.InputOutput, Constants.pairsBufferLength);
    
    // init overlap computation run
    ocl.registerLaunch(fgs.getSignal(ForegroundService.Signal.DONE_CLEANING), new OverlapImageRun());
    
    // init CC-Labeler on overlap image
    overlapLabeler = ccs.createLabeler(overlap, fgs.getLabeler().getMaxBlobs(), 1, Integer.MAX_VALUE);
    
    // init correlation run
    ocl.registerLaunch(overlapLabeler.getSignal(ConnectedComponentLabeler.Signal.DONE), new CorrelationRun());
    
    registerDisplays();
    
    log.info("Object Service activated.");
  }
  
  //<editor-fold defaultstate="collapsed" desc="Display Registration">
  /** Register displays if configured 
   * 
   */
  private void registerDisplays() {
    // register overlap display if configured
    if (config.getBoolean(Constants.PROPKEY_DISPLAY_OVERLAP)) {
      dsps.registerDisplay(Constants.WINDOWNAME_OVERLAP, "temporal overlap",
              overlap, signals.get(Signal.DONE_COMPUTE_OVERLAP));
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
  @Override
  public OCLSignal getSignal(Signal signal) {
    return signals.get(signal);
  }
    
  @Override
  public TrackerObject getObject(int id) {
    if (objects.containsKey(id)) {
      return objects.get(id);
    } else {
      return null;
    }
  }
  
  @Override
  public boolean isTracked(TrackerObject obj) {
    return trackedObjects.containsValue(obj);
  }

  @Override
  public TrackerObject[] getAllObjects() {
    return (TrackerObject[])objects.values().toArray();
  }

  @Override
  public TrackerObject[] getAllTrackedObjects() {
    return (TrackerObject[])trackedObjects.values().toArray();
  }
  //</editor-fold>

  private void updateTracking() {
    long currentTime = System.currentTimeMillis();
    Map<Integer,TrackerObject> newTrackedObjects = new TreeMap<Integer,TrackerObject>();
    int numObjs = fgs.getLabeler().getNumBlobs();
    for (int i = 0; i < numObjs; i++) {
      BoundingBox box = boxFinder.getBox(i);
      Position pos = centroidFinder.getControid(i);
      
      List<Integer> corrs = findCorrelations(i);
      
    }
  }
  
  private List<Integer> findCorrelations(int id) {
    List<Integer> corrs = new LinkedList<Integer>();
    
    return corrs;
  }
  
  private TrackerObject createTrackerObject() {
    return null;
  }
  
  private TrackerObject updateTrackerObject(TrackerObject obj, Position centroid, BoundingBox box) {
    return null;
  }
  
  private class OverlapImageRun implements ComputationRun {

    OCLSignal SIG_done = signals.get(Signal.DONE_COMPUTE_OVERLAP);
    CLKernel imageAndK = ocl.programs().getKernel("objects", "image_and");
    
    @Override
    public void launch(CLQueue queue) {
      imageAndK.setArgs(fgBuffer.current(), fgBuffer.last(), overlap);
      imageAndK.enqueueNDRange(queue, workDim);
    }

    @Override
    public void land() {
      overlapLabeler.doLabels();
      ocl.castSignal(SIG_done);
    }
  }
  
  private class CorrelationRun implements ComputationRun {
    
    OCLSignal SIG_done = signals.get(Signal.DONE_CORRELATION);
    CLKernel gatherLabelPairsK = ocl.programs().getKernel("objects", "gather_label_pairs");
    CLIntBuffer addresses = overlapLabeler.getIdBuffer();
    IntBuffer pairsH;
    int[] pairsWorkDim = new int[1];

    @Override
    public void launch(CLQueue queue) {
      ocl.utils().setValues(0, (int)label_pairs.getElementCount(), label_pairs, 0);
      // gather pairs
      if (overlapLabeler.getNumBlobs() > 0) {
        gatherLabelPairsK.setArgs(addresses, labels_current, labels_last, label_pairs, overlapLabeler.getNumBlobs(), Constants.pairsBufferLength);    // TODO length / 2 !!
        pairsWorkDim[0] = overlapLabeler.getNumBlobs();
        gatherLabelPairsK.enqueueNDRange(queue, pairsWorkDim);
        pairsH = label_pairs.read(queue);
      }
      
      // copy label buffer
      labels_current.copyTo(queue, 0, labels_current.getElementCount(), labels_last, 0);
    }

    @Override
    public void land() {
      num_corrs = overlapLabeler.getNumBlobs();
      if (num_corrs > 0) {
        pairsH.get(pairs);          
      }
      updateTracking();
      ocl.castSignal(SIG_done);
    }
  }
}
