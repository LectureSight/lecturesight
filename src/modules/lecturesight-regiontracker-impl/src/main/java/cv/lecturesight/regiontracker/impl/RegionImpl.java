package cv.lecturesight.regiontracker.impl;

import cv.lecturesight.regiontracker.Region;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.util.HashSet;
import java.util.Set;

public class RegionImpl implements Region {

  long firstSeenTS, lastMoveTS;
  BoundingBox bbox = null;
  Position centroid = null;
  int weight = 0;
  int label;
  double axis = 0;
  Region group = null;
  Set<Region> members = new HashSet<Region>();
  boolean splitter = false;

  public RegionImpl(int label, long time, Position centroid, BoundingBox bbox, int weight) {
    this.firstSeenTS = time;
    this.lastMoveTS = time;
    this.label = label;
    this.centroid = centroid;
    this.bbox = bbox;
    this.weight = weight;
  }
  
  @Override
  public void update(int label, long time, Position centroid, BoundingBox bbox, int weight) {
    this.label = label;
    if (this.centroid != centroid) {
      lastMoveTS = time;
    }
    this.centroid = centroid;
    this.bbox = bbox;
    this.weight = weight;
  }
  
  @Override
  public int getLabel() {
    return label;
  }
  
  public void setAxis(double dist) {
    axis = dist;
  }
  
  public double getAxis() {
    return axis;
  }

  @Override
  public long getFirstSeenTime() {
    return firstSeenTS;
  }

  @Override
  public long getLastMoveTime() {
    return lastMoveTS;
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
  public int getWeight() {
    return weight;
  }

  @Override
  public boolean isSplitter() {
    return splitter;
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
  public Region getGroup() {
    return group;
  }

  @Override
  public Set<Region> getGroupMembers() {
    Set<Region> out = new HashSet<Region>();
    out.addAll(members);
    return out;
  }
  
  @Override
  public String toString() {
    return new StringBuilder()
            .append("[weight=")
            .append(weight)
            .append(" box=")
            .append(bbox)
            .append(" pos=")
            .append(centroid)
            .append("]")
            .toString();
  }
}
