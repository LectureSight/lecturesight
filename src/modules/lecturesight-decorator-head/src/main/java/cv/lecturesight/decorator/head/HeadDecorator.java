package cv.lecturesight.decorator.head;

import cv.lecturesight.object.ObjectDecorator;
import cv.lecturesight.object.TrackerObject;
import cv.lecturesight.util.Log;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 */
@Component(name = "lecturesight.decorator.head", immediate = true)
@Service
@Properties({
  @Property(name = "lecturesight.decorator.name", value = "Head Finder"),
  @Property(name = "lecturesight.decorator.callon", value = "EACHFRAME"),
  @Property(name = "lecturesight.decorator.produces", value = {"head.center", "head.boundingbox"})
})
public class HeadDecorator implements ObjectDecorator {
  
  Log log = new Log("Head Finder");

  protected void activate(ComponentContext cc) throws Exception {
    log.info("Activated");
  }
  
  protected void deactivate(ComponentContext cc) throws Exception {
    log.info("Deactivated");
  }

  @Override
  public void examine(TrackerObject obj) {

  }
}
