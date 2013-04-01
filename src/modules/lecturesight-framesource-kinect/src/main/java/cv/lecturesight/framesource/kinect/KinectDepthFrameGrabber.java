package cv.lecturesight.framesource.kinect;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameSourceException;
import java.nio.Buffer;

public class KinectDepthFrameGrabber implements FrameGrabber {

  @Override
  public int getWidth() {
    return Constants.KINECT_IMAGE_WIDTH;
  }

  @Override
  public int getHeight() {
    return Constants.KINECT_IMAGE_HEIGHT;
  }

  @Override
  public PixelFormat getPixelFormat() {
    return PixelFormat.INTENSITY_8BIT;
  }

  @Override
  public Buffer captureFrame() throws FrameSourceException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
