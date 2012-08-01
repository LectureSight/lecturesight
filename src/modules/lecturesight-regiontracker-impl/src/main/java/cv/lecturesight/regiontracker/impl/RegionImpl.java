package cv.lecturesight.regiontracker.impl;

import cv.lecturesight.regiontracker.Region;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RegionImpl implements Region {

  private static int runningId = 1;
  int id;
  BoundingBox bbox = null;
  Position centroid = null;
  int weight = 0;
  int label;
  long firstSeen, lastSeen;
  HashMap<String, Object> props = new HashMap<String, Object>();
  Region group = null;
  Set<Region> members = new HashSet<Region>();

  public RegionImpl() {
    this.id = runningId++;
    this.firstSeen = System.currentTimeMillis();
    lastSeen = firstSeen;
  }

  @Override
  public int getId() {
    return id;
  }
  
  public int getRegionLabel() {
    return label;
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
  public long firstSeen() {
    return firstSeen;
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
  public Region getGroup() {
    return group;
  }

  @Override
  public Map<String, Object> getProperties() {
    return props;
  }
  
  @Override
  public String toString() {
    return new StringBuilder()
            .append("[id=")
            .append(id)
            .append(" weight=")
            .append(weight)
            .append(" box=")
            .append(bbox)
            .append(" pos=")
            .append(centroid)
            .append("]")
            .toString();
  }
}
