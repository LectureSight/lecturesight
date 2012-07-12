package cv.lecturesight.object.impl;

import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;

public class TrackerObject {

  private static int runningId = 1;
  
  private int id;
  BoundingBox bbox;
  Position centroid;
  long lastSeen = 0L;
  
  public TrackerObject() {
    this.id = runningId++;
    this.bbox = new BoundingBox();
    this.centroid = new Position();
  }
  
  public TrackerObject(BoundingBox bbox, Position centroid) {
    this.id = runningId++;
    this.bbox = bbox;
    this.centroid = centroid;
  }

  public int getId() {
    return id;
  }

  public BoundingBox getBbox() {
    return bbox;
  }

  public Position getCentroid() {
    return centroid;
  }
  
  public long getLastSeen() {
    return lastSeen;
  }
        
}
