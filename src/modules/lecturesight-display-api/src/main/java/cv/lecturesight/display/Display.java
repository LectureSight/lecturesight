package cv.lecturesight.display;

import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.image.BufferedImage;

public interface Display {

  OCLSignal getSignal();
  BufferedImage getImage();
  DisplayPanel getDisplayPanel();
  boolean isActive();
  void activate();
  void deactivate();
  void addListener(DisplayListener listener);
  void removeListener(DisplayListener listener);
}
