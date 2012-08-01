package cv.lecturesight.regiontracker;

import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.util.Map;

public interface Region {

  int getId();
  
  int getRegionLabel();
  
  long firstSeen();
  
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
  
  Region getGroup();
        
}
