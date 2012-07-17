package cv.lecturesight.object;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.awt.image.BufferedImage;

public interface TrackerObject {

  int getId();
  
  long lastSeen();

  BoundingBox getBoundingBox();

  Position getCentroid();
  
  boolean hasProperty(String key);
  
  Object getProperty(String key);
  
  void setProperty(String key, Object value);
  
  boolean isGroup();
  
  boolean isGroupMember();
  
  TrackerObject getGroup();
  
  BufferedImage getVisual();
  
  CLImage2D getVisualCL();
        
}
