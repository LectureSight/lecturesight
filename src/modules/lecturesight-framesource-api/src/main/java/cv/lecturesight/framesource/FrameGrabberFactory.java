package cv.lecturesight.framesource;

import java.util.Map;

public interface FrameGrabberFactory {

  FrameGrabber createFrameGrabber(String input, Map<String, String> config) throws FrameSourceException;
}
