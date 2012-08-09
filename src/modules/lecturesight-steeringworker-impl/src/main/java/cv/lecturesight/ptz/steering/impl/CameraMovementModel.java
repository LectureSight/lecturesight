package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.util.geometry.NormalizedPosition;

public class CameraMovementModel {

  private String cameraName = "default";
  private NormalizedPosition targetPosition = new NormalizedPosition(0.0f, 0.0f);
  private NormalizedPosition actualPosition = new NormalizedPosition(0.0f, 0.0f);
  boolean moving = false;

  public CameraMovementModel(String cameraName) {
    this.cameraName = cameraName;
  }
  
  public NormalizedPosition getTargetPosition() {
    return targetPosition.clone();
  }

  public void setTargetPosition(NormalizedPosition targetPosition) {
    this.targetPosition.setX(targetPosition.getX());
    this.targetPosition.setY(targetPosition.getY());
  }

  public NormalizedPosition getActualPosition() {
    return actualPosition.clone();
  }

  public void setActualPosition(NormalizedPosition actualPosition) {
    this.actualPosition.setX(actualPosition.getX());
    this.actualPosition.setY(actualPosition.getY());
  }
  
  public void setMoving(boolean moving) {
    this.moving = moving;
  }
  
  public boolean isMoving() {
    return moving;
  }
  
  public String getCameraName() {
    return cameraName;
  }
}
