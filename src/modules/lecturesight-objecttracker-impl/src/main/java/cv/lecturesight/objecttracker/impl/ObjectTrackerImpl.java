package cv.lecturesight.objecttracker.impl;

import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.regiontracker.RegionTracker;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.util.Set;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** ObjectDecorator Service: Head finder
 *
 */
@Component(name = "lecturesight.objecttracker.impl", immediate = true)
@Service
public class ObjectTrackerImpl implements ObjectTracker {

  private Log log = new Log("Head Finder");
  @Reference
  private Configuration config;
  @Reference
  private RegionTracker rTracker;
  private DecoratorManager dManager;

  protected void activate(ComponentContext cc) throws Exception {
    dManager = new DecoratorManager(cc);
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    log.info("Deactivated");
  }

  @Override
  public TrackerObject getObject(int id) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isCurrentlyTracked(TrackerObject object) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void discardObject(TrackerObject object) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Set<TrackerObject> getAllObjects() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Set<TrackerObject> getCurrentlyTracked() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
