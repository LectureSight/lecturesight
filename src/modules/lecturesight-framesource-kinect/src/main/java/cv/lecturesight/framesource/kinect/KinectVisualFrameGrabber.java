package cv.lecturesight.framesource.kinect;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameSourceException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.openkinect.freenect.FrameMode;
import org.openkinect.freenect.VideoHandler;

public class KinectVisualFrameGrabber implements FrameGrabber, VideoHandler {

  int devIndex;
  ByteBuffer currentFrame;

  public KinectVisualFrameGrabber(int devIndex) {
    this.devIndex = devIndex;
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
    return PixelFormat.RGB_8BIT;
  }

  @Override
  public Buffer captureFrame() throws FrameSourceException {
    return currentFrame;
  }

  @Override
  public void onFrameReceived(FrameMode fm, ByteBuffer bb, int i) {
    currentFrame = bb;
  }

}
