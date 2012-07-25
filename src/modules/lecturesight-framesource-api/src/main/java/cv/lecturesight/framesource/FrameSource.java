package cv.lecturesight.framesource;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.image.BufferedImage;

public interface FrameSource {

  OCLSignal getSignal();

  CLImage2D getImage();
  
  BufferedImage getImageHost();

  void captureFrame() throws FrameSourceException;

  int getWidth();

  int getHeight();
  
  long getFrameNumber();

}
