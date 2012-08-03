package cv.lecturesight.objecttracker;

import cv.lecturesight.opencl.api.OCLSignal;
import java.util.List;

/** Camera Operator Service API
 * 
 */
public interface ObjectTracker {
  
  static final String OBJ_PROPKEY_REGION = "obj.region";
  static final String OBJ_PROPKEY_BBOX = "obj.bbox";
  static final String OBJ_PROPKEY_CENTROID = "obj.centroid";
  static final String OBJ_PROPKEY_WEIGHT = "obj.weight";
  
  OCLSignal getSignal();
  
  TrackerObject getObject(int id);
  
  boolean isCurrentlyTracked(TrackerObject object);
  
  void discardObject(TrackerObject object);
  
  List<TrackerObject> getAllObjects();
  
  List<TrackerObject> getCurrentlyTracked();
  
}
