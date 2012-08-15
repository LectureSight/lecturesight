package cv.lecturesight.regiontracker.impl;

import cv.lecturesight.regiontracker.Region;
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
import cv.lecturesight.regiontracker.RegionTracker;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 */
@Component(name = "lecturesight.regiontracker", immediate = true)
@Service
public class RegionTrackerImpl implements RegionTracker {

  // collection of this services signals
  private EnumMap<RegionTracker.Signal, OCLSignal> signals =
          new EnumMap<RegionTracker.Signal, OCLSignal>(RegionTracker.Signal.class);
  private Log log = new Log("Region Tracker");
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
  boolean debugEnabled = false;
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
  CLImage2D overlap, current_fg, last_fg;
  int maxRegions;
  Map<Integer, RegionImpl> trackedRegions = new TreeMap<Integer, RegionImpl>();

  protected void activate(ComponentContext cc) throws Exception {
    signals.put(Signal.DONE_COMPUTE_OVERLAP, ocl.getSignal(Constants.SIGNAME_DONE_COMPUTE_OVERLAP));
    signals.put(Signal.DONE_CORRELATION, ocl.getSignal(Constants.SIGNAME_DONE_CORRELATION));

    fsrc = fsp.getFrameSource();

    // get global working range
    workDim = new int[]{(int) fgs.getForegroundMap().getWidth(),
      (int) fgs.getForegroundMap().getHeight()};
    maxRegions = config.getInt(Constants.PROPKEY_OBJECTS_MAX);

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
    current_fg = fgs.getForegroundMap();
    last_fg = ocl.context().createImage2D(Usage.InputOutput,
            Format.INTENSITY_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    ocl.utils().setValues(0, 0, workDim[0], workDim[1], last_fg, 0);
    overlap = ocl.context().createImage2D(Usage.InputOutput,
            Format.INTENSITY_UINT8.getCLImageFormat(), workDim[0], workDim[1]);

    // init buffer for label pairs
    label_pairs = ocl.context().createIntBuffer(Usage.InputOutput, Constants.pairsBufferLength);

    // init overlap computation run
    ocl.registerLaunch(fgs.getSignal(ForegroundService.Signal.DONE_CLEANING), new OverlapImageRun());

    // init CC-Labeler on overlap image
    overlapLabeler = ccs.createLabeler(overlap, fgs.getLabeler().getMaxBlobs(), 4, Integer.MAX_VALUE);

    // init correlation run
    ocl.registerLaunch(overlapLabeler.getSignal(ConnectedComponentLabeler.Signal.DONE), new CorrelationRun());

    registerDisplays();
    debugEnabled = config.getBoolean(Constants.PROPKEY_DEBUG);

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
  public Region getRegionByLabel(int id) {
    if (trackedRegions.containsKey(id)) {
      return trackedRegions.get(id);
    } else {
      return null;
    }
  }

  @Override
  public boolean isTracked(Region obj) {
    return trackedRegions.containsValue(obj);
  }

  @Override
  public List<Region> getRegions() {
    List<Region> out = new LinkedList<Region>();
    out.addAll(trackedRegions.values());
    return out;
  }

  @Override
  public int numRegions() {
    return trackedRegions.size();
  }
  
  @Override
  public void discardRegion(Region obj) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  //</editor-fold>

  private void updateTracking() {
    long currentTime = System.currentTimeMillis();
    Map<Integer, RegionImpl> newTrackedRegions = new TreeMap<Integer, RegionImpl>();
    List<Integer> regions = makeRegionList();
    Map<Integer, Set<Integer>> mergeGroups = findMergers(regions);
    Map<Integer, Set<Integer>> splitGroups = findSplitters(regions);

    // care about mergeGroups
    if (mergeGroups.size() > 0) {
      if (debugEnabled) {
        System.out.println("--- Merges ---");
      }
      for (Iterator<Integer> it = mergeGroups.keySet().iterator(); it.hasNext();) {
        int groupId = it.next();
        Set<Integer> mergerIds = mergeGroups.get(groupId);
        int heaviest = findHeaviestRegion(mergerIds);
        if (debugEnabled) {
          System.out.println(groupId + " <--- " + heaviest);
        }
        RegionImpl region = updateTrackerObject(groupId, trackedRegions.get(heaviest));
        for (Iterator<Integer> mit = mergerIds.iterator(); mit.hasNext();) {
          int mId = mit.next();
          if (mId != heaviest) {
            if (debugEnabled) {
              System.out.println(" + " + mId);
            }
            RegionImpl mregion = trackedRegions.get(mId);
            region.members.add(mregion);
// TODO: also join members here? faltten memebrs list?
//            if (mregion.isGroup()) {
//              for (Iterator<TrackerObject> mbit = mregion.members.iterator(); mbit.hasNext();) {
//                region.members.add(mbit.next());
//              }
//            }
          }
        }
        ((RegionImpl)region).splitter = false;
        newTrackedRegions.put(groupId, region);
      }
    }

    // care about splitGroups
    if (splitGroups.size() > 0) {
      if (debugEnabled) {
        System.out.println("--- Splits ---");
      }
      for (Iterator<Integer> it = splitGroups.keySet().iterator(); it.hasNext();) {
        int groupId = it.next();
        Set<Integer> splitterIds = splitGroups.get(groupId);
        int heaviest = findHeaviestOldRegion(splitterIds);
        if (debugEnabled) {
          System.out.println(groupId + ": [" + heaviest + "]");
        }
        RegionImpl region = updateTrackerObject(heaviest, trackedRegions.get(groupId));
        region.splitter = false;
        newTrackedRegions.put(heaviest, region);
        for (Iterator<Integer> sit = splitterIds.iterator(); sit.hasNext();) {
          int sId = sit.next();
          if (sId != heaviest) {
            Region match = findMatchingRegion(sId, region.members, 1.0f);
            if (match != null) {
              if (debugEnabled) {
                System.out.println(" u " + sId);
              }
              region.members.remove(match);
              ((RegionImpl)match).splitter = true;
              newTrackedRegions.put(sId, updateTrackerObject(sId, (RegionImpl) match));
            } else {
              if (debugEnabled) {
                System.out.println(" * " + sId);
              }
              RegionImpl newRegion = createTrackerRegion(sId);
              newRegion.splitter = true;
              newTrackedRegions.put(sId, newRegion);
            }
          }
        }
      }
    }

    // care for the remaining (singleton) regions
    if (regions.size() > 0) {
      if (debugEnabled) {
        System.out.println("--- Singleton ---");
      }
      for (Iterator<Integer> it = regions.iterator(); it.hasNext();) {
        int regionId = it.next();
        int lastId = findCorrelation(regionId);
        if (debugEnabled) {
          System.out.print(regionId + " <--- " + lastId);
        }
        RegionImpl region;
        if (lastId > 0) {
          region = trackedRegions.get(lastId);
          if (region == null) {
            log.warn("Region " + lastId + " was not present in last frame!");
          }
          if (debugEnabled) {
            System.out.println(" u");
          }
          region = updateTrackerObject(regionId, region);
        } else {
          if (debugEnabled) {
            System.out.println(" *");
          }
          region = createTrackerRegion(regionId);
        }
        ((RegionImpl)region).splitter = false;
        newTrackedRegions.put(regionId, region);
      }
    }

    trackedRegions = newTrackedRegions;
    if (debugEnabled) {
      if (debugEnabled) {
        System.out.println("Tracked Regions: " + trackedRegions.size() + " ");
      }
      for (Iterator<Integer> it = trackedRegions.keySet().iterator(); it.hasNext();) {
        int rid = it.next();
        if (debugEnabled) {
          System.out.println(rid + " " + trackedRegions.get(rid));
        }
      }
      if (debugEnabled) {
        System.out.println();
      }
    }
  }

  private List<Integer> makeRegionList() {
    List<Integer> out = new LinkedList<Integer>();
    for (int i = 1; i <= fgLabeler.getNumBlobs(); i++) {
      out.add(i);
    }
    return out;
  }

  private Integer findHeaviestOldRegion(Set<Integer> ids) {
    int winner = 0, maxWeight = -1;
    for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
      int id = it.next();
      if (id > 0) {                                        // FIXME workaround for region correlation sometimes producing negative IDs
        int weight = fgLabeler.getSize(id);
        if (weight > maxWeight) {
          winner = id;
          maxWeight = weight;
        }
      }
    }
    return winner;
  }

  private Integer findHeaviestRegion(Set<Integer> ids) {
    int winner = 0, maxWeight = -1;
    for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
      int id = it.next();
      int weight = trackedRegions.get(id).getWeight();
      if (weight > maxWeight) {
        winner = id;
        maxWeight = weight;
      }
    }
    return winner;
  }

  private int findCorrelation(int currentId) {
    if (currentId > 0) {                                   // FIXME workaround for region correlation sometimes producing negative IDs
      for (int i = 0; i < num_corrs; i++) {
        int idx = 2 * i;
        if (pairs[idx] == currentId) {
          return pairs[++idx];
        }
      }
    }
    return 0;
  }

  private Region findMatchingRegion(int matcheeId, Set<Region> regions, float maxErr) {
    Region out = null;
    float winErr = Float.MAX_VALUE;
//    for (Iterator<TrackerObject> it = regions.iterator(); it.hasNext();) {
//    }
    return out;
  }

  private Map<Integer, Set<Integer>> findMergers(List<Integer> regions) {
    Map<Integer, Set<Integer>> out = new TreeMap<Integer, Set<Integer>>();
    for (int i = 0; i < num_corrs; i++) {
      int idx = 2 * i;
      int current = pairs[idx];
      int last = pairs[++idx];
      if (current > 0 && last > 0) {                       // FIXME workaround for region correlation sometimes producing negative IDs
        if (!out.containsKey(current)) {
          out.put(current, new TreeSet<Integer>());
        }
        out.get(current).add(last);
      }
    }
    Map<Integer, Set<Integer>> result = new TreeMap<Integer, Set<Integer>>();
    for (Iterator<Integer> it = out.keySet().iterator(); it.hasNext();) {
      Integer id = it.next();
      if (out.get(id).size() > 1) {
        result.put(id, out.get(id));
        regions.remove(id);
      }
    }
    return result;
  }

  private Map<Integer, Set<Integer>> findSplitters(List<Integer> regions) {
    Map<Integer, Set<Integer>> out = new TreeMap<Integer, Set<Integer>>();
    for (int i = 0; i < num_corrs; i++) {
      int idx = 2 * i;
      int current = pairs[idx];
      int last = pairs[++idx];
      if (current > 0 && last > 0) {                       // FIXME workaround for region correlation sometimes producing negative IDs
        if (!out.containsKey(last)) {
          out.put(last, new TreeSet<Integer>());
        }
        out.get(last).add(current);
      }
    }
    Map<Integer, Set<Integer>> result = new TreeMap<Integer, Set<Integer>>();
    for (Iterator<Integer> it = out.keySet().iterator(); it.hasNext();) {
      int id = it.next();
      if (out.get(id).size() > 1) {
        result.put(id, out.get(id));
        regions.removeAll(out.get(id));
      }
    }
    return result;
  }

  private RegionImpl createTrackerRegion(int regionId) {
    RegionImpl region = 
            new RegionImpl(regionId, 
                    centroidFinder.getControid(regionId), 
                    boxFinder.getBox(regionId), 
                    fgLabeler.getSize(regionId));
    return region;
  }

  private RegionImpl updateTrackerObject(int regionId, RegionImpl region) {
    region.update(regionId, 
            centroidFinder.getControid(regionId), 
            boxFinder.getBox(regionId), 
            fgLabeler.getSize(regionId));
    return region;
  }

  private class OverlapImageRun implements ComputationRun {

    OCLSignal SIG_done = signals.get(Signal.DONE_COMPUTE_OVERLAP);
    CLKernel imageAndK = ocl.programs().getKernel("objects", "image_and");

    @Override
    public void launch(CLQueue queue) {
      imageAndK.setArgs(current_fg, last_fg, overlap);
      imageAndK.enqueueNDRange(queue, workDim);
      ocl.utils().copyImage(0, 0, workDim[0], workDim[1], current_fg, 0, 0, last_fg);
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
      int numBlobs = overlapLabeler.getNumBlobs();
      // gather pairs
      if (numBlobs > 0) {
        gatherLabelPairsK.setArgs(addresses, labels_current, labels_last, label_pairs, numBlobs, Constants.pairsBufferLength);    // TODO length / 2 !!
        pairsWorkDim[0] = numBlobs;
        gatherLabelPairsK.enqueueNDRange(queue, pairsWorkDim);
        pairsH = label_pairs.read(queue);
      }

      // copy label buffer
      labels_current.copyTo(queue, 0, labels_current.getElementCount(), labels_last, 0);
    }

    @Override
    public void land() {
      if (debugEnabled) {
        System.out.println("\n--[ t=" + fsrc.getFrameNumber() + " ]-----------------------------------------------");
        System.out.print("Labels : ");
        int numBlobs = fgLabeler.getNumBlobs();
        for (int i = 0; i <= numBlobs; i++) {
          System.out.print(fgLabeler.getLabels()[i] + " ");
        }
      }
      num_corrs = overlapLabeler.getNumBlobs();
      if (num_corrs > 0) {
        pairsH.get(pairs);
        if (debugEnabled) {
          System.out.print("\nPairs  : ");
          System.out.print(num_corrs + " ");
          for (int i = 0; i < num_corrs; i++) {
            int idx = 2 * i;
            System.out.print(pairs[idx] + ":" + pairs[++idx] + " ");
          }
          System.out.println();
        }
      }
      updateTracking();
      ocl.castSignal(SIG_done);
    }
  }
}
