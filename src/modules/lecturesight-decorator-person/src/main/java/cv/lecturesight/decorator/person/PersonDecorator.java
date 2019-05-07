package cv.lecturesight.decorator.person;

import cv.lecturesight.decorator.api.ObjectDecorator;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.Position;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

/** ObjectDecorator Service: Head finder
 *
 */
public class PersonDecorator implements ObjectDecorator {

  //final static String PROPKEY_PERSON = "person.probability";
  final static String OBJ_PROPKEY_HEAD_CENTROID = "head.center";
  final static String OBJ_PROPKEY_CENTROID = "obj.centroid";
  final static String OBJ_PROPKEY_HEAD_RADIUS = "head.radius";
  final static String PROPKEY_PROB = "person.probability";

  @Setter
  Configuration config;

  protected void activate(ComponentContext cc) throws Exception {
    Logger.info("BrainzzZ!");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    Logger.info("Deactivated");
  }

  @Override
  public void examine(TrackerObject obj) {

    // Try to read the image
    try {
      Position gravity = (Position)obj.getProperty(OBJ_PROPKEY_CENTROID);
      Position head    = (Position)obj.getProperty(OBJ_PROPKEY_HEAD_CENTROID);

      if(head != null && gravity != null) {
        //Logger.info("Gravity: "+gravity.toString());
        //Logger.info("Head: "+head.toString());

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
      Logger.error("Error in person finder!", e);
    }
  }
}
