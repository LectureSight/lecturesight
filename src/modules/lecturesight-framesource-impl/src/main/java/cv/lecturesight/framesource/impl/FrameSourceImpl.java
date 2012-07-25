package cv.lecturesight.framesource.impl;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.image.BufferedImage;
import java.nio.Buffer;

class FrameSourceImpl implements FrameSource {

  private FrameGrabber frameGrabber;
  private FrameUploader uploader;
  private long frameNumber = 0L;

  public FrameSourceImpl(FrameGrabber frameGrabber, FrameUploader loader) {
    this.frameGrabber = frameGrabber;
    this.uploader = loader;
  }

  @Override
  public OCLSignal getSignal() {
    return uploader.getSignal();
  }

  @Override
  public CLImage2D getImage() {
    return uploader.getOutputImage();
  }

  @Override
  public void captureFrame() throws FrameSourceException {
    try {
      Buffer buf = frameGrabber.captureFrame();
      uploader.upload(buf);
    } catch (Exception e) {
      throw new FrameSourceException("Unable to capture frame.", e);
    }
    frameNumber++;
  }

  @Override
  public int getWidth() {
    return frameGrabber.getWidth();
  }

  @Override
  public int getHeight() {
    return frameGrabber.getHeight();
  }
  
  public long getFrameNumber() {
    return frameNumber;
  }

  @Override
  public BufferedImage getImageHost() {
    return uploader.getOutputImageHost();
  }
}
