package cv.lecturesight.regiontracker;

import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.util.Set;

public interface Region {
  
  int getLabel();

  long getFirstSeenTime();
  
  long getLastMoveTime();
  
  BoundingBox getBoundingBox();

  Position getCentroid();
  
  int getWeight();
  
  boolean isSplitter();
  
  boolean isGroup();
  
  boolean isGroupMember();
  
  Region getGroup();
        
  Set<Region> getGroupMembers();

  void update(int label, long lastMoveTime, Position new_centroid, BoundingBox bbox1, int weight);

  void setAxis(double dist);

  double getAxis();
}
