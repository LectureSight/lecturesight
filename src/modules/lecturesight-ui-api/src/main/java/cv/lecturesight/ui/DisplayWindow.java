package cv.lecturesight.ui;

import cv.lecturesight.opencl.api.OCLSignal;

public interface DisplayWindow {

  void setTitle(String title);
  String getTitle();
  OCLSignal getSignal();
  void show();
  void hide();
  boolean isActive();
}
