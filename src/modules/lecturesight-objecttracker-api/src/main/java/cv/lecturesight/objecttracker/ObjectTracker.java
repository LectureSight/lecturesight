package cv.lecturesight.objecttracker;

import cv.lecturesight.opencl.api.OCLSignal;
import java.util.List;

/** Camera Operator Service API
 * 
 */
public interface ObjectTracker {
  
  OCLSignal getSignal();
  
  TrackerObject getObject(int id);
  
  boolean isCurrentlyTracked(TrackerObject object);
  
  void discardObject(TrackerObject object);
  
  List<TrackerObject> getAllObjects();
  
  List<TrackerObject> getCurrentlyTracked();
  
}
