package cv.lecturesight.object;

import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;

public interface TrackerObject {

  int getId();

  BoundingBox getBoundingBox();

  Position getCentroid();
  
  long lastSeen();
  
  boolean hasProperty(String key);
  
  Object getProperty(String key);
  
  void setProperty(String key, Object value);
        
}
