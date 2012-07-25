package cv.lecturesight.decorator.person;

import cv.lecturesight.object.ObjectDecorator;
import cv.lecturesight.object.TrackerObject;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.Position;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** ObjectDecorator Service: Head finder
 *
 */
@Component(name = "lecturesight.decorator.person", immediate = true)
@Service
@Properties({
  @Property(name = "lecturesight.decorator.name", value = "Person Finder"),
  @Property(name = "lecturesight.decorator.callon", value = "EACHFRAME"),
  @Property(name = "lecturesight.decorator.produces", value = {"person.probability"})
})
public class PersonDecorator implements ObjectDecorator {

  //final static String PROPKEY_PERSON = "person.probability";
  final static String PROPKEY_CENTROID = "head.center";
  final static String PROPKEY_RADIUS = "head.radius";
  final static String PROPKEY_PROB = "person.probability";

  private Log log = new Log("Head Finder");
  @Reference
  Configuration config;

  protected void activate(ComponentContext cc) throws Exception {
    log.info("BrainzzZ!");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    log.info("Deactivated");
  }

  @Override
  public void examine(TrackerObject obj) {

    // Try to read the image
    try {
      Position gravity = obj.getCentroid();
      Position head    = (Position)obj.getProperty(PROPKEY_CENTROID);

      double distance = Helper.euclidean_distance(gravity, head);
      double radius = (Double)(obj.getProperty(PROPKEY_RADIUS));

      double gold = 1.618;
      double prob = 0;
      if(distance != radius) {
        double ratio = distance/(distance-radius);
        if(ratio < gold*2) {
          if(ratio > gold) ratio = 2*gold-ratio;
          prob = ratio/gold;
        }
      }
      obj.setProperty(PROPKEY_PROB, prob);
      
    } catch (Exception e) {
      log.error("Error in head finder!", e);
    }
  }
}
