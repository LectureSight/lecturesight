package cv.lecturesight.ptz.api;

import java.util.Map;

/**
 * API for general Pan-Tilt-Zoom-Cameras (based on VISCA Camera Protocoll)
 *
 * Some camera specific setting should be done in the camera implementation and 
 * so the methods are not provided in this API. This is mainly connecting the 
 * camera (over RS-232, RS-485, USB, IP, ...).
 *
 * We don't expect that white ballance will be something that has to be set over
 * this interface. Some cameras may need an apperture controll (additional to 
 * backlight) too, what is currently not provided here too. We expect the 
 * systems to run in auto mode for this.
 */
public interface PTZCamera {

  /**
   * Turn camera power on or off.
   * @param power true to turn camera on, false to turn camera off
   */
  public void setPower(boolean power) throws Exception;

  /**
   * Returns the power status of the camera.
   * @return true ist the camera is on and false if the camera is off
   */
  public boolean isPowerOn() throws Exception;

  /**
   * Get the status of all changeable features of the camera from the camera.
   * 
   * @return A map with key value pairs, where the values can be of any type but 
   *         should fit to the key. Default keys are "pan", "tilt", "zoom", 
   *         "focus", "backlight", "panspeed", "tiltspeed", "zoomspeed". 
   *          Other camera specific keys can be added too.
   */
  public Map<String, Object> getStatus() throws Exception;

  /**
   * Set the given values at the cameras.
   * @param values A map with the values that should be set on the camera. Only keys that are provided will be set.Default keys are "pan", "tilt", "zoom", "focus", "backlight", "panspeed", "tiltspeed", "zoomspeed". Other camera specific keys can be added too.
   * @throws IllegalArgumentException Additional to wrong data-types there may be dependencies on the parameters. On VISCA Cameras it is not possible to set a pan value without a tilt value (and vice versa)
   */
  public void setStatus(Map<String, Object> values) throws IllegalArgumentException, Exception;

  /**
   * Set the pan speed.
   * @param speed normalizedFloat value between 0 (slow) and 1 (fast)
   */
  public void setPanSpeed(float speed) throws Exception;

  /**
   * Set the tilt speed.
   * @param speed normalized Float value between 0 (slow) and 1 (fast)
   */
  public void setTiltSpeed(float speed) throws Exception;

  /**
   * Set the zoom speed.
   * @param speed normalized Float value between 0 (slow) and 1 (fast)
   */
  public void setZoomSpeed(float speed) throws Exception;

  /**
   * Get the pan speed.
   * @return speed normalized as Float value between 0 (slow) and 1 (fast)
   */
  public float getPanSpeed() throws Exception;

  /**
   * Get the tilt speed.
   * @return speed normalized as Float value between 0 (slow) and 1 (fast)
   */
  public float getTiltSpeed() throws Exception;

  /**
   * Get the zoom speed.
   * @return speed normalized as Float value between 0 (slow) and 1 (fast)
   */
  public float getZoomSpeed() throws Exception;

  /**
   * Turn on and off backlight.
   * @param backlight true to turn backlight on and false to turn backlight off.
   */
  public void backlight(boolean backlight) throws Exception;

  /**
   * Returns if the backlight function is turned on or off.
   * @return true if backlight is on otherwise false.
   */
  public boolean isBacklight() throws Exception;

  /**
   * Zoom in continously at the speed set with setZoomSpeed.
   */
  public void zoomIn() throws Exception;

  /**
   * Zoom out continously at the speed set with setZoomSpeed.
   */
  public void zoomOut() throws Exception;

  /**
   * Stop the continous zooming.
   */
  public void stopZoom() throws Exception;

  /**
   * Get the current value of the zoom.
   * @return float with the normalized value between 0 (wide) and 1 (tele)
   */
  public float getZoom() throws Exception;

  /**
   * Zoom to the provided value.
   * @param zoom float with the normalized value between 0 (wide) and 1 (tele)
   */
  public void setZoom(float zoom) throws Exception;

  /**
   * Move the camera to the specified coordinates in the speed set in by setPanSpeed and setTiltSpeed.
   * @param pan float with the normalized value for the x-axis from -1(max. left) to +1 (max. right)
   * @param tilt float with the normalized value for the y-axis from -1(max. bottom) to +1 (max. top)
   */
  public void move(float pan, float tilt) throws Exception;

  /**
   * Move the camera down with the speed set in by setTiltSpeed.
   */
  public void moveDown() throws Exception;

  /**
   * Move the camera up with the speed set in by setTiltSpeed.
   */
  public void moveUp() throws Exception;

  /**
   * Move the camera left with the speed set in by setPanSpeed.
   */
  public void moveLeft() throws Exception;

  /**
   * Move the camera left with the speed set in by setPanSpeed.
   */
  public void moveRight() throws Exception;

  public void moveUpLeft() throws Exception;
  
  public void moveUpRight() throws Exception;
  
  public void moveDownLeft() throws Exception;
  
  public void moveDownRight() throws Exception;
  
  public void limitPanTiltUpRight(float pan, float tilt) throws Exception;
  
  public void limitPanTiltDownLeft(float pan, float tilt) throws Exception;
  
  /**
   * Stop moving the camera.
   */
  public void stopMove() throws Exception;

  /**
   * Get the current value for the focus.
   * @return Float with the normalized value for the focus from 0 (near) to 1 (far)
   */
  public float getFocus() throws Exception;

  /**
   * Set the current value for the focus.
   * @param focus Float with the normalized value for the focus from 0 (near) to 1 (far)
   */
  public void setFocus(float focus) throws Exception;

  /**
   * Move focus to far.
   */
  public void focusFar() throws Exception;

  /**
   * Move focus to near.
   */
  public void focusNear() throws Exception;

  /**
   * Stop changing the focus.
   */
  public void focusStop() throws Exception;

  /**
   * Turn autofocus and and off.
   * @param autofocus true to turn AF on and false to turn it off.
   */
  public void setAutoFocus(boolean autofocus) throws Exception;

  /**
   * Current state of the autofocus.
   * @return true to turn AF on and false to turn it off.
   */
  public boolean isAutoFocusSet() throws Exception;
  
  public float getPan() throws Exception;
  
  public float getTilt() throws Exception;
}
