package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.util.geometry.NormalizedPosition;

public interface MoveListener {

  public void moveStart(NormalizedPosition current, NormalizedPosition target);
  
  public void moveStop(NormalizedPosition current, NormalizedPosition target);
}
