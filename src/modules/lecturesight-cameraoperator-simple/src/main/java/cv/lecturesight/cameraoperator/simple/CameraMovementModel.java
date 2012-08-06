package cv.lecturesight.cameraoperator.simple;

import cv.lecturesight.util.geometry.NormalizedPosition;

public class CameraMovementModel {

  private NormalizedPosition targetPosition = new NormalizedPosition(0.0f, 0.0f);
  private NormalizedPosition actualPosition = new NormalizedPosition(0.0f, 0.0f);

  public NormalizedPosition getTargetPosition() {
    return targetPosition;
  }

  public void setTargetPosition(NormalizedPosition targetPosition) {
    this.targetPosition = targetPosition;
  }

  public NormalizedPosition getActualPosition() {
    return actualPosition;
  }

  public void setActualPosition(NormalizedPosition actualPosition) {
    this.actualPosition = actualPosition;
  }
  
  
}
