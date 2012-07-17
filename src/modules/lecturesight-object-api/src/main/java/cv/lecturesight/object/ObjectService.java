package cv.lecturesight.object;

import cv.lecturesight.opencl.api.OCLSignal;

/** Service API
 * 
 */
public interface ObjectService {
  
  enum Signal {
    DONE_COMPUTE_OVERLAP,
    DONE_CORRELATION,
    DONE_VISUAL
  }
  
  OCLSignal getSignal(Signal signal);
  boolean isTracked(TrackerObject obj);
  TrackerObject getObject(int id);
  TrackerObject[] getAllObjects();
  TrackerObject[] getAllTrackedObjects();
  
}
