package cv.lecturesight.cameraoperator.simple;

import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.geometry.CoordinatesNormalization;
import cv.lecturesight.util.geometry.NormalizedPosition;
import cv.lecturesight.util.geometry.Position;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.cameraoperator.simple", immediate = true)
@Service
public class SimpleCameraOperator implements CameraOperator {

  Log log = new Log("Simple Camera Operator");
  
  @Reference
  ObjectTracker tracker;
  
  @Reference
  CameraSteeringWorker camera;
  
  @Reference
  FrameSourceProvider fsp;
  FrameSource fsrc;
  
  int interval = 200;
  float limit_left = -0.2f;
  float limit_right = 0.2f;
  float limit_top = 0.23f;
  float limit_down = -0.58f;
  float rx_pos, rx_neg;
  
  ScheduledExecutorService executor;
  CameraOperatorWorker worker;
  CoordinatesNormalization normalizer;
  
  protected void activate(ComponentContext cc) throws Exception {
    fsrc = fsp.getFrameSource();
    normalizer = new CoordinatesNormalization(fsrc.getWidth(), fsrc.getHeight());
    rx_pos = 1.0f / limit_right;
    rx_neg = -1.0f / limit_left;
    start();
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated");
  }

  @Override
  public void start() {
    if (executor == null) {
      executor = Executors.newScheduledThreadPool(1);
      worker = new CameraOperatorWorker();
      executor.scheduleAtFixedRate(worker, 0, interval, TimeUnit.MILLISECONDS);
      log.info("Started");
    } else {
      log.warn("Already running!");
    }
  }

  @Override
  public void stop() {
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
      log.info("Stopped");
    } else {
      log.warn("Nothing to stop");
    }
  }

  @Override
  public void reset() {
    log.info("Reset not implemented");
  }
  
  
  private class CameraOperatorWorker implements Runnable {
    
    NormalizedPosition pos_home = new NormalizedPosition(0.0f, 0.0f);
    
    @Override
    public void run() {
      TrackerObject biggest = findBiggestTrackedObject(tracker.getCurrentlyTracked());
      if (biggest == null) {
        camera.setTargetPosition(pos_home);
      } else {
        Position pos_obj = (Position)biggest.getProperty(ObjectTracker.OBJ_PROPKEY_CENTROID);
        NormalizedPosition npos_obj = normalizer.toNormalized(pos_obj);
        NormalizedPosition npos_target = pos_home.clone();
        if (npos_obj.getX() < 0) {
          npos_target.setX(npos_obj.getX() / rx_neg);
        } else if (npos_obj.getX() > 0) {
          npos_target.setX(npos_obj.getX() / rx_pos);
        }
        camera.setTargetPosition(npos_target);
        //System.out.println("TARGET: " + npos_target.getX() + " " + npos_target.getY());
      }
    }
    
    private TrackerObject findBiggestTrackedObject(List<TrackerObject> objects) {
      TrackerObject out = null;
      int maxWeight = 0;
      for (TrackerObject obj : objects) {
        Integer weight = (Integer)obj.getProperty(ObjectTracker.OBJ_PROPKEY_WEIGHT);
        if (weight > maxWeight) {
          maxWeight = weight;
          out = obj;
        }
      }
      return out;
    }
  }
}
