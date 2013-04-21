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
package cv.lecturesight.cameraoperator.simple;

import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
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
  Configuration config;
  @Reference
  ObjectTracker tracker;
  @Reference
  CameraSteeringWorker camera;
  int interval = 200;
  int timeout;
  ScheduledExecutorService executor;
  CameraOperatorWorker worker;

  protected void activate(ComponentContext cc) throws Exception {
    timeout = config.getInt(Constants.PROPKEY_TIMEOUT);
    start();    
    log.info("Activated. Timeout is " + timeout + " ms");
  }

  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated");
  }

  @Override
  public void start() {
    if (executor == null) {
      executor = Executors.newScheduledThreadPool(1);
      worker = new CameraOperatorWorker();
      camera.setZoom(config.getInt(Constants.PROPKEY_ZOOM));
      executor.scheduleAtFixedRate(worker, 0, interval, TimeUnit.MILLISECONDS);
      log.info("Started");
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
    NormalizedPosition neutral = new NormalizedPosition(0.0f, 0.0f);
    camera.setTargetPosition(neutral);
    camera.setZoom(0);
  }

  private class CameraOperatorWorker implements Runnable {

    TrackerObject target = null;

    @Override
    public void run() {
      if (target == null) {
        target = findBiggestTrackedObject(tracker.getCurrentlyTracked());
      } else {
        if (System.currentTimeMillis() - target.lastSeen() < timeout) {
          Position obj_pos = (Position) target.getProperty(ObjectTracker.OBJ_PROPKEY_CENTROID);
          NormalizedPosition target_pos = new NormalizedPosition(obj_pos.getX(), config.getFloat(Constants.PROPKEY_TILT));
          camera.setTargetPosition(target_pos);
        } else {
          target = null;
        }
      }
    }

    private TrackerObject findBiggestTrackedObject(List<TrackerObject> objects) {
      TrackerObject out = null;
      int maxWeight = 0;
      for (TrackerObject obj : objects) {
        Integer weight = (Integer) obj.getProperty(ObjectTracker.OBJ_PROPKEY_WEIGHT);
        if (weight > maxWeight) {
          maxWeight = weight;
          out = obj;
        }
      }
      return out;
    }
  }
}
