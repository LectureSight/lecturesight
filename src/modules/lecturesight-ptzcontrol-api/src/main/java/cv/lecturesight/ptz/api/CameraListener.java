package cv.lecturesight.ptz.api;

import cv.lecturesight.util.geometry.Position;

public interface CameraListener {
  void positionUpdated(Position pos);
}
