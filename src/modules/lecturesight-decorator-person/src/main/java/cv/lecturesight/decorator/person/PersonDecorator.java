package cv.lecturesight.decorator.person;

import cv.lecturesight.decorator.api.ObjectDecorator;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
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
  final static String OBJ_PROPKEY_HEAD_CENTROID = "head.center";
  final static String OBJ_PROPKEY_CENTROID = "obj.centroid";
  final static String OBJ_PROPKEY_HEAD_RADIUS = "head.radius";
  final static String PROPKEY_PROB = "person.probability";

  private Log log = new Log("Person Finder");
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
      Position gravity = (Position)obj.getProperty(OBJ_PROPKEY_CENTROID);
      Position head    = (Position)obj.getProperty(OBJ_PROPKEY_HEAD_CENTROID);
      
      if(head != null && gravity != null) {
        //log.info("Gravity: "+gravity.toString());
        //log.info("Head: "+head.toString());

        double distance = Helper.euclidean_distance(gravity, head);
        double radius = (Double)(obj.getProperty(OBJ_PROPKEY_HEAD_RADIUS));

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
      }
    } catch (Exception e) {
      log.error("Error in person finder!", e);
    }
  }
}
