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
}
