package cv.lecturesight.objecttracker.impl;

import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.Triggerable;
import cv.lecturesight.regiontracker.Region;
import cv.lecturesight.regiontracker.RegionTracker;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.BoundingBox;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Object Tracker Service
 *
 */
@Component(name = "lecturesight.objecttracker.simpel", immediate = true)
@Service
public class ObjectTrackerImpl implements ObjectTracker {
  
  private Log log = new Log("Object Tracker");
  
  @Reference
  private Configuration config;
  
  @Reference
  OpenCLService ocl;
  
  @Reference
  private RegionTracker rTracker;
  
  OCLSignal sig_DONE;
  //private DecoratorManager dManager;
  private TrackerUpdate trackerUpdate;
  private Map<Integer,TrackerObject> allObjects = new TreeMap<Integer,TrackerObject>();
  private Set<TrackerObject> trackedObjects = new TreeSet<TrackerObject>();
  int width_min, width_max, height_min, height_max, timeToLive;

  protected void activate(ComponentContext cc) throws Exception {
    updateConfiguration();
    //dManager = new DecoratorManager(cc);
    sig_DONE = ocl.getSignal(Constants.SIGNAME_DONE);
    trackerUpdate = new TrackerUpdate();
    ocl.registerTriggerable(rTracker.getSignal(RegionTracker.Signal.DONE_CORRELATION), trackerUpdate);
    log.info("Activated");
  }
  
  protected void deactivate(ComponentContext cc) throws Exception {
    ocl.unregisterTriggerable(sig_DONE, trackerUpdate);
    log.info("Deactivated");
  }

  private void updateConfiguration() {
    width_min = config.getInt(Constants.PROPKEY_WIDTHMIN);
    width_max = config.getInt(Constants.PROPKEY_WIDTHMAX);
    height_min = config.getInt(Constants.PROPKEY_HEIGHTMIN);
    height_max = config.getInt(Constants.PROPKEY_HEIGHTMAX);
    timeToLive = config.getInt(Constants.PROPKEY_TTL);
  }
  
  //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
  @Override
  public OCLSignal getSignal() {
    return sig_DONE;
  }

  @Override
  public TrackerObject getObject(int id) {
    return allObjects.get(id);
  }

  @Override
  public boolean isCurrentlyTracked(TrackerObject object) {
    return trackedObjects.contains(object);
  }

  @Override
  public void discardObject(TrackerObject object) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Set<TrackerObject> getAllObjects() {
    Set<TrackerObject> out = new TreeSet<TrackerObject>();
    out.addAll(allObjects.values());
    return out;
  }

  @Override
  public Set<TrackerObject> getCurrentlyTracked() {
    Set<TrackerObject> out = new TreeSet<TrackerObject>();
    out.addAll(trackedObjects);
    return out;
  }
  //</editor-fold>
  
  /** This Triggerable is fired every time the RegionTracker signals that it
   *  has finished its work.
   * 
   *  This is where the magic happens...
   */
  private class TrackerUpdate implements Triggerable {

    @Override
    public void triggered(OCLSignal ocls) {
      // get operational parameters from config each round so params can be changed in runtime
      updateConfiguration();           
      
      long currentTime = System.currentTimeMillis();
      
      // first get list of current foreground regions
      Set<Region> regions = rTracker.getRegions();
      
      // find candidate regions by their size
      List<Region> candidates = new LinkedList<Region>();
      for (Iterator<Region> it = regions.iterator(); it.hasNext();) {
        Region region = it.next();
        BoundingBox bbox = region.getBoundingBox();
        if (bbox.getWidth() >= width_min
                && bbox.getWidth() <= width_max
                && bbox.getHeight() >= height_min
                && bbox.getHeight() <= height_max) {
          candidates.add(region);
        }
      }
      
      // for each already tracked object: try to find correlating region
      for (Iterator<TrackerObject> it = trackedObjects.iterator(); it.hasNext();) {
        TrackerObject object = it.next();
        Region lastRegion = (Region)object.getProperty(Constants.OBJ_PROPKEY_REGION);
        if (candidates.contains(lastRegion)) {
          object.setLastSeen(currentTime);
        } else {
          // matching region? 
          // update region on trackerobject
          // update timestamp
          //   else
          // over ttl? --> remove from tracking
        }
      }
      
      ocl.castSignal(sig_DONE);
    }
  }
}
