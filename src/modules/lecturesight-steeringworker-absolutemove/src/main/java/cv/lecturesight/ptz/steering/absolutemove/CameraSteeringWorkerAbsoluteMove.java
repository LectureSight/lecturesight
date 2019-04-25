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
package cv.lecturesight.ptz.steering.absolutemove;

import cv.lecturesight.ptz.api.CameraListener;
import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.ptz.steering.api.UISlave;
import cv.lecturesight.scripting.api.ScriptingService;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.CameraPositionModel;
import cv.lecturesight.util.geometry.NormalizedPosition;
import cv.lecturesight.util.geometry.Position;
import cv.lecturesight.util.geometry.Preset;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

/**
 * Camera Steering SteeringWorker Implementation
 *
 */
public class CameraSteeringWorkerAbsoluteMove implements CameraSteeringWorker {

  @Setter
  Configuration config;        // service configuration

  @Setter
  PTZCamera camera;            // PTZCamera implementation

  @Setter
  private ScriptingService engine;

  CameraPositionModel model;   // model mapping normalized coords <--> camera coords

  SteeringWorker worker;       // worker updating the pan and tilt speed

  private CameraBridge bridge; // script bridge

  int pan_min, pan_max;               // pan limits
  int tilt_min, tilt_max;             // tilt limits
  int zoom_min, zoom_max;             // zoom limits
  int maxspeed_zoom;                  // max zoom speed
  int maxspeed_pan, maxspeed_tilt;    // max pan and tilt speeds
  int alpha_x, alpha_y;               // alpha environment size in x and y direction
  float damp_pan, damp_tilt;          // movement speed dampening factors
  float frame_width;                  // The width of the frame in normalized co-ordinates (-1 to 1, so 0 < frame_width < 2)
  float frame_height;                 // The height of the frame in normalized co-ordinates (-1 to 1, so 0 < frame_width < 2)

  boolean steering = false;           // indicates if the update callback steers camera
  boolean moving = false;             // indicates if the camera if moving
  boolean xflip = false;
  boolean yflip = false;

  // lists of listeners
  List<UISlave> uiListeners = new LinkedList<UISlave>();
  List<MovementListener> moveListeners = new LinkedList<MovementListener>();

  private class SteeringWorker implements CameraListener {

    Position camera_pos;
    Position last_target = new Position(0, 0);
    int last_ps = 0;
    int last_ts = 0;

    @Override
    public void positionUpdated(Position new_pos_camera) {

      Position new_pos = new_pos_camera.flip(xflip, yflip);

      Position target_pos = model.getTargetPosition();
      boolean target_changed = !(target_pos.getX() == last_target.getX() && target_pos.getY() == last_target.getY());

      // update camera position model, notify movement listeners
      if (new_pos.getX() != camera_pos.getX() || new_pos.getY() != camera_pos.getY()) {
        if (!moving) {
          informMoveListenersStart(model.toNormalizedCoordinates(new_pos), model.toNormalizedCoordinates(target_pos));
          Logger.debug("Camera started moving");
        }
        moving = true;
        model.setCameraPosition(new_pos);
        camera_pos = new_pos;
      } else {
        if (moving) {
          informMoveListenersStop(model.toNormalizedCoordinates(new_pos), model.toNormalizedCoordinates(target_pos));
          Logger.debug("Camera stopped moving");
        }
        moving = false;
      }

      // update pan/tilt speeds, if steering is active
      if (steering) {
        int dx = camera_pos.getX() - target_pos.getX();
        int dy = camera_pos.getY() - target_pos.getY();
        int dx_abs = Math.abs(dx);
        int dy_abs = Math.abs(dy);

        // compute pan speed
        int ps;
        if (dx_abs < alpha_x) {
          ps = (int) (((float) dx_abs / (float) alpha_x) * maxspeed_pan);
          if (ps > 1) {
            ps *= damp_pan;
          }
          ps = ps == 0 ? 1 : ps;
        } else {
          ps = (int) (maxspeed_pan * damp_pan);
        }

        // compute tilt speed
        int ts;
        if (dy_abs < alpha_y) {
          ts = (int) (((float) dy_abs / (float) alpha_y) * maxspeed_tilt);
          if (ts > 1) {
            ts *= damp_tilt;
          }
          ts = ts == 0 ? 1 : ts;
        } else {
          ts = (int) (maxspeed_tilt * damp_tilt);
        }

        // apply computed speeds if speeds or target have changed
        if (target_changed || ps != last_ps || ts != last_ts || (moving && (ps == 0))) {

          Logger.debug("Steering check: moving=" + moving + " target_changed=" + target_changed + " last_ps=" + last_ps + " last_ts=" + last_ts + " ps=" + ps + " ts=" + ts + " dx=" + dx + " dy=" + dy);

          bridge.panSpeed.current = ps;
          bridge.tiltSpeed.current = ts;

          camera.moveAbsolute(ps, ts, target_pos);

          last_ps = ps;
          last_ts = ts;
        }
      }
      last_target = target_pos.clone();
      informUISlaves();
    }
  }

  protected void activate(ComponentContext cc) throws Exception {

    model = initModel();   // init camera model

    // get camera parameters
    maxspeed_pan = camera.getProfile().getPanMaxSpeed();
    maxspeed_tilt = camera.getProfile().getTiltMaxSpeed();
    maxspeed_zoom = camera.getProfile().getZoomMaxSpeed();
    zoom_min = camera.getProfile().getZoomMin();
    zoom_max = camera.getProfile().getZoomMax();

    // get service configuration
    alpha_x = config.getInt(Constants.PROPKEY_ALPHAX);
    alpha_y = config.getInt(Constants.PROPKEY_ALPHAY);
    damp_pan = config.getFloat(Constants.PROPKEY_DAMP_PAN);
    if (damp_pan > 1.0 || damp_pan < 0.0) {
      Logger.warn("Illegal value for configuration parameter " + Constants.PROPKEY_DAMP_PAN + ". Must be in range [0..1]. Using default value 1.0.");
      damp_pan = 1.0f;
    }
    damp_tilt = config.getFloat(Constants.PROPKEY_DAMP_TILT);
    if (damp_tilt > 1.0 || damp_tilt < 0.0) {
      Logger.warn("Illegal value for configuration parameter " + Constants.PROPKEY_DAMP_PAN + ". Must be in range [0..1]. Using default value 1.0.");
      damp_tilt = 1.0f;
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
    Logger.info("Deactivated");
  }

  private CameraPositionModel initModel() {
    // initialize limits for pan and tilt, if not configured by camera calibration
    // the limits from the camera profile are taken
    String val = config.get(Constants.PROPKEY_LIMIT_LEFT);
    if (val.isEmpty() || val.equalsIgnoreCase("none")) {
      pan_min = camera.getProfile().getPanMin();
    } else {
      pan_min = config.getInt(Constants.PROPKEY_LIMIT_LEFT);
    }

    val = config.get(Constants.PROPKEY_LIMIT_RIGHT);
    if (val.isEmpty() || val.equalsIgnoreCase("none")) {
      pan_max = camera.getProfile().getPanMax();
    } else {
      pan_max = config.getInt(Constants.PROPKEY_LIMIT_RIGHT);
    }

    val = config.get(Constants.PROPKEY_LIMIT_TOP);
    if (val.isEmpty() || val.equalsIgnoreCase("none")) {
      tilt_max = camera.getProfile().getTiltMax();
    } else {
      tilt_max = config.getInt(Constants.PROPKEY_LIMIT_TOP);
    }

    val = config.get(Constants.PROPKEY_LIMIT_BOTTOM);
    if (val.isEmpty() || val.equalsIgnoreCase("none")) {
      tilt_min = camera.getProfile().getTiltMin();
    } else {
      tilt_min = config.getInt(Constants.PROPKEY_LIMIT_BOTTOM);
    }

    Logger.debug("Camera pan/tilt limits: pan " + pan_min + " to " + pan_max + ", tilt " + tilt_min + " to " + tilt_max);

    yflip = config.getBoolean(Constants.PROPKEY_YFLIP);
    xflip = config.getBoolean(Constants.PROPKEY_XFLIP);

    Logger.debug("Camera co-ordinates: xflip=" + xflip + " yflip=" + yflip);

    return new CameraPositionModel(pan_min, pan_max, tilt_min, tilt_max);
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
      stopMoving();
      Logger.info("Steering is now OFF");
    }
  }

  @Override
  public boolean isMoving() {
    return moving;
  }

  @Override
  public boolean autoCalibrate() {
    // not supported
    return false;
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
    Position target_pos = model.getTargetPosition();

    camera.movePreset(presetName);

    setSteering(s);

    informUISlaves();
  }

  @Override
  public void setInitialPosition(NormalizedPosition pos) {
    Logger.debug("Set initial normalized position (x,y from -1 to 1): " + pos.getX() + " " + pos.getY());
    setTargetPosition(pos);
  }

  @Override
  public void setTargetPosition(NormalizedPosition pos) {

    Logger.debug("Set target normalized position (x,y from -1 to 1): " + pos.getX() + " " + pos.getY());

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
    return camera.movePreset(presetName);
  }

  @Override
  public void moveHome() {
    camera.moveHome();
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
