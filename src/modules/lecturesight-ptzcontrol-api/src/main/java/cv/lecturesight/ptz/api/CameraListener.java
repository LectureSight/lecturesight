package cv.lecturesight.ptz.api;

import cv.lecturesight.util.geometry.Position;

public interface CameraListener {

  public void positionUpdated(Position pos);
}