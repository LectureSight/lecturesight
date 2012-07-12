package cv.lecturesight.framesource.impl;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.opencl.api.OCLSignal;
import java.nio.Buffer;

public interface FrameUploader {

  OCLSignal getSignal();

  CLImage2D getOutputImage();

  void upload(Buffer frame);

}
