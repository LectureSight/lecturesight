package cv.lecturesight.framesource.impl;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.image.BufferedImage;
import java.nio.Buffer;

public interface FrameUploader {

  OCLSignal getSignal();

  CLImage2D getOutputImage();
  
  BufferedImage getOutputImageHost();

  void upload(Buffer frame);

}
