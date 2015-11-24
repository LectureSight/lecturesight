package cv.lecturesight.ptzcontrol.dummy;

import cv.lecturesight.ptz.api.CameraListener;
import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.ptz.api.PTZCameraProfile;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.Position;
import java.util.LinkedList;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "cv.lecturesight.ptzcontrol.dummy", immediate = true)
@Service
public class SimulatedCamera implements PTZCamera {

  static final String PROPKEY_DELAY ="delay";
  static final String CAMERA_NAME = "Simulated Camera";
  static final int PAN_MAX_SPEED = 10;
  static final int TILT_MAX_SPEED = 10;
  static final int ZOOM_MAX_SPEED = 100;
  static final int PAN_MIN = -100;
  static final int PAN_MAX = 100;
  static final int TILT_MIN = -100;
  static final int TILT_MAX = 100;
  static final int ZOOM_MAX = 1000;
  final Position HOME_POS = new Position(0, 0);
  
  static Log log = new Log(CAMERA_NAME);
  
  @Reference
  Configuration config;
  
  PTZCameraProfile myProfile = new PTZCameraProfile(
          "ACME Inc.", CAMERA_NAME,
          PAN_MIN, PAN_MAX, PAN_MAX_SPEED,
          TILT_MIN, TILT_MAX, TILT_MAX_SPEED,
          0, ZOOM_MAX, ZOOM_MAX_SPEED,
          HOME_POS);
  
  int delay;
  boolean running = true;
  Position current_pos = HOME_POS.clone();
  Position target_pos = HOME_POS.clone();
  int current_zoom = 0;
  int target_zoom;
  int speedPan = 0;
  int speedTilt = 0;
  int speedZoom = 0;
  
  final Object mutex = new Object();
  List<CameraListener> listeners = new LinkedList<CameraListener>();
  
  protected void activate(ComponentContext cc) {
    delay = config.getInt(PROPKEY_DELAY);
    (new Thread(new Runnable() {

      @Override
      public void run() {
        log.info("Entering main loop");
        
        int dx,dy;
        while(running) {
          synchronized(mutex) {
            // update X

            dx = target_pos.getX() - current_pos.getX();

            if (dx > 0 ) {
              current_pos.setX(current_pos.getX() + speedPan);
              if (current_pos.getX() > target_pos.getX()) current_pos.setX(target_pos.getX());
            } else if (dx < 0) {
              current_pos.setX(current_pos.getX() - speedPan);
              if (current_pos.getX() < target_pos.getX()) current_pos.setX(target_pos.getX());
            }

            // update Y
            dy = target_pos.getY() - current_pos.getY();

            if (dy > 0 ) {
              current_pos.setY(current_pos.getY() + speedTilt);
              if (current_pos.getY() > target_pos.getY()) current_pos.setY(target_pos.getY());
            } else if (dy < 0) {
              current_pos.setY(current_pos.getY() - speedTilt);
              if (current_pos.getY() > target_pos.getY()) current_pos.setY(target_pos.getY());
            }
          }
        }
        
        log.info("Exited main loop");
      }
      
    })).start();
    log.info("Activated. Delay is " + delay + " ms");
  }
  
  protected void deactivate(ComponentContext cc) {
    running = false;
    log.info("Deactivating");
  }
  
  @Override
  public String getName() {
    return CAMERA_NAME;
  }

  @Override
  public PTZCameraProfile getProfile() {
    return myProfile;
  }

  @Override
  public void reset() {
    // TODO ??
  }

  @Override
  public void cancel() {
    synchronized(mutex) {
      target_pos = current_pos.clone();
      target_zoom = current_zoom;
    }
  }

  @Override
  public void stopMove() {
    cancel();
  }

  @Override
  public void moveHome() {
    synchronized(mutex) {
      target_pos = HOME_POS.clone();
      speedPan = PAN_MAX_SPEED / 2;
      speedTilt = TILT_MAX_SPEED / 2;
    }
  }

  @Override
  public void movePreset(int preset) {
    moveHome();       // the only preset in this camera is the home position
  }

  @Override
  public void moveUp(int speed) {
    synchronized(mutex) {
      target_pos.setY(TILT_MAX);
      speedTilt = speed;
    }
  }

  @Override
  public void moveDown(int speed) {
    synchronized(mutex) {
      target_pos.setY(TILT_MIN);
      speedTilt = speed;
    }
  }

  @Override
  public void moveLeft(int speed) {
    synchronized(mutex) {
      target_pos.setX(PAN_MIN);
      speedPan = speed;
    }
  }

  @Override
  public void moveRight(int speed) {
    synchronized(mutex) {
      target_pos.setX(PAN_MAX);
      speedPan = speed;
    }
  }

  @Override
  public void moveUpLeft(int panSpeed, int tiltSpeed) {
    synchronized(mutex) {
      target_pos.setX(PAN_MIN);
      target_pos.setY(TILT_MAX);
      speedPan = panSpeed;
      speedTilt = tiltSpeed;
    }
  }

  @Override
  public void moveUpRight(int panSpeed, int tiltSpeed) {
    synchronized(mutex) {
      target_pos.setX(PAN_MAX);
      target_pos.setY(TILT_MAX);
      speedPan = panSpeed;
      speedTilt = tiltSpeed;
    }
  }

  @Override
  public void moveDownLeft(int panSpeed, int tiltSpeed) {
    synchronized(mutex) {
      target_pos.setX(PAN_MIN);
      target_pos.setY(TILT_MIN);
      speedPan = panSpeed;
      speedTilt = tiltSpeed;
    }
  }

  @Override
  public void moveDownRight(int panSpeed, int tiltSpeed) {
    synchronized(mutex) {
      target_pos.setX(PAN_MAX);
      target_pos.setY(TILT_MIN);
      speedPan = panSpeed;
      speedTilt = tiltSpeed;
    }
  }

  @Override
  public void moveAbsolute(int panSpeed, int tiltSpeed, Position target) {
    synchronized(mutex) {
      Position new_target = ensureTargetInLimits(target);
      target_pos.setX(new_target.getX());
      target_pos.setY(new_target.getY());
      speedPan = panSpeed;
      speedTilt = tiltSpeed;
    }
  }

  @Override
  public void moveRelative(int panSpeed, int tiltSpeed, Position target) {
    synchronized(mutex) {
      Position new_target = current_pos.clone();
      new_target.setX(current_pos.getX() + target.getX());
      new_target.setY(current_pos.getY() + target.getY());
      new_target = ensureTargetInLimits(new_target);
      target_pos.setX(new_target.getX());
      target_pos.setY(new_target.getY());
      speedPan = panSpeed;
      speedTilt = tiltSpeed;
    }
  }
  
  Position ensureTargetInLimits(Position in) {
    Position out = in.clone();
    
    // TODO add logic
    
    return out;
  }

  @Override
  public void clearLimits() {
    log.warn("method clearLimits() currently not implemented");
  }

  @Override
  public void setLimitUpRight(int pan, int tilt) {
    log.warn("method setLimitUpRight() currently not implemented");
  }

  @Override
  public void setLimitDownLeft(int pan, int tilt) {
    log.warn("method setLimitDownLeft() currently not implemented");
  }

  @Override
  public Position getPosition() {
    Position out;
    synchronized(mutex) {
      out = current_pos.clone();
    }
    return out;
  }

  @Override
  public void stopZoom() {
    synchronized(mutex) {
      speedZoom = 0;
      target_zoom = current_zoom;
    }
  }

  @Override
  public void zoomIn(int speed) {
    synchronized(mutex) {
      target_zoom = ZOOM_MAX;
      speedZoom = speed;
    }
  }

  @Override
  public void zoomOut(int speed) {
    synchronized(mutex) {
      target_zoom = 0;
      speedZoom = speed;
    }
  }

  @Override
  public void zoom(int zoom) {
    synchronized(mutex) {
      current_zoom = zoom;
    }
  }

  @Override
  public int getZoom() {
    return current_zoom;
  }

  @Override
  public void addCameraListener(CameraListener l) {
    listeners.add(l);
  }

  @Override
  public void removeCameraListener(CameraListener l) {
    listeners.remove(l);
  }
  
}
