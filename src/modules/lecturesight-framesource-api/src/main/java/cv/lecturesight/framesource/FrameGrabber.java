package cv.lecturesight.framesource;

import java.nio.Buffer;

public interface FrameGrabber {

  public enum PixelFormat{RGB_8BIT, BGRA_8BIT};

  int getWidth();
  int getHeight();
  PixelFormat getPixelFormat();
  Buffer captureFrame() throws FrameSourceException;

}
