package cv.lecturesight.regiontracker;

import cv.lecturesight.opencl.api.OCLSignal;
import java.util.Set;

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
  Set<Region> getRegions();
  int numRegions();
  void discardRegion(Region obj);
  
}
