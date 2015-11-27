package cv.lecturesight.framesource.kinect;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSourceException;
import java.util.Map;
import org.pmw.tinylog.Logger;

public class KinectDepthFrameGrabberFactory implements FrameGrabberFactory {

  private Activator parent;

  public KinectDepthFrameGrabberFactory(Activator parent) {
    this.parent = parent;
  }

  @Override
  public FrameGrabber createFrameGrabber(String input, Map<String, String> config) throws FrameSourceException {

    int index = 0;
    try {
      Integer.parseInt(input);
    } catch (NumberFormatException e) {
      String msg = "Failed to parse device number";
      Logger.error(msg, e);
      throw new FrameSourceException(msg, e);
    }

    if (!parent.hasConsumerDepth(index)) {
      FrameGrabber fg = new KinectDepthFrameGrabber(index);
      parent.setConsumerDepth(index, fg);
      return fg;
    } else {
      throw new FrameSourceException("Device " + index + " is already in use!");
    }
  }

  @Override
  public void destroyFrameGrabber(FrameGrabber fg) throws FrameSourceException {
    parent.setConsumerDepth(((KinectDepthFrameGrabber) fg).devIndex, null);
  }

}
