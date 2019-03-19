package cv.lecturesight.framesource.kinect;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameSourceException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.openkinect.freenect.DepthHandler;
import org.openkinect.freenect.FrameMode;

public class KinectDepthFrameGrabber implements FrameGrabber, DepthHandler {

  int devIndex;
  private ByteBuffer currentFrame = null;

  public KinectDepthFrameGrabber(int devIndex) {
    this.devIndex = devIndex;
  }

  @Override
  public Buffer captureFrame() throws FrameSourceException {
    return currentFrame;
  }

  @Override
  public void onFrameReceived(FrameMode fm, ByteBuffer bb, int i) {
    currentFrame = bb;
  }

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
}
