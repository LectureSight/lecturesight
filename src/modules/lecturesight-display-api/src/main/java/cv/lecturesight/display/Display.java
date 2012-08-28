package cv.lecturesight.display;

import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;

public interface Display {

  OCLSignal getSignal();
  BufferedImage getImage();
  JLabel getDisplayLabel();
  void setCustomRenderer(CustomRenderer renderer);

}
