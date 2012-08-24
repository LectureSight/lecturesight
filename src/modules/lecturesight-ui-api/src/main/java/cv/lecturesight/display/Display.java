package cv.lecturesight.display;

import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.image.BufferedImage;

public interface Display {

  void setTitle(String title);
  String getTitle();
  OCLSignal getSignal();
  BufferedImage getImage();
  void show();
  void hide();
  boolean isActive();
}
