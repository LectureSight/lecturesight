package cv.lecturesight.ptz.steering.api;

import cv.lecturesight.util.geometry.NormalizedPosition;

/** Camera Steering Worker API
 * 
 */
public interface CameraSteeringWorker {
 
  void start();
  void stop();
  boolean isSteering();
  void setSteering(boolean on);
  boolean isMoving();
  void stopMoving();
  void setTargetPosition(NormalizedPosition pos);
  NormalizedPosition getTargetPosition();
  NormalizedPosition getActualPosition();

}
