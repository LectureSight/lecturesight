package cv.lecturesight.ptz.steering.relativemove;

import cv.lecturesight.util.geometry.NormalizedPosition;

public interface MovementListener {

  void moveStart(NormalizedPosition current, NormalizedPosition target);

  void moveStop(NormalizedPosition current, NormalizedPosition target);
}
