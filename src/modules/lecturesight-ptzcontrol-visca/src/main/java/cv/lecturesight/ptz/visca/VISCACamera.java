package cv.lecturesight.ptz.visca;

import cv.lecturesight.ptz.api.PTZCameraException;
import cv.lecturesight.ptz.api.PTZCamera;
import java.util.HashMap;
import java.util.Map;

/**
 * API for general Pan-Tilt-Zoom-Cameras (based on VISCA Camera Protocoll)
 *
 * Some camera specific setting should be done in the camera implementation and so the methods are not provided in this API. This is mainly connecting the camera (over RS-232, RS-485, USB, IP, ...).
 *
 * We don't expect that white ballance will be something that has to be set over this interface.
 * Some cameras may need an apperture controll (additional to backlight) too, what is currently not provided here too. We expect the systems to run in auto mode for this.
 */
public class VISCACamera implements PTZCamera {

  static {
    System.loadLibrary("ptzcamvisca");
  }
  private int camNo;
  private String port = "";
  private String portName = "port1";

  public VISCACamera(String portName) {
    this.portName = portName;
  }

  /**
   * Returns the camera number.
   * @return camera number
   */
  public int getCamNo() {
    return camNo;
  }

  /**
   * Returns comm port.
   * @return comm port
   */
  public String getPort() {
    return port;
  }

  /**
   * Returns comm port name.
   * @return port name
   */
  public String getPortName() {
    return portName;
  }

  public native int getConnectedCams();

  /**
   * Open comm connection to the camera.
   * @param port comm port
   * @param cam camera number
   * @return true if successfull
   */
  protected native boolean initialize(String port, int cam);

  /**
   * Close comm connection.
   * @return true if successfull
   */
  protected native boolean deinitialize();

  /**
   * Turn camera power on or off.
   * @param power true to turn camera on, false to turn camera off
   */
  @Override
  public native void setPower(boolean power) throws PTZCameraException;

  /**
   * Returns the power status of the camera.
   * @return true ist the camera is on and false if the camera is off
   */
  @Override
  public native boolean isPowerOn() throws PTZCameraException;

  /**
   * Get the status of all changeable features of the camera from the camera.
   * @return A map with key value pairs, where the values can be of any type but should fit to the key.
   *         Default keys are "pan", "tilt", "zoom", "focus", "backlight", "panspeed", "tiltspeed", "zoomspeed". Other camera specific keys can be added too.
   */
  @Override
  public Map<String, Object> getStatus() throws PTZCameraException {
    Map<String, Object> status = new HashMap<String, Object>();

    status.put("cam", getCamNo());
    status.put("port", getPort());
    status.put("portname", getPortName());

    Boolean power = isPowerOn();
    status.put("power", power);

    if (power) {
      status.put("pan", getPan());
      status.put("tilt", getTilt());
      status.put("zoom", getZoom());
      status.put("panspeed", getPanSpeed());
      status.put("tiltspeed", getTiltSpeed());
      status.put("zoomspeed", getZoomSpeed());
      status.put("backlight", isBacklight());
      Boolean autofocus = isAutoFocusSet();
      status.put("autofocus", autofocus);
      if (!autofocus) {
        status.put("focus", getFocus());
      }
    }

    return status;
  }

  /**
   * Set the given values at the cameras.
   * @param values A map with the values that should be set on the camera. Only keys that are provided will be set.Default keys are "pan", "tilt", "zoom", "focus", "backlight", "panspeed", "tiltspeed", "zoomspeed". Other camera specific keys can be added too.
   * @throws IllegalArgumentPTZCamViscaException Additional to wrong data-types there may be dependencies on the parameters. On VISCA Cameras it is not possible to set a pan value without a tilt value (and vice versa)
   */
  @Override
  public void setStatus(Map<String, Object> values) throws IllegalArgumentException, PTZCameraException {
    if (values == null || values.isEmpty()) {
      return;
    }

    if (values.containsKey("power")) {
      Boolean power = (Boolean) values.get("power");
      setPower(power);
      if (!power) {
        return;
      }
    }

    if (values.containsKey("panspeed")) {
      setPanSpeed((Float) values.get("panspeed"));
    }

    if (values.containsKey("tiltspeed")) {
      setTiltSpeed((Float) values.get("tiltspeed"));
    }

    if (values.containsKey("pan") && values.containsKey("tilt")) {
      move((Float) values.get("pan"), (Float) values.get("tilt"));
    }

    if (values.containsKey("zoomspeed")) {
      setZoomSpeed((Float) values.get("zoomspeed"));
    }

    if (values.containsKey("zoom")) {
      setZoom((Float) values.get("zoom"));
    }

    if (values.containsKey("autofocus")) {
      Boolean autofocus = (Boolean) values.get("autofocus");
      setAutoFocus(autofocus);

      if (!autofocus && values.containsKey("focus")) {
        setFocus((Float) values.get("focus"));
      }
    }

    if (values.containsKey("backlight")) {
      backlight((Boolean) values.get("backlight"));
    }
  }

  /**
   * Set the pan speed.
   * @param speed normalizedFloat value between 0 (slow) and 1 (fast)
   */
  @Override
  public native void setPanSpeed(float speed) throws IllegalArgumentException, PTZCameraException;

  /**
   * Set the tilt speed.
   * @param speed normalized Float value between 0 (slow) and 1 (fast)
   */
  @Override
  public native void setTiltSpeed(float speed) throws IllegalArgumentException, PTZCameraException;

  /**
   * Set the zoom speed.
   * @param speed normalized Float value between 0 (slow) and 1 (fast)
   */
  @Override
  public native void setZoomSpeed(float speed) throws IllegalArgumentException, PTZCameraException;

  /**
   * Get the pan speed.
   * @return speed normalized as Float value between 0 (slow) and 1 (fast)
   */
  @Override
  public native float getPanSpeed() throws PTZCameraException;

  /**
   * Get the tilt speed.
   * @return speed normalized as Float value between 0 (slow) and 1 (fast)
   */
  @Override
  public native float getTiltSpeed() throws PTZCameraException;

  /**
   * Get the zoom speed.
   * @return speed normalized as Float value between 0 (slow) and 1 (fast)
   */
  @Override
  public native float getZoomSpeed() throws PTZCameraException;

  /**
   * Turn on and off backlight.
   * @param backlight true to turn backlight on and false to turn backlight off.
   */
  @Override
  public native void backlight(boolean backlight) throws PTZCameraException;

  /**
   * Returns if the backlight function is turned on or off.
   * @return true if backlight is on otherwise false.
   */
  @Override
  public native boolean isBacklight() throws PTZCameraException;

  /**
   * Zoom in continously at the speed set with setZoomSpeed.
   */
  @Override
  public native void zoomIn() throws PTZCameraException;

  /**
   * Zoom out continously at the speed set with setZoomSpeed.
   */
  @Override
  public native void zoomOut() throws PTZCameraException;

  /**
   * Stop the continous zooming.
   */
  @Override
  public native void stopZoom() throws PTZCameraException;

  /**
   * Get the current value of the zoom.
   * @return float with the normalized value between 0 (wide) and 1 (tele)
   */
  @Override
  public native float getZoom() throws PTZCameraException;

  /**
   * Zoom to the provided value.
   * @param zoom float with the normalized value between 0 (wide) and 1 (tele)
   */
  @Override
  public native void setZoom(float zoom) throws IllegalArgumentException, PTZCameraException;

  /**
   * Move the camera to the specified coordinates in the speed set in by setPanSpeed and setTiltSpeed.
   * @param pan float with the normalized value for the x-axis from -1(max. left) to +1 (max. right)
   * @param tilt float with the normalized value for the y-axis from -1(max. bottom) to +1 (max. top)
   */
  @Override
  public native void move(float pan, float tilt) throws IllegalArgumentException, PTZCameraException;

  /**
   * Move the camera down with the speed set in by setTiltSpeed.
   */
  @Override
  public native void moveDown() throws PTZCameraException;

  /**
   * Move the camera up with the speed set in by setTiltSpeed.
   */
  @Override
  public native void moveUp() throws PTZCameraException;

  /**
   * Move the camera left with the speed set in by setPanSpeed.
   */
  @Override
  public native void moveLeft() throws PTZCameraException;

  /**
   * Move the camera left with the speed set in by setPanSpeed.
   */
  @Override
  public native void moveRight() throws PTZCameraException;

  /**
   * Stop moving the camera.
   */
  @Override
  public native void stopMove() throws PTZCameraException;

  /**
   * Get the current value for the focus.
   * @return Float with the normalized value for the focus from 0 (near) to 1 (far)
   */
  @Override
  public native float getFocus() throws PTZCameraException;

  /**
   * Set the current value for the focus.
   * @param focus Float with the normalized value for the focus from 0 (near) to 1 (far)
   */
  @Override
  public native void setFocus(float focus) throws IllegalArgumentException, PTZCameraException;

  /**
   * Move focus to far.
   */
  @Override
  public native void focusFar() throws PTZCameraException;

  /**
   * Move focus to near.
   */
  @Override
  public native void focusNear() throws PTZCameraException;

  /**
   * Stop changing the focus.
   */
  @Override
  public native void focusStop() throws PTZCameraException;

  /**
   * Turn autofocus and and off.
   * @param autofocus true to turn AF on and false to turn it off.
   */
  @Override
  public native void setAutoFocus(boolean autofocus) throws PTZCameraException;

  /**
   * Current state of the autofocus.
   * @return true to turn AF on and false to turn it off.
   */
  @Override
  public native boolean isAutoFocusSet() throws PTZCameraException;

  /**
   * Get pan position.
   */
  @Override
  public native float getPan() throws PTZCameraException;

  /**
   * Get tilt position.
   */
  @Override
  public native float getTilt() throws PTZCameraException;
}
