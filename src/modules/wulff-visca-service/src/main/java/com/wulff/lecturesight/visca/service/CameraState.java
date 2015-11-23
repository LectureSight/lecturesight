package com.wulff.lecturesight.visca.service;

import com.wulff.lecturesight.visca.api.CameraPosition;

public class CameraState {

  CameraPosition position = new CameraPosition();

  public boolean isMoving() {
    return false;
  }

  public CameraPosition currentPosition() {
    return position;
  }
}
