package cv.lecturesight.object;

import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.util.Map;

public interface TrackerObject {

  int getId();
  
  long lastSeen();

  BoundingBox getBoundingBox();

  Position getCentroid();

  int getWeight();
  
  boolean hasProperty(String key);
  
  Object getProperty(String key);
  
  Map<String,Object> getProperties();
  
  void setProperty(String key, Object value);
  
  boolean isGroup();
  
  boolean isGroupMember();
  
  TrackerObject getGroup();
        
}
