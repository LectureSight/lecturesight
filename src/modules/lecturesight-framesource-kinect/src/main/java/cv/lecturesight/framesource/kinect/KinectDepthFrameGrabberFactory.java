package cv.lecturesight.framesource.kinect;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSourceException;
import java.util.Map;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

@Component(name="lecturesight.framesource.kinect.depth", immediate=true)
@Service()
@Properties({
  @Property(name="cv.lecturesight.framesource.name", value="Kinect Depth Sensor"),
  @Property(name="cv.lecturesight.framesource.type", value="kinect-depth")  
})
public class KinectDepthFrameGrabberFactory implements FrameGrabberFactory {

  @Override
  public FrameGrabber createFrameGrabber(String input, Map<String, String> config) throws FrameSourceException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
