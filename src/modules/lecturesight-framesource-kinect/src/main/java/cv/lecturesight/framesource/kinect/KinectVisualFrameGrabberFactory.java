package cv.lecturesight.framesource.kinect;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSourceException;
import java.util.Map;
import org.pmw.tinylog.Logger;

public class KinectVisualFrameGrabberFactory implements FrameGrabberFactory {

  Activator parent;

  public KinectVisualFrameGrabberFactory(Activator parent) {
    this.parent = parent;
  }

  @Override
  public FrameGrabber createFrameGrabber(String input, Map<String, String> config) throws FrameSourceException {

    int index = -1;
    try {
      Integer.parseInt(input);
    } catch (NumberFormatException e) {
      String msg = "Failed to parse device number";
      Logger.error(msg, e);
      throw new FrameSourceException(msg, e);
    }

    FrameGrabber fg = new KinectDepthFrameGrabber(index);
    parent.setConsumerRGB(index, fg);
    return fg;
  }

  @Override
  public void destroyFrameGrabber(FrameGrabber fg) throws FrameSourceException {
    parent.setConsumerRGB(((KinectVisualFrameGrabber)fg).devIndex, null);
  }

}
