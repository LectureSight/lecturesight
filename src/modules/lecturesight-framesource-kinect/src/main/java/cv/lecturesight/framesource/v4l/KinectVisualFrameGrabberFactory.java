package cv.lecturesight.framesource.v4l;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSourceException;
import java.util.Map;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

@Component(name="lecturesight.framesource.v4l", immediate=true)
@Service()
@Properties({
  @Property(name="cv.lecturesight.framesource.name", value="Kinect QVGA sensor"),
  @Property(name="cv.lecturesight.framesource.type", value="kinect-visual")  
})
public class KinectVisualFrameGrabberFactory implements FrameGrabberFactory {

  @Override
  public FrameGrabber createFrameGrabber(String input, Map<String, String> config) throws FrameSourceException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
