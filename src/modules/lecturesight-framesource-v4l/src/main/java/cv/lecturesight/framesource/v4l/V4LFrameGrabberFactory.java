package cv.lecturesight.framesource.v4l;

import au.edu.jcu.v4l4j.DeviceInfo;
import au.edu.jcu.v4l4j.ImageFormat;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.util.Map;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 */
@Component(name="lecturesight.framesource.v4l", immediate=true)
@Service()
@Properties({
@Property(name="cv.lecturesight.framesource.name", value="Video4Linux"),
@Property(name="cv.lecturesight.framesource.type", value="v4l")  
})
public class V4LFrameGrabberFactory implements FrameGrabberFactory {

  private final static String PROPKEY_FRAME_WIDTH = "resolution.width";
  private final static String PROPKEY_FRAME_HEIGHT = "resolution.height";
  private final static String PROPKEY_STANDARD = "standard";
  private final static String PROPKEY_CHANNEL = "channel";
  private final static String PROPKEY_QUALITY = "quality";
  private Log log = new Log("Video4Linux FrameSource");
  @Reference
  private Configuration config;

  protected void activate(ComponentContext cc) {
    System.loadLibrary("libvideo.so.0");
  }

  @Override
  public FrameGrabber createFrameGrabber(String input, Map<String, String> conf) throws FrameSourceException {
    VideoDevice device = initVideoDevice(input);
    log.info(generateDeviceInfo(device));

    int width = conf.containsKey("width") ? Integer.parseInt(conf.get("width")) : config.getInt(PROPKEY_FRAME_WIDTH);
    int height = conf.containsKey("height") ? Integer.parseInt(conf.get("height")) : config.getInt(PROPKEY_FRAME_HEIGHT);
    int videoStandard = conf.containsKey("standard") ? Integer.parseInt(conf.get("standard")) : config.getInt(PROPKEY_STANDARD);
    int videoChannel = conf.containsKey("channel") ? Integer.parseInt(conf.get("channel")) : config.getInt(PROPKEY_CHANNEL);
    int videoQuality = conf.containsKey("quality") ? Integer.parseInt(conf.get("quality")) : config.getInt(PROPKEY_QUALITY);
    return new V4LFrameGrabber(device, width, height, videoStandard, videoChannel, videoQuality);
  }

  private VideoDevice initVideoDevice(String name) throws FrameSourceException {
    try {
      log.info("Opening capture device " + name);
      VideoDevice device = new VideoDevice(name);
      log.info("Device name: " + device.getDeviceInfo().getName());
      if (device == null) {
        throw new FrameSourceException("Could not open capture device: " + name);
      }
      return device;
    } catch (V4L4JException ex) {
      throw new FrameSourceException("Could not open capture device " + name + ": " + ex.getMessage());
    }
  }

  private String generateDeviceInfo(VideoDevice device) throws FrameSourceException {
    try {
      StringBuilder out = new StringBuilder();
      out.append("Device supports formats: ");
      DeviceInfo info = device.getDeviceInfo();
      for (ImageFormat f : info.getFormatList().getNativeFormats()) {
        out.append(f.toNiceString());
        out.append(" ");
      }
      return out.toString();
    } catch (V4L4JException ex) {
      throw new FrameSourceException("Could not get device information: " + ex.getMessage());
    }
  }
}
