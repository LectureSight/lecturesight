/* Copyright (C) 2012 Benjamin Wulff
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package cv.lecturesight.objecttracker.clustering;

import cv.lecturesight.decorator.api.DecoratorManager;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.Triggerable;
import cv.lecturesight.regiontracker.Region;
import cv.lecturesight.regiontracker.RegionTracker;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.conf.ConfigurationListener;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

/**
 * Object Tracker Service
 *
 */
public class ObjectTrackerImpl implements ObjectTracker, ConfigurationListener {

  @Setter
  private Configuration config;
  @Setter
  OpenCLService ocl;
  @Setter
  private RegionTracker rTracker;
  @Setter
  private DecoratorManager dManager;
  @Setter
  private ForegroundService fgs;
  @Setter
  private FrameSourceProvider fsp;
  private OCLSignal sig_DONE;
  private TrackerUpdate trackerUpdate;
  private Map<Integer, TrackerObject> allObjects = new TreeMap<Integer, TrackerObject>();
  private Map<Region, TrackerObject> regionAssignments = new HashMap<Region, TrackerObject>();
  private Map<Region, TrackerObject> newAssignments;
  private List<TrackerObject> trackedObjects = new LinkedList<TrackerObject>();
  private int template_width, width_max, template_height, height_max, weight_min;
  private double dist_max;

  protected void activate(ComponentContext cc) throws Exception {
    updateConfiguration();
    sig_DONE = ocl.getSignal(Constants.SIGNAME_DONE);
    trackerUpdate = new TrackerUpdate();
    ocl.registerTriggerable(rTracker.getSignal(RegionTracker.Signal.DONE_CORRELATION), trackerUpdate);
    Logger.info("Activated");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    ocl.unregisterTriggerable(rTracker.getSignal(RegionTracker.Signal.DONE_CORRELATION), trackerUpdate);
    Logger.info("Deactivated");
  }

  private void updateConfiguration() {
    template_width = config.getInt(Constants.PROPKEY_TEMPWIDTH);
    template_height = config.getInt(Constants.PROPKEY_TEMPHEIGHT);
    width_max = config.getInt(Constants.PROPKEY_WIDTHMAX);
    height_max = config.getInt(Constants.PROPKEY_HEIGHTMAX);
    weight_min = config.getInt(Constants.PROPKEY_WEIGHTMIN);
    dist_max = config.getInt(Constants.PROPKEY_DISTMAX);
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
  public Map<Integer, TrackerObject> getAllObjects() {
    return allObjects;
  }

  @Override
  public List<TrackerObject> getCurrentlyTracked() {
    return trackedObjects;
  }
  
  @Override
  public void configurationChanged() {
    updateConfiguration();
  }
  //</editor-fold>

  private TrackerObject createTrackerObject(Region region, long time) {
    TrackerObject object = new TrackerObject(time);
    object.setProperty(OBJ_PROPKEY_REGION, region);
    object.setProperty(OBJ_PROPKEY_CENTROID, region.getCentroid().clone());
    object.setProperty(OBJ_PROPKEY_WEIGHT, region.getWeight());
    object.setProperty(OBJ_PROPKEY_MOVEMENT, 0);
    
    // make bounding box
    BoundingBox rBox = region.getBoundingBox();
    object.setProperty(OBJ_PROPKEY_BBOX, rBox);
    
    allObjects.put(object.getId(), object);
    return object;
  }

  private void updateTrackerObject(TrackerObject object, Region region, long time) {
    Position lastPos = (Position) object.getProperty(OBJ_PROPKEY_CENTROID);
    
    object.setLastSeen(time);
    object.setProperty(OBJ_PROPKEY_REGION, region);
    object.setProperty(OBJ_PROPKEY_CENTROID, region.getCentroid());
    object.setProperty(OBJ_PROPKEY_WEIGHT, region.getWeight());
    
    // update bounding box
    BoundingBox rBox = region.getBoundingBox();
    object.setProperty(OBJ_PROPKEY_BBOX, rBox);

    double distance = lastPos.distance(region.getCentroid());
    if (distance < width_max / 5 && distance > 0) {
      int movement = (Integer) object.getProperty(OBJ_PROPKEY_MOVEMENT);
      object.setProperty(OBJ_PROPKEY_MOVEMENT, ++movement);
    }
  }

  /**
   * This Triggerable is fired every time the RegionTracker signals that it has
   * finished its work.
   *
   */
  private class TrackerUpdate implements Triggerable {
    
    final static int CONFIG_REFRESH_ITERATIONS = 10;
    private int lastUpdated = 0;

    // TODO call Decorators
    @Override
    public void triggered(OCLSignal signal) {
      if (++lastUpdated > CONFIG_REFRESH_ITERATIONS) {
        updateConfiguration();
        lastUpdated = 0;
      }
      long time = System.currentTimeMillis();
      
      newAssignments = new HashMap<Region, TrackerObject>();
      List<Region> regions = filterRegions(rTracker.getRegions());

      // 1. filter out too big regions
      // 2. for all objects: go through regions and add fitting regions / remove regions that don't fit anymore
      // 3. for all remaining regions: try to create object on / fist look if object from past can be revived
      
      for (Region region : regions) {
        TrackerObject object = regionAssignments.get(region);
        if (object == null) {
          
          if (region.getWeight() >= weight_min) {
            object = findMatchingObject(region);
            if (object != null) {
              updateTrackerObject(object, region, time);
              newAssignments.put(region, object);
            } else {
              if (!inTrackedObjects(region)) {
                object = createTrackerObject(region, time);
                newAssignments.put(region, object);
              } 
            }
          }
          
        } else {
          updateTrackerObject(object, region, time);
          if ((Integer)object.getProperty(OBJ_PROPKEY_MOVEMENT) > 20) {
            rTracker.strengthenRegion(region);
          }
          newAssignments.put(region, object);
        }
      }
      regionAssignments = newAssignments;
      
      trackedObjects = new LinkedList<TrackerObject>();
      trackedObjects.addAll(regionAssignments.values());
      
      for (TrackerObject obj : trackedObjects) {
        dManager.applyDecorators(DecoratorManager.CallType.EACHFRAME, obj);
      }
      
      ocl.castSignal(sig_DONE);
    }

    private List<Region> filterRegions(List<Region> regions) {
      LinkedList<Region> out = new LinkedList<Region>();
      for (Region r : regions) {
        BoundingBox box = r.getBoundingBox();
        if (box.getWidth() <= width_max && box.getHeight() <= height_max) {
          out.add(r);
        } else {
          rTracker.discardRegion(r);
        }
      }
      return out;
    }
    
    private TrackerObject findMatchingObject(Region region) {
      double winner_dist = dist_max;
      TrackerObject winner = null;
      for (int i = allObjects.size() - 1; i > 0; i--) {
        TrackerObject obj = allObjects.get(i);
        if (!trackedObjects.contains(obj)) {
          BoundingBox oBox = (BoundingBox)obj.getProperty(OBJ_PROPKEY_BBOX);
          Position oPos = new Position(oBox.getMin().getX() + oBox.getWidth()/2,
                  oBox.getMin().getY() + oBox.getHeight()/2);
          double distance = region.getCentroid().distance(oPos);
          if (distance < winner_dist) {
            winner = obj;
          }
        }
      }
      return winner;
    }
    
    private boolean inTrackedObjects(Region region) {
      Position pos = region.getCentroid();
      for (TrackerObject obj : newAssignments.values()) {
        BoundingBox box = (BoundingBox)obj.getProperty(OBJ_PROPKEY_BBOX);
        if (pos.getX() >= box.getMin().getX() && pos.getY() >= box.getMin().getY() &&
                pos.getX() <= box.getMax().getX() && pos.getY() <= box.getMax().getY()) {
          return true;
        }
      }
      return false;
    }
  }
}
