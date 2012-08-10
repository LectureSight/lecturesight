package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.NormalizedPosition;
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
  final static String PROPKEY_THRESHALPHA = "thresh.alpha";
  final static String PROPKEY_THRESHBETA = "thresh.beta";
  final static String PROPKEY_THRESHXY = "thresh.xy";
  final static String PROPKEY_SNAP = "move.snap";
  final static String PROPKEY_DAMP_PAN = "move.damp.pan";
  final static String PROPKEY_DAMP_TILT = "move.damp.tilt";
  private Log log = new Log("Camera Steering Worker");
  @Reference
  Configuration config;
  @Reference
  PTZCamera camera;
  private CameraMovementModel model;
  CameraMovementUI ui;
  SteeringWorker worker;
  ScheduledExecutorService executor = null;
  float dt_a = 0.2f, dt_b = 0.05f, dt_xy = 0.02f, damp_pan, damp_tilt;
  boolean move_snap = false;
  boolean steering = false;

  protected void activate(ComponentContext cc) throws Exception {
    model = new CameraMovementModel(camera.getPortName());
    ui = new CameraMovementUI(model);
    if (config.getBoolean(PROPKEY_SHOWUI)) {
      ui.show(true);
    }
    if (config.getBoolean(PROPKEY_AUTOSTART)) {
      start();
      setSteering(true);
    }
    log.info("Activated. Steering " + camera.getPortName());
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    stop();
    ui.show(false);
    log.info("Deactivated");
  }

  void updateConfiguration() {
    dt_a = config.getFloat(PROPKEY_THRESHALPHA);
    dt_b = config.getFloat(PROPKEY_THRESHBETA);
    dt_xy = config.getFloat(PROPKEY_THRESHXY);
    damp_pan = config.getFloat(PROPKEY_DAMP_PAN);
    damp_tilt = config.getFloat(PROPKEY_DAMP_TILT);
    move_snap = config.getBoolean(PROPKEY_SNAP);
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
      updateConfiguration();
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
      worker.interrupt();
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

    boolean running = true;
    NormalizedPosition last_pos = model.getActualPosition();
    NormalizedPosition current_pos;

    @Override
    public void run() {
      // get current position of camera
      try {
        current_pos = new NormalizedPosition(camera.getPan(), camera.getTilt());
        model.setActualPosition(current_pos);
        model.setMoving(last_pos.getX() != current_pos.getX() || last_pos.getY() != current_pos.getY());
        ui.update();
      } catch (Exception e) {
        log.warn("Unable to update actual postion: " + e.getMessage());
      }

      // compute raw, absolute and euclidean distance btw actual position and target position
      float dx = model.getActualPosition().getX() - model.getTargetPosition().getX();
      float dx_abs = (float) Math.abs(dx);
      float dy = model.getActualPosition().getY() - model.getTargetPosition().getY();
      float dy_abs = (float) Math.abs(dy);
      float d = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

      try {
        if (steering && d > dt_b) {                  // did we reach target?

          if (dx_abs < dt_a) {
            float r = dx_abs / dt_a;
            camera.setPanSpeed(damp_pan * r);
          } else {
            camera.setPanSpeed(damp_pan * 1.0f);
          }

          if (dy_abs < dt_a) {
            float r = dy_abs / dt_a;
            camera.setTiltSpeed(damp_tilt * r);
          } else {
            camera.setTiltSpeed(damp_tilt * 1.0f);
          }

          if (dx > dt_xy && dy < dt_xy) {
            camera.moveUpLeft();

          } else if (dx < dt_xy && dy < dt_xy) {
            camera.moveUpRight();

          } else if (dx > dt_xy && dy > dt_xy) {
            camera.moveDownLeft();

          } else if (dx < dt_xy && dy > dt_xy) {
            camera.moveDownRight();

          } else if (dx < dt_xy) {
            camera.moveRight();

          } else if (dx > dt_xy) {
            camera.moveLeft();

          } else if (dy < dt_xy) {
            camera.moveUp();

          } else if (dy > dt_xy) {
            camera.moveDown();

          }
        } else {
          camera.stopMove();
          if (move_snap) {
            camera.move(
                    model.getTargetPosition().getX(),
                    model.getTargetPosition().getY());
          }
        }

      } catch (Exception e) {
        log.warn("Unable to set camera movement: " + e.getMessage());
      }

      last_pos = current_pos;
    }

    public void interrupt() {
      running = false;
    }
  }
}
