package cv.lecturesight.decorator.color;

import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.decorator.api.ObjectDecorator;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** ObjectDecorator Service: Head finder
 *
 */
@Component(name = "lecturesight.decorator.color", immediate = true)
@Service
@Properties({
  @Property(name = "lecturesight.decorator.name", value = "Color Decorator"),
  @Property(name = "lecturesight.decorator.callon", value = "EACHFRAME"),
  @Property(name = "lecturesight.decorator.produces", value = {"color.histogram"})
})
public class ColorDecorator implements ObjectDecorator {

  final static String OBJ_PROPKEY_COLOR_HISTOGRAM = "color.histogram";
  private Log log = new Log("Color Decorator");
  @Reference
  Configuration config;
  @Reference
  ForegroundService fgs;
  @Reference
  private FrameSourceProvider fsp;
  private int width_min, width_max, height_min, height_max;

  protected void activate(ComponentContext cc) throws Exception {
    log.info("BrainzzZ!");
    width_min = config.getInt("width.min");
    width_max = config.getInt("width.max");
    height_min = config.getInt("height.min");
    height_max = config.getInt("height.max");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    log.info("Deactivated");
  }

  @Override
  public void examine(TrackerObject object) {
    // Try to read the image
    try {
      BoundingBox bbox = (BoundingBox) object.getProperty(ObjectTracker.OBJ_PROPKEY_BBOX);

      BufferedImage sil = fgs.getForegroundMapHost();
      
      FrameSource fsrc = fsp.getFrameSource();
      BufferedImage scene = fsrc.getImageHost();
      WritableRaster img = sil.getRaster();
      WritableRaster imgc = scene.getRaster();
      
      ColorHistogram ch = (ColorHistogram) object.getProperty(OBJ_PROPKEY_COLOR_HISTOGRAM);
      if(ch == null) {
        object.setProperty(OBJ_PROPKEY_COLOR_HISTOGRAM, 
              new ColorHistogram(img, imgc, bbox, 256));
      }
      else {
        object.setProperty(OBJ_PROPKEY_COLOR_HISTOGRAM, new ColorHistogram(img, imgc, bbox, 256, ch));
      }
    } catch (Exception e) {
        log.error("Error in color decorator!", e);
    }
  }
}
