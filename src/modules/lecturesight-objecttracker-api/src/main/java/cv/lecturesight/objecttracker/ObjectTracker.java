package cv.lecturesight.objecttracker;

import cv.lecturesight.opencl.api.OCLSignal;
import java.util.List;
import java.util.Map;

/** Camera Operator Service API
 * 
 */
public interface ObjectTracker {
  
  static final String OBJ_PROPKEY_REGION = "obj.region";
  static final String OBJ_PROPKEY_BBOX = "obj.bbox";
  static final String OBJ_PROPKEY_CENTROID = "obj.centroid";
  static final String OBJ_PROPKEY_WEIGHT = "obj.weight";
  static final String OBJ_PROPKEY_COLOR_HISTOGRAM = "color.histogram";
  static final String OBJ_PROPKEY_COLOR = "obj.color";
  static final String OBJ_PROPKEY_INSCENE = "obj.in_scene";
  static final String OBJ_PROPKEY_MOVEMENT = "obj.movement";
  
  OCLSignal getSignal();
  
  TrackerObject getObject(int id);
  
  boolean isCurrentlyTracked(TrackerObject object);
  
  void discardObject(TrackerObject object);
  
  Map<Integer, TrackerObject> getAllObjects();
  
  List<TrackerObject> getCurrentlyTracked();
  
}
