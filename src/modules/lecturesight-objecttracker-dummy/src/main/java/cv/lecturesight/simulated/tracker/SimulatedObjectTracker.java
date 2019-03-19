package cv.lecturesight.simulated.tracker;

import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.conf.ConfigurationListener;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component(name = "lecturesight.simulated.tracker", immediate = true)
@Service
@Properties({
  @Property(name = "osgi.command.scope", value = "va"),
  @Property(name = "osgi.command.function", value = {"reset"})
})

public class SimulatedObjectTracker implements ObjectTracker, ConfigurationListener {

  @Reference
  Configuration config;       // configuration parameters

  private OCLSignal sig_VA_DONE = null;

  protected void activate(ComponentContext cc) {
    Logger.info("Activated.");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    Logger.info("Deactivated");
  }

  @Override
  public void configurationChanged() {
  }

  @Override
  public OCLSignal getSignal() {
    return sig_VA_DONE;
  }

  @Override
  public TrackerObject getObject(int id) {
    return null;
  }

  @Override
  public boolean isCurrentlyTracked(TrackerObject object) {
    return false;
  }

  @Override
  public void discardObject(TrackerObject object) {
  }

  @Override
  public Map<Integer, TrackerObject> getAllObjects() {
    Map m = new HashMap<Integer, TrackerObject>();
    return m;
  }

  @Override
  public List<TrackerObject> getCurrentlyTracked() {
    List l = new LinkedList<TrackerObject>();
    return l;
  }

  // Console Commands __________________________________________________________
  public void reset(String[] args) {
    Logger.info("Clearing target list.");
  }
}
