package cv.lecturesight.objecttracker.impl;

import cv.lecturesight.decorator.api.DecoratorManager;
import cv.lecturesight.decorator.api.DecoratorManager.CallType;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import cv.lecturesight.decorator.color.ColorHistogram;
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
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
  private List<TrackerObject> trackedObjects = new LinkedList<TrackerObject>();
//  private List<Region> regions = new LinkedList<Region>();
  int width_min, width_max, height_min, height_max, timeToLive;
  float matchThreshold, chThreshold, distanceThreshold;
  private List<Color> color_list = new LinkedList<Color>();
  final static String OBJ_PROPKEY_COLOR_HISTOGRAM = "color.histogram";
  final static String OBJ_PROPKEY_BW_PIXELS = "obj.bw_pixels";

  @Reference
  ForegroundService fgs;
  @Reference
  private FrameSourceProvider fsp;

  protected void activate(ComponentContext cc) throws Exception {
    updateConfiguration();
    sig_DONE = ocl.getSignal(Constants.SIGNAME_DONE);
    trackerUpdate = new TrackerUpdate();
    ocl.registerTriggerable(rTracker.getSignal(RegionTracker.Signal.DONE_CORRELATION), trackerUpdate);
    log.info("Activated");
    color_list.add(Color.BLUE);
    color_list.add(Color.CYAN);
    color_list.add(Color.GREEN);
    color_list.add(Color.MAGENTA);
    color_list.add(Color.ORANGE);
    color_list.add(Color.PINK);
    color_list.add(Color.RED);
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
    chThreshold = config.getFloat(Constants.PROPKEY_CHTHRESHOLD);
    matchThreshold = config.getFloat(Constants.PROPKEY_MATCHTHRESH);
    distanceThreshold = config.getFloat(Constants.PROPKEY_DISTANCETHRESH);
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
 //   List<TrackerObject> out = new LinkedList<TrackerObject>();
 //   out.addAll(allObjects.values());
 //   return out;
    return allObjects;
  }

  @Override
  public List<TrackerObject> getCurrentlyTracked() {
//    List<TrackerObject> out = new LinkedList<TrackerObject>();
//    for(Iterator<TrackerObject>it = trackedObjects.iterator(); it.hasNext();) {
//      TrackerObject object = it.next();
//      if(object.lastSeen() - System.currentTimeMillis() < 6000) {
//        out.add(object);
//      }
//    }
//    return out;
    return trackedObjects;
  }
  //</editor-fold>

  /** This Triggerable is fired every time the RegionTracker signals that it
   *  has finished its work.
   * 
   *  This is where the magic happens...
   */
  private class TrackerUpdate implements Triggerable {

    long currentTime;
    WritableRaster img;
    WritableRaster imgc;
    List<Region> regions;
    
    @Override
    public void triggered(OCLSignal ocls) {
      // get operational parameters from config each round so params can be changed in runtime
      updateConfiguration();
          
      BufferedImage sil = fgs.getForegroundMapHost();
      FrameSource fsrc = fsp.getFrameSource();
      BufferedImage scene = fsrc.getImageHost();
      img = sil.getRaster();
      imgc = scene.getRaster();

      currentTime = System.currentTimeMillis();

      // first get list of current foreground regions
      List<Region> regions1 = rTracker.getRegions();
      
      // find candidate regions by their size
      List<Region> candidates = new LinkedList<Region>();
      regions = new LinkedList<Region>();
      
      for (Region region : regions1) {
        BoundingBox bbox = region.getBoundingBox();
        if (bbox.getWidth() >= width_min
                && bbox.getWidth() <= width_max
                && bbox.getHeight() >= height_min
                && bbox.getHeight() <= height_max
                && (currentTime - region.getLastMoveTime() < timeToLive/2)) {
          candidates.add(region);
        }
        else {
          if(bbox.getWidth() <= width_max && bbox.getHeight() <= height_max
                && bbox.getWidth() > width_min/5 && bbox.getHeight() > height_min/5
                && (currentTime - region.getLastMoveTime() < timeToLive/2)) {
            regions.add(region);
          }
        }
      }
      
      List<TrackerObject> trackedObjects1 = new LinkedList<TrackerObject>();
      for(TrackerObject obj : trackedObjects) {
        if(currentTime - obj.lastSeen() < timeToLive)
          trackedObjects1.add(obj);
      }

      List<TrackerObject> newTrackedObjects = assign_new(trackedObjects1, candidates);
      trackedObjects = newTrackedObjects;
      
      ocl.castSignal(sig_DONE);
    }

    private Region findMatchingRegion(TrackerObject object, List<Region> regions) {
      Region result = null;
      //BoundingBox bbox = (BoundingBox)object.getProperty(Constants.OBJ_PROPKEY_BBOX);
      Position centroid = (Position)object.getProperty(OBJ_PROPKEY_CENTROID);
      double error = Double.MAX_VALUE;
      for (Region region : regions) {
        BoundingBox bbox = region.getBoundingBox();
        double err = centroid.distance(region.getCentroid());
        //System.out.println("Error = " + err);
        if (err <= matchThreshold && bbox.getWidth() >= width_min/3
                && bbox.getWidth() <= width_max
                && bbox.getHeight() >= height_min/3
                && bbox.getHeight() <= height_min) {
          if (err < error) {
            error = err;
            result = region;
          }
        }
      }
      return result;
    }
    
    private Region findMatchingRegion(Region region, List<Region> regions) {
      Region result = null;
      Position centroid = region.getCentroid();
      double error = Double.MAX_VALUE;
      for (Region r1 : regions) {
        double err = centroid.distance(r1.getCentroid());
        if(err <= matchThreshold) {
          if(err < error) {
            error = err;
            result = r1;
          }
        }
      }
      return result;
    }

    private TrackerObject matchColorHistogram(Region r, float threshold, long currentTime) {
      // we set the actual element to the element to which we have to compare the
      // rest. This is to assure, that match does not fail if called with an empty
      // list.
      if(allObjects.isEmpty()) return null;
      
      double min_distance = 1.0;
      TrackerObject result = null;
      ColorHistogram ch = new ColorHistogram(img, imgc, r.getBoundingBox(), 256);
      
      for(TrackerObject act : allObjects.values()) {
        if(act.lastSeen() - currentTime != 0) {
          try {
            ColorHistogram ch1 = (ColorHistogram) act.getProperty(OBJ_PROPKEY_COLOR_HISTOGRAM);
            double distance = ch.bhattacharya_distance(ch1);
            if(distance < min_distance && distance < threshold) {
              min_distance = distance;
              result = act;
            }
          } catch (Exception ex) {
            Logger.getLogger(ObjectTrackerImpl.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }
      return result;
    }

    private TrackerObject createTrackerObject(Region region, long time) {
      TrackerObject object = new TrackerObject(time);
      object.setProperty(OBJ_PROPKEY_REGION, region);
      object.setProperty(OBJ_PROPKEY_CENTROID, region.getCentroid().clone());
      object.setProperty(OBJ_PROPKEY_BBOX, region.getBoundingBox().clone());
      object.setProperty(OBJ_PROPKEY_WEIGHT, region.getWeight());
      allObjects.put(object.getId(), object);
      dManager.applyDecorators(CallType.EACHFRAME, object);
      Color c = Color.yellow;
      if(color_list.size() > 0)
        c = color_list.remove(color_list.size()-1);
      object.setProperty(OBJ_PROPKEY_COLOR, c);
      return object;
    }

    private TrackerObject updateTrackerObject(TrackerObject object, Region region, long time) {
      object.setLastSeen(time);
      object.setProperty(OBJ_PROPKEY_REGION, region);
      object.setProperty(OBJ_PROPKEY_CENTROID, region.getCentroid().clone());
      BoundingBox bbox = region.getBoundingBox().clone();
      object.setProperty(OBJ_PROPKEY_BBOX, bbox);
      object.setProperty(OBJ_PROPKEY_WEIGHT, region.getWeight());
      if(!checkSplitter(region) && bbox.getWidth() >= width_min
                && bbox.getWidth() <= width_max
                && bbox.getHeight() >= height_min
                && bbox.getHeight() <= height_max) {
        dManager.applyDecorators(CallType.EACHFRAME, object);
      }
      return object;
    }
    
    private TrackerObject moveTrackerObject(TrackerObject object, Position pos, long time) {
      Position centroid = ((Position) object.getProperty(OBJ_PROPKEY_CENTROID)).clone();
      double d_x = -(centroid.getX()-pos.getX());
      double d_y = -(centroid.getY()-pos.getY());
      BoundingBox bbox = ((BoundingBox) object.getProperty(OBJ_PROPKEY_BBOX)).clone();
        if(Math.abs(d_x-d_y) < 1) {
          bbox.setMin(new Position((int)(bbox.getMin().getX()+d_x),(int)(bbox.getMin().getY()+d_y)));
          bbox.setMax(new Position((int)(bbox.getMax().getX()+d_x),(int)(bbox.getMax().getY()+d_y)));
          centroid.setX((int)(centroid.getX()+d_x));
          centroid.setY((int)(centroid.getY()+d_y));
        }
        else {
          if(d_x > d_y) {
            bbox.setMin(new Position((int)(bbox.getMin().getX()+d_x),bbox.getMin().getY()));
            bbox.setMax(new Position((int)(bbox.getMax().getX()+d_x),bbox.getMax().getY()));
            centroid.setX((int)(centroid.getX()+d_x));
          }
          if(d_y > d_x) {
            bbox.setMin(new Position(bbox.getMin().getX(),(int) (bbox.getMin().getY()+d_y)));
            bbox.setMax(new Position(bbox.getMax().getX(),(int) (bbox.getMax().getY()+d_y)));
            centroid.setY((int)(centroid.getY()+d_y));
          }
        }
      
      object.setLastSeen(time);
      object.setProperty(OBJ_PROPKEY_CENTROID, centroid);
      object.setProperty(OBJ_PROPKEY_BBOX, bbox);
      return object;
    }

    private boolean checkSplitter(Region lastRegion) {
      if(lastRegion.isSplitter()) {
        Set<Region> regions = lastRegion.getGroupMembers();
        for(Region r : regions) {
          BoundingBox bbox = r.getBoundingBox();
          if(bbox.getWidth()     >= width_min
             && bbox.getWidth()  <= width_max
             && bbox.getHeight() >= height_min
             && bbox.getHeight() <= height_max)
            return true;
        }
      }
      return false;
    }

    /**
     * Recursively assigns a list of candidate regions to a list of
     * TrackerObjects
     *
     * @param List<Region> candidates
     * @param List<TrackerObject> trackedObjects
     *
     * @return List<TrackerObject> the assigned new list
     */
    private List<TrackerObject> assign(List<Region> candidates, List<TrackerObject> trackedObjects) {
      // Abbruchbedingung
      if(candidates.isEmpty() && trackedObjects.isEmpty()) {
        return new LinkedList<TrackerObject>();
      }
      if(candidates.size() > trackedObjects.size()) {
        // we have to create one object!
        double max_distance = 0;
        Region winner = null;
        for(Region r : candidates) {
          double min_distance = Double.MAX_VALUE;
          for(TrackerObject obj : trackedObjects) {
            double distance = distance(obj, r);
            //log.info("Mehr Kandidaten als Objekte, Distanz von "+obj.getId()+" zu Region "+r.getLabel()+" = "+distance);
            if(distance < min_distance) {
              min_distance = distance;
            }
          }
          if(min_distance > max_distance) {
            winner = r;
            max_distance = min_distance;
          }
        }
        // it seems, that our winner region is not part of the actual scene.
        // we therefore search through the list of Objects, which where tracked up to now
        TrackerObject result = matchColorHistogram(winner, chThreshold, currentTime);
        if(result != null) {
          // we have a result!
          updateTrackerObject(result, winner, currentTime);
        }
        else {
          // we have no result, lets add a new TrackerObject
          result = createTrackerObject(winner, currentTime);
        }
        candidates.remove(winner);
        List<TrackerObject> n_list = assign(candidates, trackedObjects);
        n_list.add(result);
        return n_list;
      }
      else {
        if(candidates.size() < trackedObjects.size()) {
//          double min_distance = Double.MAX_VALUE;
//          TrackerObject winner = null;
//          Region r_winner = null;
//          for(TrackerObject obj : trackedObjects) {
//            for(Region r : regions) {
//              BoundingBox bbox1 = r.getBoundingBox();
//              double distance = distance(obj, r);
//              if(distance < min_distance &&
//              bbox1.getWidth() >= width_min/3 && 
//              bbox1.getWidth() <= width_max &&
//              bbox1.getHeight() >= height_min/3 &&
//              bbox1.getHeight() <= height_max) {
//                min_distance = distance;
//                winner = obj;
//                r_winner = r;
//              }
//            }
//          }
//          if(winner != null && r_winner != null && min_distance < 5.0){
//            trackedObjects.remove(winner);
//            regions.remove(r_winner);
//            moveTrackerObject(winner, r_winner.getCentroid(), currentTime);
//            List<TrackerObject> n_list = assign(candidates, trackedObjects);
//            n_list.add(winner);
//            return n_list;
//          }
////          else {
////            // there are two possibilities:
////            // 1.: we find a region, which is likely to be the object in question
////            double min_d1 = Double.MAX_VALUE;
////            Region n_region = null;
////            TrackerObject b_object = null;
////            for(TrackerObject obj : trackedObjects) {
////              double min_d2 = Double.MAX_VALUE;
////              Region b_region = null;
////              for(Region r : regions) {
////                double dist = distance(obj, r);
////                BoundingBox bbox = r.getBoundingBox();
////                if(dist < min_d2 && dist < 2.0 &&
////                 bbox.getWidth() >= width_min/3 && 
////                 bbox.getWidth() <= width_max &&
////                 bbox.getHeight() >= height_min/3 &&
////                 bbox.getHeight() <= height_max) {
////                  b_region = r;
////                  min_d2 = dist;
////                }
////              }
////              if(min_d2 < min_d1) {
////                b_object = obj;
////                n_region = b_region;
////              }
////            }trackedObjects
////            // then let's use this one!
////            if(b_object != null) {
////              updateTrackerObject(b_object, n_region, currentTime);
////              trackedObjects.remove(b_object);
////              regions.remove(n_region);
////              List<TrackerObject> n_list = assign(candidates, trackedObjects);
////            }
//            // 2.: we don't and won't move the object
//            else {
//              if(candidates.isEmpty())
//                return trackedObjects;
//              else {
//                double max_distance = 0;
//                TrackerObject loser = null;
//                for(TrackerObject obj : trackedObjects) {
//                  double m_dist = Double.MAX_VALUE;
//                  for(Region r : candidates) {
//                    double distance = distance(obj, r);
//                    if(distance < m_dist)
//                      m_dist = distance;
//                  }
//                  if(m_dist > max_distance) {
//                    loser = obj;
//                    max_distance = m_dist;
//                  }
//                }
//                trackedObjects.remove(loser);
//                List<TrackerObject> n_list = assign(candidates, trackedObjects);
//                n_list.add(loser);
//                return n_list;
//              }
//            }
            
          }
        
        else {
          if(candidates.size() == trackedObjects.size()) {
            // finally... we removed all overhead, the rest is simple matching
            double min_distance = Double.MAX_VALUE;
            TrackerObject t_winner = null;
            Region r_winner = null;
            for(TrackerObject obj : trackedObjects) {
              for(Region r : candidates) {
                double distance = distance(obj, r);
                //log.info("Genauso viele Objekte wie Regionen, Distanz von "+obj.getId()+" zu Region "+r.getLabel()+" = "+distance);
                if(distance < min_distance) {
                  t_winner = obj;
                  r_winner = r;
                  min_distance = distance;
                }
              }
            }
            if(min_distance < matchThreshold) {
              updateTrackerObject(t_winner, r_winner, currentTime);
              candidates.remove(r_winner);
              trackedObjects.remove(t_winner);
              List<TrackerObject> n_list = assign(candidates, trackedObjects);
              n_list.add(t_winner);
              return n_list;
            }
            else {
              TrackerObject obj_new = createTrackerObject(r_winner, currentTime);
              candidates.remove(r_winner);
              List<TrackerObject> n_list = assign(candidates, trackedObjects);
              n_list.add(obj_new);
              return n_list;
            }
          }
        }
      }
      return null;
    }
    
    private List<TrackerObject> assign_new(List<TrackerObject> trackerObjects, List<Region> candidates) {
     if(trackerObjects.isEmpty() && candidates.isEmpty()) {
       return new LinkedList<TrackerObject>();
     }
     else{
       // mehr Kandidaten als TrackedObjects -> erst zuweisen, dann neu erstellen!
       if(trackerObjects.size() < candidates.size()) {
         if(trackerObjects.size() == 0) {
           Region cand = candidates.get(0);
           TrackerObject obj = matchColorHistogram(cand, matchThreshold, currentTime);
           if(obj == null) {
             obj = createTrackerObject(cand, currentTime);
             candidates.remove(cand);
             List<TrackerObject> new_list = assign_new(trackerObjects, candidates);
             new_list.add(obj);
             return new_list;
           }
           else {
             updateTrackerObject(obj, cand, currentTime);
             candidates.remove(cand);
             List<TrackerObject> new_list = assign_new(trackerObjects, candidates);
             new_list.add(obj);
             return new_list;
           }
         }
         else {
           TrackerObject winner = null;
           Region best_region = null;
           double min_distance1 = Double.MAX_VALUE;
           for(TrackerObject obj : trackerObjects) {
             Region winner_region = null;
             double min_distance = Double.MAX_VALUE;
             for(Region r : candidates) {
               double distance = distance(obj, r);
               if(distance < min_distance) {
                 winner_region = r;
                 min_distance = distance;
               }
             }
             if(min_distance < min_distance1 && min_distance < matchThreshold) {
               min_distance1 = min_distance;
               winner = obj;
               best_region = winner_region;
             }
           }
           if(winner != null) {
             updateTrackerObject(winner, best_region, currentTime);
             candidates.remove(best_region);
             trackerObjects.remove(winner);
             List<TrackerObject> new_list = assign_new(trackerObjects, candidates);
             new_list.add(winner);
             return new_list;
           }
           else {
             Region loser = null;
             double max_distance = 0;
             for(Region cand : candidates) {
               double min_distance = Double.MAX_VALUE;
               for(TrackerObject obj : trackerObjects) {
                 double distance = distance(obj, cand);
                 if(distance < min_distance)
                   min_distance = distance;
               }
               if(min_distance > max_distance) {
                 max_distance = min_distance;
                 loser = cand;
               }
             }
             TrackerObject loser_obj = matchColorHistogram(loser, matchThreshold, currentTime);
             if(loser_obj != null) {
               updateTrackerObject(loser_obj, loser, currentTime);
             }
             else {
               loser_obj = createTrackerObject(loser, currentTime);
             }
             candidates.remove(loser);
             List<TrackerObject> new_list = assign_new(trackerObjects, candidates);
             new_list.add(loser_obj);
           }
         }
       }
       if(trackerObjects.size() == candidates.size()) {
         // wir suchen die besten matches raus, wenn einer schlechter als 
         // threshold ist, heißt das wohl, dass wir nen neuen kandidaten haben.
         TrackerObject winner = null;
         Region best_region = null;
         double min_distance1 = Double.MAX_VALUE;
         for(TrackerObject obj : trackerObjects) {
           Region winner_region = null;
           double min_distance = Double.MAX_VALUE;
           for(Region r : candidates) {
             double distance = distance(obj, r);
             if(distance < min_distance) {
               winner_region = r;
               min_distance = distance;
             }
           }
           if(min_distance < min_distance1) {
             min_distance1 = min_distance;
             winner = obj;
             best_region = winner_region;
           }
         }
         // wir haben da nen guten Match, also hauen wir das auch aufeinander!!
         if(min_distance1 < matchThreshold) {
           updateTrackerObject(winner, best_region, currentTime);
           candidates.remove(best_region);
           trackerObjects.remove(winner);
           List<TrackerObject> new_list = assign_new(trackerObjects, candidates);
           new_list.add(winner);
           return new_list;
         }
         // der Match war wohl nicht so toll... na dann: neu erstellen!
         else {
           TrackerObject new_to = createTrackerObject(best_region, currentTime);
           candidates.remove(best_region);
           List<TrackerObject> new_list = assign_new(trackerObjects, candidates);
           new_list.add(new_to);
           return new_list;
         }
       }
       if(trackerObjects.size() > candidates.size()) {
         // wir haben da wohl mindestens ein TrackerObject zu viel, bzw. nen Kandidaten zu wenig...
         // Idee ist: Wenn es nen Kandidaten gibt, wird der wohl mal zuerst gematcht.
         // falls das nicht der Fall ist, suchen wir uns die nächstbeste Region...
         if(!candidates.isEmpty()) {
           TrackerObject winner = null;
           Region best_region = null;
           double min_distance1 = Double.MAX_VALUE;
           for(TrackerObject obj : trackerObjects) {
             Region winner_region = null;
             double min_distance = Double.MAX_VALUE;
             for(Region r : candidates) {
               double distance = distance(obj, r);
               if(distance < min_distance) {
                 winner_region = r;
                 min_distance = distance;
               }
             }
             if(min_distance < min_distance1) {
               min_distance1 = min_distance;
               winner = obj;
               best_region = winner_region;
             }
           }
           // wir haben da nen guten Match, also hauen wir das auch aufeinander!!
           if(min_distance1 < matchThreshold) {
             updateTrackerObject(winner, best_region, currentTime);
             candidates.remove(best_region);
             trackerObjects.remove(winner);
             List<TrackerObject> new_list = assign_new(trackerObjects, candidates);
             new_list.add(winner);
             return new_list;
           }
           // der Match war wohl nicht so toll... na dann: neu erstellen!
           else {
             TrackerObject new_to = createTrackerObject(best_region, currentTime);
             candidates.remove(best_region);
             List<TrackerObject> new_list = assign_new(trackerObjects, candidates);
             new_list.add(new_to);
             return new_list;
           }
         }
         // Ach Mist, es gibt keine Kandidaten, aber noch TrackerObjects...
         else {
           double min_distance = Double.MAX_VALUE;
           TrackerObject winner = null;
           Region winner_region = null;
           for(TrackerObject obj : trackerObjects) {
             Region w_region = null;
             double min_distance1 = Double.MAX_VALUE;
             for(Region r : regions) {
               double dist = distance(obj, r);
               if(dist < min_distance1) {
                 min_distance1 = dist;
                 w_region = r;
               }
             }
             if(min_distance1 < min_distance && min_distance1 < matchThreshold) {
               min_distance = min_distance1;
               winner = obj;
               winner_region = w_region;
             }
           }
           if(winner != null) {
             moveTrackerObject(winner, winner_region.getCentroid(), currentTime);
             regions.remove(winner_region);
             trackerObjects.remove(winner);
             List<TrackerObject> new_list = assign_new(trackerObjects, candidates);
             new_list.add(winner);
             return new_list;
           }
           else {
             List<TrackerObject> new_list = new LinkedList<TrackerObject>();
             new_list.addAll(trackerObjects);
             return new_list;
           }
         }
       }
     }
     return null;
    }

    /**
     * Measures the hypothetic distance between a region and a trackerObject
     *
     * @param TrackerObject obj
     * @param Region r
     *
     * @return returns distance measure.
     **/
    private double distance(TrackerObject object, Region r) {
      try {
        // Compute ColorHistogram distance
        BoundingBox bbox = r.getBoundingBox();
        ColorHistogram ch1 = new ColorHistogram(img, imgc, bbox, 256);
        ColorHistogram ch2 = (ColorHistogram) object.getProperty(OBJ_PROPKEY_COLOR_HISTOGRAM);
        double d2 = ch1.bhattacharya_distance(ch2);
        // if some region splitted beforehand, we don't consider the physical
        // distance
        if(!checkSplitter(r)) {
          // compute physical distance
          Position centroid = (Position) object.getProperty(OBJ_PROPKEY_CENTROID);
          Position r_centroid = r.getCentroid();
          double d1 = centroid.distance(r_centroid);
          if(d1 < distanceThreshold)
            return Math.sqrt(d1*d2);
//          if(d1 < matchThreshold && d2 < chThreshold)           
//            return Math.sqrt(d1*d2);
//          if(d1 < matchThreshold)
        }
        return d2;
      } catch (Exception ex) {
        Logger.getLogger(ObjectTrackerImpl.class.getName()).log(Level.SEVERE, null, ex);
      }
      return Double.MAX_VALUE;
    }
    
  }
}
