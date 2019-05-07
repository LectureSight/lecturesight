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
package cv.lecturesight.ptz.steering.relativemove;

import cv.lecturesight.profile.api.SceneProfile;
import cv.lecturesight.profile.api.SceneProfileManager;
import cv.lecturesight.profile.api.Zone;
import cv.lecturesight.ptz.api.CameraListener;
import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.ptz.steering.api.UISlave;
import cv.lecturesight.scripting.api.ScriptingService;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.conf.ConfigurationListener;
import cv.lecturesight.util.geometry.CameraPositionModel;
import cv.lecturesight.util.geometry.CoordinatesNormalization;
import cv.lecturesight.util.geometry.NormalizedPosition;
import cv.lecturesight.util.geometry.Position;
import cv.lecturesight.util.geometry.Preset;
import cv.lecturesight.util.metrics.MetricsService;

import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Camera Steering SteeringWorker Implementation
 *
 */
public class CameraSteeringWorkerRelativeMove implements CameraSteeringWorker, ConfigurationListener {

  @Setter
  private Configuration config;        // service configuration

  @Setter
  private MetricsService metrics;      // metrics

  @Setter
  private PTZCamera camera;            // PTZCamera implementation

  @Setter
  private SceneProfileManager spm;     // Scene profile manager

  @Setter
  private ScriptingService engine;

  private CameraPositionModel model;   // model mapping normalized coords <--> camera coords

  private SteeringWorker worker;       // worker updating the pan and tilt speed

  private CameraBridge bridge; // script bridge

  // pan, tilt and zoom limits
  private int pan_min;
  private int pan_max;
  private int tilt_min;
  private int tilt_max;
  private int zoom_max;
  private boolean autoCalibrated = false;

  // max pan, tilt and zoom speeds
  protected int maxspeed_pan;
  protected int maxspeed_tilt;

  // alpha environment size in x and y direction
  private int alpha_x;
  private int alpha_y;

  // Distance within which the camera is considered to have reached the target
  private int stop_x;
  private int stop_y;

  // Time in milliseconds to allow camera to reach initial position
  private int initial_delay;

  // movement speed dampening factors
  private float damp_pan;
  private float damp_tilt;

  // The width and height of the frame in normalized co-ordinates (-1 to 1, so 0 < frame_width < 2)
  private float frame_width;
  private float frame_height;

  private boolean steering = false;           // indicates if the update callback steers camera
  private boolean moving = false;             // indicates if the camera if moving
  private boolean xflip = false;
  private boolean yflip = false;
  private boolean focus_fixed = false;        // Switch off auto-focus when in tracking position

  public enum CameraCmd { STOP, MOVE };
  private CameraCmd last_cmd;

  // lists of listeners
  private List<UISlave> uiListeners = new LinkedList<UISlave>();
  private List<MovementListener> moveListeners = new LinkedList<MovementListener>();

  private class SteeringWorker implements CameraListener {

    Position camera_pos;
    Position last_target = new Position(0, 0);
    int last_ps = 0;
    int last_ts = 0;
    int stopped_time = 500;
    long first_stop = 0;
    long move_start = 0;

    @Override
    public void positionUpdated(Position new_pos_camera) {

      Position new_pos = new_pos_camera.flip(xflip, yflip);

      Logger.trace("new_pos_camera: {} xflip: {} yflip: {}", new_pos_camera, xflip, yflip);

      // If no target has been set yet, do nothing except record position
      if (!model.isTargetSet()) {
        model.setCameraPosition(new_pos);
        camera_pos = new_pos;
        return;
      }

      Position target_pos = model.getTargetPosition();

      boolean target_changed = !(target_pos.getX() == last_target.getX() && target_pos.getY() == last_target.getY());

      Logger.trace("Camera position: {} target position: {} target_changed: {}", camera_pos, target_pos, target_changed);

      // update camera position model, notify movement listeners
      if (!new_pos.equals(camera_pos)) {
        if (!moving) {
          informMoveListenersStart(model.toNormalizedCoordinates(new_pos), model.toNormalizedCoordinates(target_pos));
          Logger.debug("Camera started moving");
          metrics.incCounter("camera.worker.move.start");
          move_start = System.currentTimeMillis();
        }
        moving = true;
        model.setCameraPosition(new_pos);
        camera_pos = new_pos;
        first_stop = 0;
      } else {
        long now = System.currentTimeMillis();
        if (first_stop > 0) {
          if (moving && (now - first_stop > stopped_time)) {
            informMoveListenersStop(model.toNormalizedCoordinates(new_pos), model.toNormalizedCoordinates(target_pos));
            Logger.debug("Camera stopped moving");
            moving = false;
            metrics.timedEvent("camera.worker.movement", now - move_start);
          }
        } else {
          first_stop = now;
        }
      }

      // update pan/tilt speeds, if steering is active
      if (steering) {
        int dx = camera_pos.getX() - target_pos.getX();
        int dy = camera_pos.getY() - target_pos.getY();
        int dx_abs = Math.abs(dx);
        int dy_abs = Math.abs(dy);
        int ps;
        int ts;

        // 1.5708rads == 90degs
        double theta = dx_abs==0 ? 1.5708 : Math.atan(dy_abs/(float)dx_abs);

        // If camera not close enough compute pan speed
        if (dx_abs >= stop_x) {
          int speed_pan = (int) (maxspeed_pan * Math.cos(theta));

          if (dx_abs < alpha_x) {
            ps = (int) (((float) dx_abs / (float) alpha_x) * speed_pan);
            if (ps > 1) {
              ps *= damp_pan;
            } else if (ps == 0) {
              dx = 0;
            }
            ps = (ps == 0) ? 1 : ps;
          } else {
            ps = (int) (speed_pan * damp_pan);
          }
        } else {
          dx = 0;
          ps = 0;
        }

        // If camera not close enough compute tilt speed
        if (dy_abs >= stop_y) {
          int speed_tilt = (int) (maxspeed_tilt * Math.sin(theta));
          if (dy_abs < alpha_y) {
            ts = (int) (((float) dy_abs / (float) alpha_y) * speed_tilt);
            if (ts > 1) {
              ts *= damp_tilt;
            }
            ts = (ts == 0) ? 1 : ts;
          } else {
            ts = (int) (speed_tilt * damp_tilt);
          }
        } else {
          dy = 0;
          ts = 0;
        }

        // apply computed speeds if speeds or target have changed
        if (target_changed || ps != last_ps || ts != last_ts || (moving && (ps == 0) && (ts == 0))) {

          Logger.debug("Steering check: moving={} target_changed={} last_ps={} last_ts={} ps={} ts={} dx={} dy={}",
            moving, target_changed, last_ps, last_ts, ps, ts, dx, dy);

          bridge.panSpeed.current = ps;
          bridge.tiltSpeed.current = ts;

          if (ps == 0 && ts == 0) {
            if (last_cmd != CameraCmd.STOP) {
              camera.stopMove();
              last_cmd = CameraCmd.STOP;
              Logger.trace("Camera Stop");
            }
          } else {

            if (dx < 0 && dy == 0) {
              camera.moveRight(ps);
            } else if (dx > 0 && dy == 0) {
              camera.moveLeft(ps);
            } else if (dx == 0 && dy < 0) {
              camera.moveUp(ts);
            } else if (dx == 0 && dy > 0) {
              camera.moveDown(ts);
            } else if (dx < 0 && dy < 0) {
              camera.moveUpRight(ps, ts);
            } else if (dx < 0 && dy > 0) {
              camera.moveDownRight(ps, ts);
            } else if (dx > 0 && dy < 0) {
              camera.moveUpLeft(ps, ts);
            } else if (dx > 0 && dy > 0) {
              camera.moveDownLeft(ps, ts);
            }
            last_cmd = CameraCmd.MOVE;
            Logger.trace("Camera Move");

          }

          last_ps = ps;
          last_ts = ts;
        }
      }
      last_target = target_pos.clone();
      informUISlaves();
    }
  }

  protected void activate(ComponentContext cc) throws Exception {

    // get service configuration
    setConfiguration();

    // get camera parameters
    maxspeed_pan = camera.getProfile().getPanMaxSpeed();
    maxspeed_tilt = camera.getProfile().getTiltMaxSpeed();
    zoom_max = camera.getProfile().getZoomMax();

    // Camera model
    model = new CameraPositionModel(pan_min, pan_max, tilt_min, tilt_max);

    // Now update the model for marker/preset calibration if available
    if (autoCalibrate()) {
      Logger.info("Automatic calibration, camera pan/tilt limits: pan {} to {}, tilt {} to {}",
        pan_min, pan_max, tilt_min, tilt_max);
    } else {
      Logger.info("Automatic calibration not possible");
    }

    // initialize worker
    worker = new SteeringWorker();
    worker.camera_pos = camera.getPosition();
    camera.addCameraListener(worker);
    camera.clearLimits();

    Logger.info("Activated. Steering " + camera.getName());
    if (config.getBoolean(Constants.PROPKEY_AUTOSTART)) {
      setSteering(true);
    }

    // create Camera scripting bridge
    bridge = new CameraBridge(this, engine);
    Dictionary<String, Object> props = new Hashtable<String, Object>();
    props.put("bridge.name", "Camera");
    props.put("bridge.imports", "cv.lecturesight.util.geometry");
    cc.getBundleContext().registerService(CameraBridge.class.getName(), (Object)bridge, props);
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    camera.removeCameraListener(worker);

    // Wait for any camera movements to complete (e.g. move to home / preset)
    Thread.sleep(1000);

    camera.stopMove();
    Logger.info("Deactivated");
  }

  public boolean autoCalibrate() {

    SceneProfile profile = spm.getActiveProfile();
    List<Zone> markerZones = profile.getCalibrationZones();

    Logger.debug("Active scene profile '{}' has {} calibration markers", profile.name, markerZones.size());

    if (markerZones.size() < 2) {
      // Need at least 2 calibration points
      return false;
    }

    List<Preset> presets = camera.getPresets();

    Logger.debug("Camera has {} presets", presets.size());

    HashMap<String,Preset> presetMap = new HashMap<>();
    for (Preset preset : presets) {
      presetMap.put(preset.getName(), preset);
    }

    List<NormalizedPosition> sceneMarkers = new ArrayList<>();
    List<Position> cameraPresets = new ArrayList<>();

    // Convert between overview co-ordinates and normalized co-ordinates
    CoordinatesNormalization normalizer = new CoordinatesNormalization(profile.width, profile.height);

    for (Zone marker : markerZones) {
      // Is there a preset for this zone?
      if (presetMap.containsKey(marker.name)) {

        Position p = (Position) presetMap.get(marker.name);
        cameraPresets.add(p);

        // marker is a rectangle, so find the centre point
        Position marker_pos = new Position(marker.x + marker.width/2, marker.y + marker.height/2);
        NormalizedPosition marker_posN = normalizer.toNormalized(marker_pos);
        sceneMarkers.add(marker_posN);

        Logger.debug("Using marker '{}' for calibration: overview {} camera {},{}",
          marker.name, marker_posN, p.getX(), p.getY());
      }
    }

    if (!cameraPresets.isEmpty()) {
      // Let the model decide if it has enough points
      if (model.update(sceneMarkers, cameraPresets)) {
        // Update the scene limits
        pan_min = model.getPanMin();
        pan_max = model.getPanMax();
        tilt_min = model.getTiltMin();
        tilt_max = model.getTiltMax();
        autoCalibrated = true;
        return true;
      }
    }

    return false;
  }

  @Override
  public void configurationChanged() {
    Logger.debug("Refreshing configuration");
    setConfiguration();

    if (!autoCalibrated) {
      // Update the model only if the scene limits are set manually
      model.update(pan_min, pan_max, tilt_min, tilt_max);
    }
  }

  /*
   ** Set configuration values
   */
  private void setConfiguration() {

    alpha_x = config.getInt(Constants.PROPKEY_ALPHAX);
    alpha_y = config.getInt(Constants.PROPKEY_ALPHAY);
    stop_x = config.getInt(Constants.PROPKEY_STOPX);
    stop_y = config.getInt(Constants.PROPKEY_STOPY);

    damp_pan = config.getFloat(Constants.PROPKEY_DAMP_PAN);
    if (damp_pan > 1.0 || damp_pan < 0.0) {
      Logger.warn("Illegal value for configuration parameter {}. Must be in range [0..1]. Using default value 1.0.",
        Constants.PROPKEY_DAMP_PAN);
      damp_pan = 1.0f;
    }
    damp_tilt = config.getFloat(Constants.PROPKEY_DAMP_TILT);
    if (damp_tilt > 1.0 || damp_tilt < 0.0) {
      Logger.warn("Illegal value for configuration parameter {}. Must be in range [0..1]. Using default value 1.0.",
        Constants.PROPKEY_DAMP_TILT);
      damp_tilt = 1.0f;
    }

    initial_delay = config.getInt(Constants.PROPKEY_INITIAL_DELAY);

    focus_fixed = config.getBoolean(Constants.PROPKEY_FOCUS_FIXED);

    // Update the scene limits only if they have not been auto-calibrated
    if (!autoCalibrated) {
      // Initialize limits for pan and tilt. If not configured, the limits from the camera profile are used.
      String val = config.get(Constants.PROPKEY_LIMIT_LEFT);
      if (val.isEmpty() || "none".equalsIgnoreCase(val)) {
        pan_min = camera.getProfile().getPanMin();
      } else {
        pan_min = config.getInt(Constants.PROPKEY_LIMIT_LEFT);
      }

      val = config.get(Constants.PROPKEY_LIMIT_RIGHT);
      if (val.isEmpty() || "none".equalsIgnoreCase(val)) {
        pan_max = camera.getProfile().getPanMax();
      } else {
        pan_max = config.getInt(Constants.PROPKEY_LIMIT_RIGHT);
      }

      val = config.get(Constants.PROPKEY_LIMIT_TOP);
      if (val.isEmpty() || "none".equalsIgnoreCase(val)) {
        tilt_max = camera.getProfile().getTiltMax();
      } else {
        tilt_max = config.getInt(Constants.PROPKEY_LIMIT_TOP);
      }

      val = config.get(Constants.PROPKEY_LIMIT_BOTTOM);
      if (val.isEmpty() || "none".equalsIgnoreCase(val)) {
        tilt_min = camera.getProfile().getTiltMin();
      } else {
        tilt_min = config.getInt(Constants.PROPKEY_LIMIT_BOTTOM);
      }

      Logger.debug("Camera pan/tilt limits: pan {} to {}, tilt {} to {}", pan_min, pan_max, tilt_min, tilt_max);
    }

    yflip = config.getBoolean(Constants.PROPKEY_YFLIP);
    xflip = config.getBoolean(Constants.PROPKEY_XFLIP);

    Logger.debug("Camera co-ordinates: xflip=" + xflip + " yflip=" + yflip);

    return;
  }

  public void stopMoving() {
    setTargetPosition(model.getCameraPositionNorm());
    try {
      camera.stopMove();
    } catch (Exception e) {
      Logger.warn("Unable to stop camera movement: " + e.getMessage());
    }
  }

  @Override
  public boolean isSteering() {
    return steering;
  }

  @Override
  public void setSteering(boolean b) {
    steering = b;
    if (b) {
      Logger.info("Steering is now ON");
    } else {
      Logger.info("Steering is now OFF");
      stopMoving();
    }
  }

  @Override
  public boolean isMoving() {
    return moving;
  }

  @Override
  public void setInitialPosition(String presetName) {

    // Get list of presets
    List<Preset> presets = camera.getPresets();

    NormalizedPosition pos = null;

    for (Preset preset : presets) {
      if (preset.getName().equalsIgnoreCase(presetName)) {
        // Found matching preset
        pos = model.toNormalizedCoordinates((Position) preset);
        break;
      }
    }

    if (pos == null) {
      Logger.warn("Camera preset '{}' not found: unable to move to initial camera position", presetName);
      return;
    }

    Logger.debug("Set initial normalized position from camera preset '{}' (x,y from -1 to 1): {} {}",
      presetName, pos.getX(), pos.getY());

    boolean s = steering;
    setSteering(false);

    model.setTargetPositionNorm(pos);

    camera.movePreset(presetName);

    // Allow the camera to reach the target absolute position before sending it any other movement commands
    try {
      Thread.sleep(initial_delay);
    } catch (Exception e) {
      // ignore
    }
    setSteering(s);

    if (focus_fixed) {
      camera.focusMode(PTZCamera.FocusMode.MANUAL);
    }

    informUISlaves();
  }

  @Override
  public void setInitialPosition(NormalizedPosition pos) {

    Logger.debug("Set initial normalized position (x,y from -1 to 1): " + pos.getX() + " " + pos.getY());

    // Implemented using an absolute move
    boolean s = steering;
    setSteering(false);

    model.setTargetPositionNorm(pos);
    Position target_pos = model.getTargetPosition();

    camera.moveAbsolute(maxspeed_pan, maxspeed_tilt, target_pos.flip(xflip, yflip));

    // Allow the camera to reach the target absolute position before sending it any other movement commands
    try {
      Thread.sleep(initial_delay);
    } catch (Exception e) {
      // ignore
    }
    setSteering(s);

    if (focus_fixed) {
      camera.focusMode(PTZCamera.FocusMode.MANUAL);
    }

    informUISlaves();
  }

  @Override
  public void setTargetPosition(NormalizedPosition pos) {

    Logger.debug("Set target normalized position (x,y from -1 to 1): {}", pos);

    model.setTargetPositionNorm(pos);
    informUISlaves();
  }

  @Override
  public NormalizedPosition getTargetPosition() {
    return model.getTargetPositionNorm();
  }

  @Override
  public Position getTargetCameraPosition() {
    return model.getTargetPosition();
  }

  @Override
  public NormalizedPosition getActualPosition() {
    return model.getCameraPositionNorm();
  }

  @Override
  public Position getActualCameraPosition() {
    return model.getCameraPosition();
  }

  @Override
  public void setZoom(float zoom) {
    int zoom_val = (int) (zoom_max * zoom);
    camera.zoom(zoom_val);
  }

  @Override
  public float getZoom() {
    return ((float) camera.getZoom()) / ((float) zoom_max);
  }

  @Override
  public void setFrameWidth(float frame_width) {
    // Ideally we want to actually use this to set the camera's zoom position. For now it's just for display purposes.
    this.frame_width = frame_width;
  }

  @Override
  public float getFrameWidth() {
    return frame_width;
  }

  @Override
  public void setFrameHeight(float frame_height) {
    // Ideally we want to actually use this to set the camera's zoom position. For now it's just for display purposes.
    this.frame_height = frame_height;
  }

  @Override
  public float getFrameHeight() {
    return frame_height;
  }

  @Override
  public boolean movePreset(String presetName) {
    if (camera.movePreset(presetName)) {
      if (focus_fixed) {
        camera.focusMode(PTZCamera.FocusMode.AUTO);
      }
      return true;
    }
    return false;
  }

  @Override
  public void moveHome() {
    camera.moveHome();
    if (focus_fixed) {
      camera.focusMode(PTZCamera.FocusMode.AUTO);
    }
  }

  @Override
  public int getPanMin() {
    return pan_min;
  }

  @Override
  public int getPanMax() {
    return pan_max;
  }

  @Override
  public int getTiltMin() {
    return tilt_min;
  }

  @Override
  public int getTiltMax() {
    return tilt_max;
  }

  @Override
  public void addUISlave(UISlave slave) {
    uiListeners.add(slave);
  }

  @Override
  public void removeUISlave(UISlave slave) {
    uiListeners.remove(slave);
  }

  private void informUISlaves() {
    for (UISlave s : uiListeners) {
      s.refresh();
    }
  }

  public void addMoveListener(MovementListener l) {
    moveListeners.add(l);
  }

  public void removeMoveListener(MovementListener l) {
    moveListeners.remove(l);
  }

  private void informMoveListenersStart(NormalizedPosition current, NormalizedPosition target) {
    for (MovementListener l : moveListeners) {
      l.moveStart(current, target);
    }
  }

  private void informMoveListenersStop(NormalizedPosition current, NormalizedPosition target) {
    for (MovementListener l : moveListeners) {
      l.moveStop(current, target);
    }
  }
}
