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
import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.Iterator;
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
@Component(name = "lecturesight.objects", immediate = true)
@Service
public class ObjectServiceImpl implements ObjectService {

  // collection of this services signals
  private EnumMap<ObjectService.Signal, OCLSignal> signals =
          new EnumMap<ObjectService.Signal, OCLSignal>(ObjectService.Signal.class);
  private Log log = new Log("Object Service");
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
//  @Reference 
//  private ObjectIdentityService identity;
  int[] workDim;
  FrameSource fsrc;
  ConnectedComponentLabeler fgLabeler, overlapLabeler;
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
  Map<Integer, TrackerObjectImpl> objects = new TreeMap<Integer, TrackerObjectImpl>();           // contains every obejct ever created, TODO: this must be cleaned!!!
  Map<Integer, TrackerObjectImpl> trackedObjects = new TreeMap<Integer, TrackerObjectImpl>();

  protected void activate(ComponentContext cc) throws Exception {
    signals.put(Signal.DONE_COMPUTE_OVERLAP, ocl.getSignal(Constants.SIGNAME_DONE_COMPUTE_OVERLAP));
    signals.put(Signal.DONE_CORRELATION, ocl.getSignal(Constants.SIGNAME_DONE_CORRELATION));

    fsrc = fsp.getFrameSource();

    // get global working range
    workDim = new int[]{(int) fgs.getForegroundMap().getWidth(),
      (int) fgs.getForegroundMap().getHeight()};
    max_objects = config.getInt(Constants.PROPKEY_OBJECTS_MAX);

    fgLabeler = fgs.getLabeler();
    boxFinder = ccs.createBoundingBoxFinder(fgLabeler);
    centroidFinder = ccs.createCentroidFinder(fgLabeler);
    analysisBarrier = ocl.createSignalBarrier(new OCLSignal[]{
              fgs.getSignal(ForegroundService.Signal.DONE_CLEANING),
              boxFinder.getSignal(BoundingBoxFinder.Signal.DONE),
              centroidFinder.getSignal(CentroidFinder.Signal.DONE)
            });

    // init label buffer copy 
    labels_current = fgLabeler.getLabelBuffer();
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

    log.info("Activated");
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
    return (TrackerObject[]) objects.values().toArray();
  }

  @Override
  public TrackerObject[] getAllTrackedObjects() {
    return (TrackerObject[]) trackedObjects.values().toArray();
  }

  @Override
  public void discardObject(TrackerObject obj) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  //</editor-fold>

  private void updateTracking() {
    long currentTime = System.currentTimeMillis();
    Map<Integer, TrackerObjectImpl> newTrackedObjects = new TreeMap<Integer, TrackerObjectImpl>();
    List<Integer> regions = makeRegionList();
    Map<Integer, List<Integer>> mergers = findMergers(regions);
    Map<Integer, List<Integer>> splitters = findSplitters(regions);

    // care about mergers
    if (mergers.size() > 0) {
      for (Iterator<Integer> it = mergers.keySet().iterator(); it.hasNext();) {
        int currentId = it.next();
        TrackerObjectImpl newGroup = createTrackerObject(currentId, currentTime);
        for (Iterator<Integer> git = mergers.get(currentId).iterator(); it.hasNext();) {
          int mergerId = git.next();
          TrackerObjectImpl merger = trackedObjects.get(mergerId);
          if (merger.isGroup()) {
            newGroup.members.addAll(merger.members);
            merger.members.clear();
            merger.group = newGroup;
            objects.remove(merger.getId());
          } else {
            newGroup.members.add(merger);
          }
        }
        newTrackedObjects.put(currentId, newGroup);
        // TODO call decorators for case: MERGE
      }
    }

    // care about splitters
    if (splitters.size() > 0) {
      for (Iterator<Integer> it = splitters.keySet().iterator(); it.hasNext();) {
        int lastId = it.next();
        TrackerObjectImpl oldObj = trackedObjects.get(lastId);
        List<Integer> splitterCurrentIds = splitters.get(lastId);
        int heaviestId = findHeaviestRegion(splitterCurrentIds);    // find biggest region
        splitterCurrentIds.remove(heaviestId);    
//        if (oldObj.isGroup()) {
//          for (Iterator<Integer> sit = splitterCurrentIds.iterator(); sit.hasNext();) {
//            // TODO use IdentityServiec here!
//          }
//        } else {                  // object that split was recognized as a single object before                  
          updateTrackerObject(heaviestId, oldObj, currentTime);       // update old object with biggest region data
          for (Iterator<Integer> sit = splitterCurrentIds.iterator(); sit.hasNext();) {     // create new objects for all others
            createTrackerObject(sit.next(), currentTime);
          }
//        }
      }
    }

    // care about remaining
    for (Iterator<Integer> it = regions.iterator(); it.hasNext();) {
      int currentId = it.next();
      int lastId = findCorrelation(currentId);
      if (lastId == 0) {
        createTrackerObject(currentId, currentTime);
      } else if (lastId > 0) {
        TrackerObjectImpl obj = trackedObjects.get(lastId);
        updateTrackerObject(currentId, obj, currentTime);
      }
    }

    trackedObjects = newTrackedObjects;
  }

  private List<Integer> makeRegionList() {
    List<Integer> out = new LinkedList<Integer>();
    for (int i = 0; i < fgLabeler.getNumBlobs(); i++) {
      out.add(i);
    }
    return out;
  }
  
  private Integer findHeaviestRegion(List<Integer> ids) {
    int winner = -1, maxWeight = -1;
    for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
      int id = it.next();
      int weight = fgLabeler.getSize(id);
      if (weight > maxWeight) {
        winner = id;
        maxWeight = weight;
      }
    }
    return winner;
  }

  private int findCorrelation(int currentId) {
    for (int i = 0; i < num_corrs; i++) {
      int idx = 2 * i;
      if (pairs[idx] == currentId) {
        return pairs[++idx];
      }
    }
    return 0;
  }

  private Map<Integer, List<Integer>> findMergers(List<Integer> regions) {
    Map<Integer, List<Integer>> out = new TreeMap<Integer, List<Integer>>();
    for (int i = 0; i < num_corrs; i++) {
      int idx = 2 * i;
      int current = pairs[idx];
      int last = pairs[++idx];
      if (!out.containsKey(current)) {
        out.put(current, new LinkedList<Integer>());
      }
      out.get(current).add(last);
    }
    Map<Integer, List<Integer>> result = new TreeMap<Integer, List<Integer>>();
    for (Iterator<Integer> it = out.keySet().iterator(); it.hasNext();) {
      int id = it.next();
      if (out.get(id).size() > 1) {
        result.put(id, out.get(id));
        regions.removeAll(out.get(id));
      }
    }
    return result;
  }

  private Map<Integer, List<Integer>> findSplitters(List<Integer> regions) {
    Map<Integer, List<Integer>> out = new TreeMap<Integer, List<Integer>>();
    for (int i = 0; i < num_corrs; i++) {
      int idx = 2 * i;
      int current = pairs[idx];
      int last = pairs[++idx];
      if (!out.containsKey(last)) {
        out.put(last, new LinkedList<Integer>());
      }
      out.get(last).add(current);
    }
    Map<Integer, List<Integer>> result = new TreeMap<Integer, List<Integer>>();
    for (Iterator<Integer> it = out.keySet().iterator(); it.hasNext();) {
      int id = it.next();
      if (out.get(id).size() > 1) {
        result.put(id, out.get(id));
        regions.removeAll(out.get(id));
      }
    }
    return result;
  }

  private TrackerObjectImpl createTrackerObject(int regionId, long timestamp) {
    TrackerObjectImpl obj = new TrackerObjectImpl();
    obj.bbox = boxFinder.getBox(regionId);
    obj.centroid = centroidFinder.getControid(regionId);
    obj.weight = fgLabeler.getSize(regionId);
    obj.lastSeen = timestamp;
    return obj;
  }

  private TrackerObjectImpl updateTrackerObject(int regionId, TrackerObjectImpl obj, long timestamp) {
    obj.bbox = boxFinder.getBox(regionId);
    obj.centroid = centroidFinder.getControid(regionId);
    obj.weight = fgLabeler.getSize(regionId);
    obj.lastSeen = timestamp;
    return obj;
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
      ocl.utils().setValues(0, (int) label_pairs.getElementCount(), label_pairs, 0);
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
        for (int i=0; i < num_corrs; i++) {
          int idx = 2 * num_corrs;
          System.out.print(pairs[idx] + ":" + pairs[idx++] + " ");
        }
        System.out.println("-------------------------------------------------");
      }
      updateTracking();
      ocl.castSignal(SIG_done);
    }
  }
}
