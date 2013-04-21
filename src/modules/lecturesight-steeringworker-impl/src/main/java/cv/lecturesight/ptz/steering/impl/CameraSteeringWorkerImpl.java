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
package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.NormalizedPosition;
import cv.lecturesight.util.geometry.Position;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Camera Steering SteeringWorker Implementation
 *
 */
@Component(name = "lecturesight.ptz.steering.worker", immediate = true)
@Service
public class CameraSteeringWorkerImpl implements CameraSteeringWorker {

  private Log log = new Log("Camera Steering Worker");
  @Reference
  Configuration config;
  @Reference
  PTZCamera camera;
  private CameraPositionModel model;
  private CameraControlPanel controlPanel;
  SteeringWorker worker;
  ScheduledExecutorService executor = null;
  int maxspeed_pan, maxspeed_tilt;
  boolean move_snap = false;
  boolean steering = false;

  protected void activate(ComponentContext cc) throws Exception {
    model = new CameraPositionModel(camera, config);
    maxspeed_pan = camera.getProfile().getPanMaxSpeed();
    maxspeed_tilt = (int)(camera.getProfile().getTiltMaxSpeed());
    controlPanel = new CameraControlPanel(model);
    if (config.getBoolean(Constants.PROPKEY_AUTOSTART)) {
      start();
      setSteering(true);
    }
    log.info("Activated. Steering " + camera.getName());
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    stop();
    log.info("Deactivated");
  }

  @Override
  public boolean isSteering() {
    return steering;
  }

  @Override
  public void setSteering(boolean b) {
    if (b == false) {
      stopMoving();
      log.info("Steering OFF");
    } else {
      log.info("Steering ON");
    }
    steering = b;
  }

  @Override
  public boolean isMoving() {
    return model.isMoving();
  }

  @Override
  public void stopMoving() {
    setTargetPosition(model.getActualPosition());
    try {
      camera.stopMove();
    } catch (Exception e) {
      log.warn("Unable to stop camera movement: " + e.getMessage());
    }
  }

  @Override
  public void start() {
    if (executor == null) {
      executor = Executors.newScheduledThreadPool(1);
      worker = new SteeringWorker();
      executor.scheduleAtFixedRate(worker, 0, config.getInt(Constants.PROPKEY_INTERVAL), TimeUnit.MILLISECONDS);
      log.info("Started");
    } else {
      log.warn("Already running!");
    }
  }

  @Override
  public void stop() {
    if (executor != null) {
      setSteering(false);
      executor.shutdownNow();
      executor = null;
      log.info("Stopped");
    } else {
      log.warn("Nothing to stop");
    }
  }

  @Override
  public void setTargetPosition(NormalizedPosition pos) {
    model.setTargetPosition(pos);
    controlPanel.repaint();
  }

  @Override
  public NormalizedPosition getTargetPosition() {
    return model.getTargetPosition();
  }

  @Override
  public NormalizedPosition getActualPosition() {
    return model.getTargetPosition();
  }

  CameraControlPanel getControlPanel() {
    return controlPanel;
  }

  @Override
  public void setZoom(int factor) {
    camera.zoom(factor);
  }
  
  public int getZoom() {
    return camera.getZoom();
  }

  private class SteeringWorker implements Runnable {

    Position cam_pos;
    Position target_pos;
    NormalizedPosition cam_posn;
    NormalizedPosition last_posn = model.getActualPosition();

    @Override
    public void run() {
      int stop_x = config.getInt(Constants.PROPKEY_STOPX);
      int alpha_x = config.getInt(Constants.PROPKEY_ALPHAX);
      int stop_y = config.getInt(Constants.PROPKEY_STOPY);
      int alpha_y = config.getInt(Constants.PROPKEY_ALPHAY);
      
      // get current position of camera
      try {
        cam_pos = camera.getPosition();
        model.setCamPosition(cam_pos);
        cam_posn = model.toNormalizedCoordinates(cam_pos);
        model.setActualPosition(cam_posn);
        if (!steering) {
          model.setMoving(last_posn.getX() != cam_posn.getX() || last_posn.getY() != cam_posn.getY());
        }
      } catch (Exception e) {
        log.warn("Unable to update actual postion: " + e.getMessage());
      }

      if (steering) {
        target_pos = model.toCameraCoordinates(model.getTargetPosition());

        int dx = cam_pos.getX() - target_pos.getX();
        int dx_abs = Math.abs(dx);
        int dy = cam_pos.getY() - target_pos.getY();
        int dy_abs = Math.abs(dy);
        
        // adjust pan speed
        int ps;
        if (model.isMoving() && dx_abs < stop_x) {
          ps = 0;
        } else {
          if (dx_abs < alpha_x) {
            ps = (int) (((float) dx_abs / (float) alpha_x) * maxspeed_pan);
          } else {
            ps = maxspeed_pan;
          }
        }

        // adjust tilt speed
        int ts;
        if (model.isMoving() && dy_abs < stop_y) {
          ts = 0;
        } else {
          if (dy_abs < alpha_y) {
            ts = (int) (((float) dy_abs / (float) alpha_y) * maxspeed_tilt);
          } else {
            ts = maxspeed_tilt;
          }
        }

        String status = "dx = " + dx + "  dy = " + dy + "\nv_pan = " + ps + "  v_tilt = " + ts +  "\ncmd:";
        
        // set speed on camera
        if (ps == 0 && ts == 0) {
          if (model.isMoving()) {
            camera.stopMove();
            model.setMoving(false);
            status += " STOP";
          } else {
            status += " STILL";
          }
          
        } else if (dx < 0 && dy < 0 && ps > 0 && ts > 0) {
          camera.moveUpRight(ps, ts);
          model.setMoving(true);
          status += " UP-RIGHT";

        } else if (dx < 0 && dy > 0 && ps > 0 && ts > 0) {
          camera.moveDownRight(ps, ts);
          model.setMoving(true);
          status += " DOWN-RIGHT";

        } else if (dx > 0 && dy < 0 && ps > 0 && ts > 0) {
          camera.moveUpLeft(ps, ts);
          model.setMoving(true);
          status += " UP-LEFT";

        } else if (dx > 0 && dy > 0 && ps > 0 && ts > 0) {
          camera.moveDownLeft(ps, ts);
          model.setMoving(true);
          status += " DOWN_LEFT";

        } else if (dx < 0 && ps > 0 && ts == 0) {
          camera.moveRight(ps);
          model.setMoving(true);
          status += " RIGHT";

        } else if (dx > 0 && ps > 0 && ts == 0) {
          camera.moveLeft(ps);
          model.setMoving(true);
          status += " LEFT";

        } else if (dy < 0 && ps == 0 && ts > 0) {
          camera.moveUp(ts);
          model.setMoving(true);
          status += " UP";

        } else if (dy > 0 && ps == 0 && ts > 0) {
          camera.moveDown(ts);
          model.setMoving(true);
          status += " DOWN";
        }
        model.setStatus(status);
      }
      
      controlPanel.repaint();
      last_posn = cam_posn;
    }
  }
}
