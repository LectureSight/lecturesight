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

  final static String PROPKEY_AUTOSTART = "autostart";
  final static String PROPKEY_SHOWUI = "ui.show";
  final static String PROPKEY_INTERVAL = "interval";
  final static String PROPKEY_ALPHAX = "move.alpha.x";
  final static String PROPKEY_ALPHAY = "move.alpha.y";
  final static String PROPKEY_STOPX = "move.stop.x";
  final static String PROPKEY_STOPY = "move.stop.y";
  final static String PROPKEY_DAMP_PAN = "move.damp.pan";
  final static String PROPKEY_DAMP_TILT = "move.damp.tilt";
  private Log log = new Log("Camera Steering Worker");
  @Reference
  Configuration config;
  @Reference
  PTZCamera camera;
  private CameraPositionModel model;
  CameraMovementUI ui;
  SteeringWorker worker;
  ScheduledExecutorService executor = null;
  int maxspeed_pan, maxspeed_tilt;
  boolean move_snap = false;
  boolean steering = false;

  protected void activate(ComponentContext cc) throws Exception {
    model = new CameraPositionModel(camera);
    maxspeed_pan = camera.getProfile().getPanMaxSpeed();
    maxspeed_tilt = (int)(0.7 * camera.getProfile().getTiltMaxSpeed());
    ui = new CameraMovementUI(model);
    if (config.getBoolean(PROPKEY_SHOWUI)) {
      ui.show(true);
    }
    if (config.getBoolean(PROPKEY_AUTOSTART)) {
      start();
      setSteering(true);
    }
    log.info("Activated. Steering " + camera.getName());
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    stop();
    ui.show(false);
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
      executor.scheduleAtFixedRate(worker, 0, config.getInt(PROPKEY_INTERVAL), TimeUnit.MILLISECONDS);
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
    ui.update();
  }

  @Override
  public NormalizedPosition getTargetPosition() {
    return model.getTargetPosition();
  }

  @Override
  public NormalizedPosition getActualPosition() {
    return model.getTargetPosition();
  }

  private class SteeringWorker implements Runnable {

    Position cam_pos;
    Position target_pos;
    NormalizedPosition cam_posn;
    NormalizedPosition last_posn = model.getActualPosition();

    @Override
    public void run() {
      int stop_x = config.getInt(PROPKEY_STOPX);
      int alpha_x = config.getInt(PROPKEY_ALPHAX);
      int stop_y = config.getInt(PROPKEY_STOPY);
      int alpha_y = config.getInt(PROPKEY_ALPHAY);
      
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

        String status = "pan = " + ps + "  tilt = " + ts + "  dx = " + dx + "  dy = " + dy + " cmd:";
        
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
      
      ui.update();
      last_posn = cam_posn;
    }
  }
}
