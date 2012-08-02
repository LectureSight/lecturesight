package cv.lecturesight.objecttracker.impl;

import cv.lecturesight.decorator.api.DecoratorManager;
import cv.lecturesight.decorator.api.DecoratorManager.CallType;
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
import cv.lecturesight.util.geometry.Position;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
  
  @Reference
  private DecoratorManager dManager;
  
  OCLSignal sig_DONE;
  private TrackerUpdate trackerUpdate;
  private Map<Integer, TrackerObject> allObjects = new TreeMap<Integer, TrackerObject>();
  private Set<TrackerObject> trackedObjects = new HashSet<TrackerObject>();
  int width_min, width_max, height_min, height_max, timeToLive;
  float matchThreshold;

  protected void activate(ComponentContext cc) throws Exception {
    updateConfiguration();
    sig_DONE = ocl.getSignal(Constants.SIGNAME_DONE);
    trackerUpdate = new TrackerUpdate();
    ocl.registerTriggerable(rTracker.getSignal(RegionTracker.Signal.DONE_CORRELATION), trackerUpdate);
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    ocl.unregisterTriggerable(rTracker.getSignal(RegionTracker.Signal.DONE_CORRELATION), trackerUpdate);
    log.info("Deactivated");
  }

  private void updateConfiguration() {
    width_min = config.getInt(Constants.PROPKEY_WIDTHMIN);
    width_max = config.getInt(Constants.PROPKEY_WIDTHMAX);
    height_min = config.getInt(Constants.PROPKEY_HEIGHTMIN);
    height_max = config.getInt(Constants.PROPKEY_HEIGHTMAX);
    timeToLive = config.getInt(Constants.PROPKEY_TTL);
    matchThreshold = config.getFloat(Constants.PROPKEY_MATCHTHRESH);
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
  public List<TrackerObject> getAllObjects() {
    List<TrackerObject> out = new LinkedList<TrackerObject>();
    out.addAll(allObjects.values());
    return out;
  }

  @Override
  public List<TrackerObject> getCurrentlyTracked() {
    List<TrackerObject> out = new LinkedList<TrackerObject>();
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
      List<Region> regions = rTracker.getRegions();

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
      Set<TrackerObject> newTrackedObjects = new HashSet<TrackerObject>();
      for (Iterator<TrackerObject> it = trackedObjects.iterator(); it.hasNext();) {
        TrackerObject object = it.next();
        Region lastRegion = (Region) object.getProperty(Constants.OBJ_PROPKEY_REGION);

        if (candidates.contains(lastRegion)) {            // is the region from last frame still tracked by RegionTracker?
          newTrackedObjects.add(                          // yes --> simply update TrackerObject from region data
                  updateTrackerObject(object, lastRegion, currentTime));
          dManager.applyDecorators(CallType.EACHFRAME, object);
          candidates.remove(lastRegion);
        } else {                                          // no --> try to find a matching region
          Region mregion = findMatchingRegion(object, regions);
          if (mregion != null) {
            newTrackedObjects.add(
                    updateTrackerObject(object, mregion, currentTime));
            dManager.applyDecorators(CallType.EACHFRAME, object);
            candidates.remove(mregion);
          } else {                                         // didn't find matching region --> remove object from tracking if TTL is up
            if (currentTime - object.lastSeen() < timeToLive) {
              newTrackedObjects.add(object);
            }
          }
        }
      }
      
      // for all remaining candidates: create new TrackerObjects
      for (Iterator<Region> it = candidates.iterator(); it.hasNext(); ) {
        newTrackedObjects.add(
                createTrackerObject(it.next(), currentTime));
      }

      trackedObjects = newTrackedObjects;
      ocl.castSignal(sig_DONE);
    }

    private Region findMatchingRegion(TrackerObject object, List<Region> regions) {
      Region result = null;
      //BoundingBox bbox = (BoundingBox)object.getProperty(Constants.OBJ_PROPKEY_BBOX);
      Position centroid = (Position)object.getProperty(Constants.OBJ_PROPKEY_CENTROID);
      double error = Double.MAX_VALUE;
      for (Iterator<Region> it = regions.iterator(); it.hasNext(); ) {
        Region region = it.next();
        double err = centroid.distance(region.getCentroid());
        //System.out.println("Error = " + err);
        if (err <= matchThreshold) {
          if (err < error) {
            error = err;
            result = region;
          }
        }
      }
      return result;
    }

    private TrackerObject createTrackerObject(Region region, long time) {
      TrackerObject object = new TrackerObject(time);
      object.setProperty(Constants.OBJ_PROPKEY_REGION, region);
      object.setProperty(Constants.OBJ_PROPKEY_CENTROID, region.getCentroid().clone());
      object.setProperty(Constants.OBJ_PROPKEY_BBOX, region.getBoundingBox().clone());
      object.setProperty(Constants.OBJ_PROPKEY_WEIGHT, region.getWeight());
      return object;
    }

    private TrackerObject updateTrackerObject(TrackerObject object, Region region, long time) {
      object.setLastSeen(time);
      object.setProperty(Constants.OBJ_PROPKEY_REGION, region);
      object.setProperty(Constants.OBJ_PROPKEY_CENTROID, region.getCentroid().clone());
      object.setProperty(Constants.OBJ_PROPKEY_BBOX, region.getBoundingBox().clone());
      object.setProperty(Constants.OBJ_PROPKEY_WEIGHT, region.getWeight());
      return object;
    }
  }
}
