package cv.lecturesight.object.impl;

import cv.lecturesight.object.TrackerObject;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.util.HashMap;

public class TrackerObjectImpl implements TrackerObject {
  
  private static int runningId = 1;
  
  private int id;
  BoundingBox bbox;
  Position centroid;
  long lastSeen = 0L;
  HashMap<String, Object> props = new HashMap<String, Object>();
  
  public TrackerObjectImpl() {
    this.id = runningId++;
    this.bbox = new BoundingBox();
    this.centroid = new Position();
  }
  
  public TrackerObjectImpl(BoundingBox bbox, Position centroid) {
    this.id = runningId++;
    this.bbox = bbox;
    this.centroid = centroid;
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
  
}
