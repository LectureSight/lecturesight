package cv.lecturesight.framesource.v4l;

import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import cv.lecturesight.framesource.FrameGrabber.PixelFormat;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.util.Log;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class V4LFrameGrabber implements cv.lecturesight.framesource.FrameGrabber {

  private Log log;
  VideoDevice device;
  int width, height, standard, channel, quality;
  private FrameGrabber grabber;

  V4LFrameGrabber(VideoDevice device, int frameWidth, int frameHeight, int videoStandard,
    int videoChannel, int videoQuality) throws FrameSourceException {
    this.device = device;
    this.width = frameWidth;
    this.height = frameHeight;
    this.standard = videoStandard;
    this.channel = videoChannel;
    this.quality = videoQuality;

    try {
      log = new Log(device.getDeviceInfo().getName());
      grabber = device.getRGBFrameGrabber(width, height, channel, standard);
      grabber.startCapture();
    } catch (V4L4JException e) {
      throw new FrameSourceException("Failed to initialize FrameGrabber on device " + device.getDevicefile());
    }
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public PixelFormat getPixelFormat() {
    return PixelFormat.RGB_8BIT;
  }

  @Override
  public Buffer captureFrame() throws FrameSourceException {
    try {
      ByteBuffer bb = grabber.getFrame();
      return bb;
    } catch (V4L4JException ex) {
      throw new FrameSourceException("Could not capture frame from " + device.getDevicefile() + ": " + ex.getMessage());
    }
  }

  @Override
  public void finalize() throws Throwable {
    super.finalize();
    log.info("Shutting down");
    grabber.stopCapture();
    device.releaseFrameGrabber();
    device.release();
  }
}
