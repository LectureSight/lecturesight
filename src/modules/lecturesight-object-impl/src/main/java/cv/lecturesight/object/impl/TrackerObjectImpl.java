package cv.lecturesight.object.impl;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.object.TrackerObject;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TrackerObjectImpl implements TrackerObject {
  
  private static int runningId = 1;
  
  private int id;
  BoundingBox bbox = null;
  Position centroid = null;
  long lastSeen = 0L;
  HashMap<String, Object> props = new HashMap<String, Object>();
  TrackerObject group = null;
  Set<TrackerObject> members = new HashSet<TrackerObject>();
  
  public TrackerObjectImpl() {
    this.id = runningId++;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return bbox;
  }

  @Override
  public Position getCentroid() {
    return centroid;
  }
  
  @Override
  public long lastSeen() {
    return lastSeen;
  }

  @Override
  public boolean hasProperty(String key) {
    return props.containsKey(key);
  }

  @Override
  public Object getProperty(String key) {
    return props.get(key);
  }

  @Override
  public void setProperty(String key, Object value) {
    props.put(key, value);
  }

  @Override
  public boolean isGroup() {
    return !members.isEmpty();
  }

  @Override
  public boolean isGroupMember() {
    return group != null;
  }

  @Override
  public TrackerObject getGroup() {
    return group;
  }

  @Override
  public BufferedImage getVisual() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public CLImage2D getVisualCL() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
