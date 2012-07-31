package cv.lecturesight.regiontracker;

import cv.lecturesight.opencl.api.OCLSignal;

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
  Region[] getRegions();
  int numRegions();
  void discardRegion(Region obj);
  
}
