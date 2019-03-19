package com.wulff.lecturesight.viscaoverip.service;

import com.wulff.lecturesight.visca.api.CameraPosition;

public class CameraState {

  int focus = -1;

  CameraPosition position = new CameraPosition();

  public boolean isMoving() {
    return false;
  }

  public CameraPosition currentPosition() {
    return position;
  }

  public int currentFocus() {
    return focus;
  }
}
