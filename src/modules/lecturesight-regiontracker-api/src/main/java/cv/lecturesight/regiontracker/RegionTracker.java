package cv.lecturesight.regiontracker;

import cv.lecturesight.opencl.api.OCLSignal;
import java.util.List;

/** Service API
 * 
 */
public interface RegionTracker {
  
  enum Signal {
    DONE_COMPUTE_OVERLAP,
    DONE_CORRELATION,
    DONE_VISUAL
  }
  
  OCLSignal getSignal(Signal signal);
  boolean isTracked(Region obj);
  Region getRegionByLabel(int id);
  List<Region> getRegions();
  int numRegions();
  void strengthenRegion(Region region);
  void discardRegion(Region region);
  
}
