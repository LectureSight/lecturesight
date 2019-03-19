package cv.lecturesight.ptz.steering.absolutemove;

import cv.lecturesight.util.geometry.NormalizedPosition;

public interface MovementListener {

  public void moveStart(NormalizedPosition current, NormalizedPosition target);
  
  public void moveStop(NormalizedPosition current, NormalizedPosition target);
}
