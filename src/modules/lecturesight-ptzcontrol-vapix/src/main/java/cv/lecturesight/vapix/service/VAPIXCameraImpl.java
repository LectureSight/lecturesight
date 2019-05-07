package cv.lecturesight.vapix.service;

import cv.lecturesight.ptz.api.CameraListener;
import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.ptz.api.PTZCameraException;
import cv.lecturesight.ptz.api.PTZCameraProfile;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.Position;
import cv.lecturesight.util.geometry.Preset;

import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PTZCamera implementation using the Axis VAPIX protocol:
 * http://www.axis.com/techsup/cam_servers/dev/cam_http_api_index.php
 *
 * The communication with the camera is based around Hypertext Transfer Protocol
 * (HTTP) response and requests.
 * The returning value for success is:
 *
 * - HTTP_NO_CONTENT (204): Command has been sent.
 * - HTTP_OK (200): Command sent.
 *
 * and response in text format. The returning text format is structured
 * as [propertyName]=[propertyValue]
 *
 * @author Corne Oosthuizen (CILT - UCT) [2015-12]

 */
public class VAPIXCameraImpl implements PTZCamera {

  @Setter
  Configuration config; // service configuration

  String model_name;
  String brand;
  String host;
  String username;
  String password;

  // Is camera inverted
  boolean inverted = false;

  public Limits lim_pan;
  public Limits lim_tilt;
  public Limits lim_zoom;
  Limits speed_pan;
  Limits speed_tilt;
  Limits speed_zoom;
  public Limits range_pan;
  public Limits range_tilt;
  public Limits range_zoom;
  public Limits range_focus;

  // Scale values are used to scale inputs to corrected output values
  float scale_pan = 0f;
  float scale_tilt = 0f;
  float scale_zoom = 0f;
  float scale_pan_rev = 0f;
  float scale_tilt_rev = 0f;
  float scale_zoom_rev = 0f;
  float scale_pan_speed = 0f;
  float scale_tilt_speed = 0f;
  int zoom;
  int focus;

  boolean can_move_absolute = false;
  boolean can_move_relative = false;
  boolean can_move_continuous = false;
  boolean flipped = false;

  Properties properties;
  PTZCameraProfile profile;
  List<CameraListener> observers;
  String[] presets;

  long last_update; // time in milliseconds of last position update
  int updateInterval = 200; // number of millisec. between state updates

  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  ScheduledFuture updaterHandle;

  /**
   * Service Component Runtime uses default constructor, so the implicite
   * default constructor suffices.
   */

  /**
   * OSGI service activation method.
   *
   * Initialize this VAPIX Camera service object and load all the appropriate
   * details from properties.
   *
   * @param cc
   *            ComponentContext
   * @throws cv.lecturesight.ptz.api.PTZCameraException
   */
  protected void activate(ComponentContext cc) throws PTZCameraException {
    this.last_update = 0L;

    // Load properties
    host = config.get(Constants.PROPKEY_VAPIX_CAMERA);
    username = config.get(Constants.PROPKEY_VAPIX_USERNAME);
    password = config.get(Constants.PROPKEY_VAPIX_PASSWORD);

    updateInterval = config.getInt(Constants.PROPKEY_UPDATER_INTERVAL);

    lim_pan = new Limits(config.getInt(Constants.PROFKEY_PAN_MIN), config.getInt(Constants.PROFKEY_PAN_MAX));
    lim_tilt = new Limits(config.getInt(Constants.PROFKEY_TILT_MIN), config.getInt(Constants.PROFKEY_TILT_MAX));
    lim_zoom = new Limits(config.getInt(Constants.PROFKEY_ZOOM_MIN), config.getInt(Constants.PROFKEY_ZOOM_MAX));

    speed_pan = new Limits(1, config.getInt(Constants.PROFKEY_PAN_MAXSPEED));
    speed_tilt = new Limits(1, config.getInt(Constants.PROFKEY_TILT_MAXSPEED));
    speed_zoom = new Limits(1, config.getInt(Constants.PROFKEY_ZOOM_MAXSPEED));

    if (host.length() > 0) {

      this.model_name = "VAPIX Camera (404)";
      this.brand = "None";
      this.profile = new PTZCameraProfile(this.brand, // manufacturer
                                          this.model_name, // should be "VAPIX Camera (404)"
                                          0, 0, 0, // pan min, max, speed
                                          0, 0, 0, // tilt min, max, speed
                                          0, 0, 0, // zoom min, max, speed
                                          new Position(0, 0));

      Hashtable<String, String> result = processCommand("/axis-cgi/param.cgi?usergroup=admin&action=list&group=Brand");
      Logger.info("Vapix connecting to " + host + " [ " + username + " : " + password + " ] ("
                  + result.get("success") + ")");

      if (result.get("success").equals("1")) {
        this.model_name = result.get("root.Brand.ProdFullName");
        this.brand = result.get("root.Brand.Brand");

        this.presets = getPresetNames();

        Logger.info("Vapix: " + this.brand + " " + this.model_name);

        // Get camera orientation ie inverted or not
        Hashtable<String, String>  sensor = processCommand("/axis-cgi/param.cgi?action=list&usergroup=admin&group=ImageSource.I0.Sensor");

        if ("180".equals(sensor.getOrDefault("root.ImageSource.I0.Sensor.VideoRotation", "0"))) {
          inverted = true;
          Logger.debug("camera is inverted");
        }

        Hashtable<String, String> parameters = processCommand("/axis-cgi/param.cgi?action=list&group=PTZ");

        if (parameters.get("success").equals("1")) {

          can_move_absolute = parameters.get("root.PTZ.Support.S1.AbsolutePan").equals("true")
          && parameters.get("root.PTZ.Support.S1.AbsoluteTilt").equals("true")
          && parameters.get("root.PTZ.Support.S1.AbsoluteZoom").equals("true");

          can_move_relative = parameters.get("root.PTZ.Support.S1.RelativePan").equals("true")
          && parameters.get("root.PTZ.Support.S1.RelativeTilt").equals("true")
          && parameters.get("root.PTZ.Support.S1.RelativeZoom").equals("true");

          can_move_continuous = parameters.get("root.PTZ.Support.S1.ContinuousPan").equals("true")
          && parameters.get("root.PTZ.Support.S1.ContinuousTilt").equals("true")
          && parameters.get("root.PTZ.Support.S1.ContinuousZoom").equals("true");

          /*
           * root.PTZ.Limit.L1.MaxBrightness=9999
           * root.PTZ.Limit.L1.MaxFieldAngle=623
           * root.PTZ.Limit.L1.MaxFocus=9999
           * root.PTZ.Limit.L1.MaxIris=9999
           * root.PTZ.Limit.L1.MaxPan=170
           * root.PTZ.Limit.L1.MaxTilt=90
           * root.PTZ.Limit.L1.MaxZoom=9999
           * root.PTZ.Limit.L1.MinBrightness=1
           * root.PTZ.Limit.L1.MinFieldAngle=22
           * root.PTZ.Limit.L1.MinFocus=770
           * root.PTZ.Limit.L1.MinIris=1 root.PTZ.Limit.L1.MinPan=-170
           * root.PTZ.Limit.L1.MinTilt=-20 root.PTZ.Limit.L1.MinZoom=1
           */
          range_pan = new Limits(Integer.parseInt(parameters.get("root.PTZ.Limit.L1.MinPan")),
                                 Integer.parseInt(parameters.get("root.PTZ.Limit.L1.MaxPan")));

          if (inverted) {
            range_tilt = new Limits(Integer.parseInt(parameters.get("root.PTZ.Limit.L1.MaxTilt"))*-1,
                                    Integer.parseInt(parameters.get("root.PTZ.Limit.L1.MinTilt"))*-1);
            // modify tilt min/max range to match
            lim_tilt = new Limits(config.getInt(Constants.PROFKEY_TILT_MAX)*-1, config.getInt(Constants.PROFKEY_TILT_MIN)*-1);
          } else {
            range_tilt = new Limits(Integer.parseInt(parameters.get("root.PTZ.Limit.L1.MinTilt")),
                                    Integer.parseInt(parameters.get("root.PTZ.Limit.L1.MaxTilt")));
          }

          range_zoom = new Limits(Integer.parseInt(parameters.get("root.PTZ.Limit.L1.MinZoom")),
                                  Integer.parseInt(parameters.get("root.PTZ.Limit.L1.MaxZoom")));

          range_focus = new Limits(Integer.parseInt(parameters.get("root.PTZ.Limit.L1.MinFocus")),
                                   Integer.parseInt(parameters.get("root.PTZ.Limit.L1.MaxFocus")));

          this.scale_pan = (float) (range_pan.max - range_pan.min) / (float) (lim_pan.max - lim_pan.min);
          this.scale_tilt = (float) (range_tilt.max - range_tilt.min) / (float) (lim_tilt.max - lim_tilt.min);
          this.scale_zoom = (float) (range_zoom.max - range_zoom.min) / (float) (lim_zoom.max - lim_zoom.min);

          this.scale_pan_rev = (float) (lim_pan.max - lim_pan.min)
          / (float) (range_pan.max - this.range_pan.min);
          this.scale_tilt_rev = (float) (lim_tilt.max - lim_tilt.min)
          / (float) (range_tilt.max - this.range_tilt.min);
          this.scale_zoom_rev = (float) (lim_zoom.max - lim_zoom.min)
          / (float) (range_zoom.max - this.range_zoom.min);

          // documentation for pan and tilt speed is 100 for max speed
          this.scale_pan_speed = (float) (100 - 0) / (float) (speed_pan.max - speed_pan.min);
          this.scale_tilt_speed = (float) (100 - 0) / (float) (speed_tilt.max - speed_tilt.min);

          Logger.trace("Profile: " + "Pan: " + lim_pan + " " + speed_pan + " " + range_pan + " | Tilt: "
                       + lim_tilt + " " + speed_tilt + " " + range_tilt + " | Zoom: " + lim_zoom + " " + speed_zoom
                       + " " + range_zoom);

          Logger.trace(" Delta: " + scale_pan + " " + scale_tilt + " " + scale_zoom + " | " + scale_pan_rev
                       + " " + scale_tilt_rev + " " + scale_zoom_rev + " | " + scale_pan_speed + " "
                       + scale_tilt_speed);

          this.profile = new PTZCameraProfile(this.brand, this.model_name, lim_pan.min, lim_pan.max,
                                              speed_pan.max, lim_tilt.min, lim_tilt.max, speed_tilt.max, lim_zoom.min, lim_zoom.max,
                                              speed_zoom.max, getPosition());

          this.observers = new LinkedList<CameraListener>();

          // start update thread
          updaterHandle = executor.scheduleWithFixedDelay(new CameraStateUpdater(), updateInterval,
                                                          updateInterval, TimeUnit.MILLISECONDS);
        } else {
          String msg = "Camera not responding correctly to HTTPRequest - group=PTZ.";
          Logger.error(msg);
          throw new PTZCameraException(msg);
        }
      } else {
        String msg = "Camera not responding correctly to HTTPRequest - group=Brand.";
        Logger.error(msg);
        throw new PTZCameraException(msg);
      }

    } else {
      String msg = "Could not connect to VAPIX Camera - no host name";
      Logger.error(msg);

      // OSGI-Hint: In case this service is not able initialize its
      // functionality it must to throw an Exception
      // to signal to the OSGI-framework that the service activation has
      // failed.
      throw new PTZCameraException(msg);
    }
  }

  /**
   * OSGI service de-activation method.
   *
   * @param cc
   *            Context of component in OSGI
   */
  protected void deactivate(ComponentContext cc) {
    // de-init
    try {
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.SECONDS);
    } catch (Exception e) {
      Logger.debug("Unable to terminate scheduled processes cleanly");
    }
    Logger.debug("Deactivated");
  }

  /**
   * The url connection for the camera
   *
   * @return string with url to send HTTP request to
   */
  public String getUrl() {
    return "http://" + this.host;
  }

  /**
   * Send the command to the camera and handles response
   *
   * @param command
   *            The command to execute
   * @return String containing the resulting string of executing the command
   */
  public String doCommand(String command) throws IOException {

    Authenticator.setDefault(new MyAuthenticator(username, password));

    URL obj = new URL(getUrl() + command);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestMethod("GET");

    int responseCode = con.getResponseCode();
    Logger.trace("GET [" + getUrl() + command + "]: " + responseCode);

    if (responseCode == HttpURLConnection.HTTP_OK) {

      // success
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine + "~");
      }
      in.close();

      String result = response.toString();
      return result.length() == 0 ? "ok" : result;
    } else if ((responseCode == HttpURLConnection.HTTP_CREATED) || // 201
               (responseCode == HttpURLConnection.HTTP_ACCEPTED) || // 202
               (responseCode == HttpURLConnection.HTTP_NO_CONTENT)) // 204
    {
      return "ok";
    } else {
      return "err";
    }
  }

  /**
   * The response in most cases is a list of properties and is then put in a
   * Hashtable for easy access
   *
   * @param command
   *            The command to execute
   * @return Hastable structure with result of command as <propertyName,
   *         propertyValue>
   */
  public Hashtable<String, String> processCommand(String command) {

    String result = "err";
    try {
      result = doCommand(command);
    } catch (IOException e) {
      Logger.debug("Invalid command: " + e.getMessage());
    }

    Hashtable<String, String> processed = new Hashtable<String, String>();

    if ("err".equals(result)) {
      processed.put("success", "0");
    } else if ("ok".equals(result)) {
      processed.put("success", "1");
    } else {
      processed.put("success", "1");

      String[] list = result.split("~");

      for (String line : list) {
        String[] parts = line.split("=", 2);

        if (parts.length > 1) {
          processed.put(parts[0], (parts.length >= 2 ? parts[1] : ""));
        }
      }
    }
    return processed;
  }

  /**
   * Return the name of the VAPIX camera
   *
   * @return String containing the model name of this camera
   */
  @Override
  public String getName() {
    return this.model_name;
  }

  /**
   * Return the camera profile object
   *
   * @return Camera profile
   */
  @Override
  public PTZCameraProfile getProfile() {
    return this.profile;
  }

  /**
   * This function is not implemented, because actual usage not defined in any
   * documentation
   */
  @Override
  public void reset() {
    Logger.debug("Reset (do nothing)");
  }

  /**
   * Cancel the current movement - calls stopMove()
   */
  @Override
  public void cancel() {
    this.stopMove();
  }

  /**
   * Stops the current camera movement if any is in progress
   */
  @Override
  public void stopMove() {

    Logger.debug("Stop movement");

    try {
      String result = this.doCommand("/axis-cgi/com/ptz.cgi?continuouspantiltmove=0,0");
      Logger.trace("Stop movement (" + result + ")");
    } catch (IOException e) {
      Logger.error("stop: " + e.getMessage());
    }
  }

  /**
   * Move the camera to home position - preset (home)
   */
  @Override
  public void moveHome() {

    Logger.debug("Move home");

    try {
      String result = this.doCommand("/axis-cgi/com/ptz.cgi?move=home&speed=100");
      Logger.trace("Move home (" + result + ")");
    } catch (IOException e) {
      Logger.error("moveHome: " + e.getMessage());
    } finally {
    }
  }

  /**
   * Move the camera to a set preset in the preset list using the index of the
   * preset (0 index)
   *
   * @param preset_index
   *            The number of the preset in the preset list to move camera to
   */
  public void movePreset(int preset_index) {

    Logger.debug("Move preset index " + preset_index);

    try {
      String result = this.doCommand("/axis-cgi/com/ptz.cgi?gotoserverpresetno=" + (preset_index + 1));
      Logger.trace("Move preset number {}: {}", preset_index, result);
    } catch (IOException e) {
      Logger.error("movePreset: " + e.getMessage());
    }

  }

  /**
   * Move the camera to a set preset using the name of that preset
   *
   * @param preset
   *            The name of the preset to move the camera to
   */
  public boolean movePreset(String preset) {

    Logger.debug("Move preset name " + preset);

    this.presets = getPresetNames(); // refresh the list of presets

    if (Arrays.asList(presets).contains(preset)) {
      try {
        String result = this.doCommand("/axis-cgi/com/ptz.cgi?gotoserverpresetname=" + preset);
        Logger.trace("Move preset [" + preset + "] (" + result + ")");
      } catch (IOException e) {
        Logger.error("moveHome: " + e.getMessage());
        return false;
      }
    } else {
      Logger.debug("Preset name not found: " + preset);
      return false;
    }

    return true;
  }

  /**
   * Set a preset at the current camera position with the provided name
   *
   * @param name
   *            Name of the preset to store current position
   */
  public void setPreset(String name) {

    try {
      String result = doCommand("/axis-cgi/com/ptzconfig.cgi?setserverpresetname=" + name);
      Logger.trace("setPreset: " + name + "(" + result + ")");
      this.presets = getPresetNames(); // refresh the list of presets
    } catch (IOException e) {
      Logger.error("setPreset: " + e.getMessage());
    } finally {
    }
  }

  /**
   * Remove a preset by name
   *
   * @param name
   *            Remove a preset with the given name from the camera
   */
  public void removePreset(String name) {

    try {
      String result = doCommand("/axis-cgi/com/ptzconfig.cgi?removeserverpresetname=" + name);
      Logger.trace("removePreset: " + name + "(" + result + ")");
      this.presets = getPresetNames(); // refresh the list of presets
    } catch (Exception e) {
      Logger.error("removePreset: " + e.getMessage());
    }
  }

  /**
   * Get a list of all the available presets for this camera
   *
   * @return String array of all the presets on the camera
   */
  private String[] getPresetNames() {

    Hashtable<String, String> list = processCommand("/axis-cgi/com/ptz.cgi?query=presetposall");
    String[] result = new String[list.size() - 1];
    int preset = 0;
    if (list.get("success").equals("1")) {

      for (Entry<String, String> entry : list.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();

        if (key.indexOf("presetposno") >= 0)
          result[preset++] = value;
          Logger.trace("Camera preset {}: {}", key, value);
      }
    }

    return result;
  }

  /**
   * Move the camera in a direction using the set speeds
   *
   * @param panSpeed
   *            speed to pan camera (-100, 100)
   * @param tiltSpeed
   *            speed to tilt camera at (-100, 100)
   */
  public void moveCamera(int panSpeed, int tiltSpeed) {
    if (this.can_move_continuous) {

      try {

        if (panSpeed < -100)
          panSpeed = -100;

        if (panSpeed > 100)
          panSpeed = 100;

        if (tiltSpeed < -100)
          tiltSpeed = -100;

        if (tiltSpeed > 100)
          tiltSpeed = 100;

        String result = this
        .doCommand("/axis-cgi/com/ptz.cgi?continuouspantiltmove=" + panSpeed + "," + tiltSpeed);
        Logger.trace("Move " + panSpeed + ", " + tiltSpeed + " (" + result + ")");
      } catch (IOException e) {
        Logger.error(e.getMessage());
      }

    } else {
      this.moveNotSupported(tiltSpeed);
    }

  }

  /**
   * Tilt the camera up at tiltSpeed
   *
   * @param tiltSpeed
   *            The speed of the tilt motion
   */
  @Override
  public void moveUp(int tiltSpeed) {

    int tilt = Math.round(map(tiltSpeed, speed_tilt.min, scale_tilt_speed, 0));
    moveCamera(0, tilt);
  }

  /**
   * Tilt the camera down at tiltSpeed
   *
   * @param tiltSpeed
   *            The speed of the tilt motion
   */
  @Override
  public void moveDown(int tiltSpeed) {

    int tilt = Math.round(map(tiltSpeed, speed_tilt.min, scale_tilt_speed, 0)) * -1;
    moveCamera(0, tilt);
  }

  /**
   * Pan the camera left at panSpeed
   *
   * @param panSpeed
   *            The speed of the panning motion
   */
  @Override
  public void moveLeft(int panSpeed) {

    int pan = Math.round(map(panSpeed, speed_pan.min, scale_pan_speed, 0)) * -1;
    moveCamera(pan, 0);
  }

  /**
   * Pan the camera right at panSpeed
   *
   * @param panSpeed
   *            The speed of the panning motion
   */
  @Override
  public void moveRight(int panSpeed) {

    int pan = Math.round(map(panSpeed, speed_pan.min, scale_pan_speed, 0));
    moveCamera(pan, 0);
  }

  /**
   * Simultaneously pan and tilt the camera Up and Left at panSpeed and
   * tiltSpeed respectively
   *
   * @param panSpeed
   *            The speed of the panning motion
   * @param tiltSpeed
   *            The speed of the tilt motion
   */
  @Override
  public void moveUpLeft(int panSpeed, int tiltSpeed) {

    int pan = Math.round(map(panSpeed, speed_pan.min, scale_pan_speed, 0)) * -1;
    int tilt = Math.round(map(tiltSpeed, speed_tilt.min, scale_tilt_speed, 0));

    moveCamera(pan, tilt);
  }

  /**
   * Simultaneously pan and tilt the camera Up and Right at panSpeed and
   * tiltSpeed respectively
   *
   * @param panSpeed
   *            The speed of the panning motion
   * @param tiltSpeed
   *            The speed of the tilt motion
   */
  @Override
  public void moveUpRight(int panSpeed, int tiltSpeed) {

    int pan = Math.round(map(panSpeed, speed_pan.min, scale_pan_speed, 0));
    int tilt = Math.round(map(tiltSpeed, speed_tilt.min, scale_tilt_speed, 0));

    moveCamera(pan, tilt);
  }

  /**
   * Simultaneously pan and tilt the camera Down and Left at panSpeed and
   * tiltSpeed respectively
   *
   * @param panSpeed
   *            The speed of the panning motion
   * @param tiltSpeed
   *            The speed of the tilt motion
   */
  @Override
  public void moveDownLeft(int panSpeed, int tiltSpeed) {

    int pan = Math.round(map(panSpeed, speed_pan.min, scale_pan_speed, 0)) * -1;
    int tilt = Math.round(map(tiltSpeed, speed_tilt.min, scale_tilt_speed, 0)) * -1;

    moveCamera(pan, tilt);
  }

  /**
   * Simultaneously pan and tilt the camera Down and Right at panSpeed and
   * tiltSpeed respectively
   *
   * @param panSpeed
   *            The speed of the panning motion
   * @param tiltSpeed
   *            The speed of the tilt motion
   */
  @Override
  public void moveDownRight(int panSpeed, int tiltSpeed) {

    int pan = Math.round(map(panSpeed, speed_pan.min, scale_pan_speed, 0));
    int tilt = Math.round(map(tiltSpeed, speed_tilt.min, scale_tilt_speed, 0)) * -1;

    moveCamera(pan, tilt);
  }

  /**
   * Move to an absolute position - target to move to and the speed to use.
   * VAPIX does not support separate speed setting for pan and tilt so
   * panSpeed is used.
   *
   * @param panSpeed
   *            The speed of the panning motion
   * @param tiltSpeed
   *            The speed of the tilt motion
   * @param target
   *            Target to move camera to at the provided speed
   */
  @Override
  public void moveAbsolute(int panSpeed, int tiltSpeed, Position target) {

    if (this.can_move_absolute) {

      // The VAPIX protocol only has one speed setting for both pan an
      // tilt - so the pan speed is used
      float x = this.map(target.getX(), lim_pan.min, scale_pan, range_pan.min());
      float y = this.map(target.getY(), lim_tilt.min, scale_tilt, range_tilt.min());
      // float z = this.map((int) this.zoom, lim_zoom.min, scale_zoom,
      // range_zoom.min());
      float speed = this.map(panSpeed, speed_pan.min, scale_pan_speed, 0);

      try {
        String result = doCommand("/axis-cgi/com/ptz.cgi?pan=" + range_pan.clamp(x) + "&tilt="
                                  + range_tilt.clamp(y) + "&speed=" + speed_pan.clamp(speed));
        Logger.trace("Move absolute to [" + target.getX() + ";" + target.getY() + "] at [" + speed + "] ("
                     + result + ")");

        Logger.trace("    P:" + x + " (" + target.getX() + "; " + lim_pan.min + "; " + scale_pan + "; "
                     + range_pan.min() + ")");
        Logger.trace("    T:" + y + " (" + target.getY() + "; " + lim_tilt.min + "; " + scale_tilt + "; "
                     + range_tilt.min() + ")");
        Logger.trace(
                     "    S:" + speed + " (" + panSpeed + "; " + speed_pan.min + "; " + scale_pan_speed + "; 0)");
      } catch (IOException e) {
        Logger.error("moveAbsolute: " + e.getMessage());
      }

    } else {
      this.moveNotSupported(panSpeed, tiltSpeed, target);
    }
  }

  /**
   * Move relative position - target to move to and the speed to use. VAPIX
   * does not support separate speed setting for pan and tilt so panSpeed is
   * used.
   *
   * @param panSpeed
   *            The speed of the panning motion
   * @param tiltSpeed
   *            The speed of the tilt motion
   * @param target
   *            Target to move camera to at the provided speed
   */
  @Override
  public void moveRelative(int panSpeed, int tiltSpeed, Position target) {

    if (this.can_move_relative) {

      // The VAPIX protocol only has one speed setting for both pan an
      // tilt - so the pan speed is used
      float x = this.map(target.getX(), lim_pan.min, scale_pan, range_pan.min());
      float y = this.map(target.getY(), lim_tilt.min, scale_tilt, range_tilt.min());
      // float z = this.map((int) this.zoom, lim_zoom.min, scale_zoom,
      // range_zoom.min());
      float speed = this.map(panSpeed, speed_pan.min, scale_pan_speed, 0);

      try {
        String result = doCommand("/axis-cgi/com/ptz.cgi?rpan=" + range_pan.clamp(x) + "&rtilt="
                                  + range_tilt.clamp(y) + "&speed=" + speed_pan.clamp(panSpeed));
        Logger.trace("Move relative to [" + target.getX() + ";" + target.getY() + "] at [" + panSpeed + "] ("
                     + result + ")");

        Logger.trace("    P:" + x + " (" + target.getX() + "; " + lim_pan.min + "; " + scale_pan + "; "
                     + range_pan.min() + ")");
        Logger.trace("    T:" + y + " (" + target.getY() + "; " + lim_tilt.min + "; " + scale_tilt + "; "
                     + range_tilt.min() + ")");
        Logger.trace(
                     "    S:" + speed + " (" + panSpeed + "; " + speed_pan.min + "; " + scale_pan_speed + "; 0)");
      } catch (IOException e) {
        Logger.error("moveRelative: " + e.getMessage());
      }

    } else {
      this.moveNotSupported(panSpeed, tiltSpeed, target);
    }
  }

  /**
   * Clear the camera limits and reset to provided properties
   */
  @Override
  public void clearLimits() {

    Logger.trace("Clear camera limits");
    lim_pan = new Limits(config.getInt(Constants.PROFKEY_PAN_MIN), config.getInt(Constants.PROFKEY_PAN_MAX));
    if (inverted) {
      lim_tilt = new Limits(config.getInt(Constants.PROFKEY_TILT_MAX)*-1, config.getInt(Constants.PROFKEY_TILT_MIN)*-1);
    } else {
      lim_tilt = new Limits(config.getInt(Constants.PROFKEY_TILT_MIN), config.getInt(Constants.PROFKEY_TILT_MAX));
    }
    lim_zoom = new Limits(config.getInt(Constants.PROFKEY_ZOOM_MIN), config.getInt(Constants.PROFKEY_ZOOM_MAX));

    speed_pan = new Limits(0, config.getInt(Constants.PROFKEY_PAN_MAXSPEED));
    speed_tilt = new Limits(0, config.getInt(Constants.PROFKEY_TILT_MAXSPEED));
    speed_zoom = new Limits(0, config.getInt(Constants.PROFKEY_ZOOM_MAXSPEED));

    try {
      String result = doCommand("/axis-cgi/com/ptz.cgi?speed=100");
      Logger.trace("clearLimits: speed=100");
    } catch (IOException e) {
      Logger.error("moveRelative: " + e.getMessage());
    }
  }

  @Override
  public void setLimitUpRight(int pan, int tilt) {

    // TODO
    Logger.debug("Not Implemented: Set limit up-right");

  }

  @Override
  public void setLimitDownLeft(int pan, int tilt) {

    // TODO
    Logger.debug("Not Implemented: Set limit down-left");

  }

  /**
   * Stop the camera zoom operation
   */
  @Override
  public void stopZoom() {

    try {

      if (this.can_move_continuous) {

        String result = doCommand("/axis-cgi/com/ptz.cgi?continuouszoommove=0");
        Logger.trace("Stop zoom " + zoom + " (" + result + ")");
      } else {
        this.moveNotSupported(0);
      }
    } catch (IOException e) {
      Logger.error("stopZoom: " + e.getMessage());
    }
  }

  /**
   * Zoom in the camera at this speed.
   *
   * @param speed
   *            speed to zoom
   */
  @Override
  public void zoomIn(int speed) {

    Logger.debug("Zoom to " + zoom);

    try {

      if (this.can_move_continuous) {

        speed = (new Limits(0, lim_zoom.max())).clamp(speed);

        if (speed > 100)
          speed = 100;

        String result = doCommand("/axis-cgi/com/ptz.cgi?continuouszoommove=" + speed);
        Logger.trace("Zoom in [" + speed + "] (" + result + ")");
      } else {
        this.moveNotSupported(speed);
      }
    } catch (IOException e) {
      Logger.error("zoomIn: " + e.getMessage());
    }
  }

  /**
   * Zoom out the camera at this speed.
   *
   * @param speed
   *            speed to zoom
   */
  @Override
  public void zoomOut(int speed) {

    try {

      if (this.can_move_continuous) {

        // negative values zoom out
        speed = (new Limits(0, lim_zoom.max())).clamp(speed) * -1;

        if (speed < -100)
          speed = -100;

        String result = doCommand("/axis-cgi/com/ptz.cgi?continuouszoommove=" + speed);
        Logger.trace("Zoom out [" + speed + "] (" + result + ")");
      } else {
        this.moveNotSupported(speed);
      }
    } catch (IOException e) {
      Logger.error("zoomOut: " + e.getMessage());
    }
  }

  /**
   * Set the zoom level of the camera
   *
   * @param zoom
   *            set the zoom to this level
   */
  @Override
  public void zoom(int zoom) {

    Logger.debug("Zoom to " + zoom);

    try {
      String result = doCommand("/axis-cgi/com/ptz.cgi?zoom=" + zoom);
      Logger.trace("Zoom to " + zoom + " (" + result + ")");
    } catch (IOException e) {
      Logger.error("zoom: " + e.getMessage());
    }
  }

  /**
   * Return the value of the cameras current zoom level
   *
   * @return Value of the current zoom level
   */
  @Override
  public int getZoom() {

    Logger.trace("get zoom" + this.zoom);
    return this.zoom;
  }

  /**
   * Set the focus value for the camera
   *
   * @param new_focus
   *            The focus level to set the camera to
   */
  @Override
  public void focus(int new_focus) {

    try {
      String result = doCommand("/axis-cgi/com/ptz.cgi?focus=" + new_focus);
      Logger.trace("Set focus to " + new_focus + " (" + result + ")");
    } catch (IOException e) {
      Logger.error("focus: " + e.getMessage());
    }
  }

  /**
   * Return the focus value for the camera
   *
   * @return Value of the current focus level
   */
  @Override
  public int getFocus() {
    Logger.trace("Get focus " + focus);
    return focus;
  }

  /**
   * Focus mode
   */
  @Override
  public void focusMode(FocusMode mode) {
    // not supported
  }

  /**
   * Return the current position of the camera
   *
   * @return Position of camera.
   */
  @Override
  public Position getPosition() {
    /*
     * pan=85.0781 tilt=87.9883 zoom=460 iris=6784 focus=2393
     * brightness=4999 autofocus=on autoiris=on
     */
    Hashtable<String, String> result = processCommand("/axis-cgi/com/ptz.cgi?query=position");

    if (result.get("success").equals("1")) {
      int x = Math.round(this.map(Float.parseFloat(result.get("pan")), range_pan.min, scale_pan_rev, lim_pan.min));
      int y = Math.round(this.map(Float.valueOf(result.get("tilt")), range_tilt.min, scale_tilt_rev, lim_tilt.min));
      int z = Math.round(this.map(Float.valueOf(result.get("zoom")), range_zoom.min, scale_zoom_rev, lim_zoom.min));

      zoom = z;
      focus = Integer.parseInt(result.get("focus"));

      Logger.trace("getPosition: " + x + ";" + y + ";" + z);
      return new Position(x, y);
    }

    return new Position();
  }

  @Override
  public List<Preset> getPresets() {

    Logger.debug("Fetching camera preset positions");

    ArrayList<Preset> presetList = new ArrayList<>();

    Hashtable<String, String> result = processCommand("/axis-cgi/admin/param.cgi?action=list&group=PTZ.Preset.P0.Position");
    if (!result.get("success").equals("1")) {
      Logger.warn("Unable to fetch camera preset positions");
      return presetList;
    }

    /*
     * Results are pairs of .Data and .Name like this:
     *   root.PTZ.Preset.P0.Position.P1.Data=tilt=24.628906:focus=32766.000000:pan=-1.425781:iris=32766.000000:zoom=154.000000
     *   root.PTZ.Preset.P0.Position.P1.Name=Home
     * But if the camera is inverted, then we have to swap the sign of the pan and tilt values.
     */

    String presetPatternRe = "root\\.PTZ\\.Preset\\.P0\\.Position\\.(P[0-9]+)\\.Name";
    String dataPatternRe = "tilt=([0-9.-]+):focus=([0-9.]+):pan=([0-9.-]+):iris=([0-9.-]+):zoom=([0-9.-]+)$";

    Pattern presetPattern = Pattern.compile(presetPatternRe);
    Pattern dataPattern = Pattern.compile(dataPatternRe);

    for (String presetInfo : result.keySet()) {
      if (presetInfo.endsWith(".Name")) {
        String presetName = result.get(presetInfo);
        Matcher matcher = presetPattern.matcher(presetInfo);
        if (matcher.matches()) {
          String presetNumber = matcher.group(1);
          String dataPrefix = "root.PTZ.Preset.P0.Position." + presetNumber+ ".Data";
          String dataLine = result.get(dataPrefix);
          Matcher dataMatcher = dataPattern.matcher(dataLine);
          if (dataMatcher.matches()) {
            int x = Math.round(this.map(Float.parseFloat(dataMatcher.group(3)), range_pan.min, scale_pan_rev, lim_pan.min)) * (inverted ? -1 : 1);
            int y = Math.round(this.map(Float.valueOf(dataMatcher.group(1)), range_tilt.min, scale_tilt_rev, lim_tilt.min)) * (inverted ? -1 : 1);
            int z = Math.round(this.map(Float.valueOf(dataMatcher.group(5)), range_zoom.min, scale_zoom_rev, lim_zoom.min));
            Preset p = new Preset(presetName, x, y, z);
            Logger.debug("Camera has preset {}", p);
            presetList.add(p);
          } else {
            Logger.warn("Unexpected format for preset data string: {}", dataLine);
          }
        } else {
          Logger.warn("Unexpected format for preset response key: {}", presetInfo);
        }
      }
    }

    return presetList;
  }

  /**
   * Add camera listener to list of observers
   *
   * @param listener
   *            CameraListener to add to this cameras list to notify
   */
  @Override
  public void addCameraListener(CameraListener listener) {
    if (listener != null) {
      observers.add(listener);
    } else {
      Logger.warn("VAPIX: null CameraListener");
    }
  }

  /**
   * Remove the camera listener from the list of observers
   *
   * @param listener
   *            CameraListener to remove from list to notify
   */
  @Override
  public void removeCameraListener(CameraListener listener) {
    if (listener != null) {
      observers.remove(listener);
    }
  }

  /**
   * Notify all camera listeners of cameras current position
   */
  void notifyCameraListeners() {
    Position pos = getPosition();
    for (CameraListener cl : observers) {
      if (cl != null)
        cl.positionUpdated(pos);
    }
  }

  /**
   * Map a value as sent through from camera operator to the range defined by
   * the config and then to camera co-ordinates
   *
   * @param val
   *            value to map to the scaled range
   * @param in_min
   *            minimum input value
   * @param delta
   *            delta scale value difference between input and output
   * @param out_min
   *            minimum output value
   * @return mapped value
   */
  private float map(int val, int in_min, float delta, float out_min) {
    float x = (float) (val - in_min);
    return (x * delta) + out_min;
  }

  /**
   * Map a value as sent through from camera operator to the range defined by
   * the config and then to camera co-ordinates
   *
   * @param val
   *            value to map to the scaled range
   * @param in_min
   *            minimum input value
   * @param delta
   *            delta scale value difference between input and output
   * @param out_min
   *            minimum output value
   * @return mapped value
   */
  private float map(float val, int in_min, float delta, float out_min) {
    float x = val - (float) in_min;
    return (x * delta) + out_min;
  }

  /**
   * Display error message for a move that is not supported
   *
   * @param a
   *            Value of input
   */
  private void moveNotSupported(int a) {
    Logger.warn("Move not supported:" + this.can_move_absolute + ":" + this.can_move_relative + ":"
                + this.can_move_continuous + " (" + a + ")");
  }

  /**
   * Display error message for a move that is not supported
   *
   * @param a
   *            Value of input
   * @param b
   *            Value of input
   */
  private void moveNotSupported(int a, int b) {
    Logger.warn("Move not supported:" + this.can_move_absolute + ":" + this.can_move_relative + ":"
                + this.can_move_continuous + " (" + a + "," + b + ")");
  }

  /**
   * Display error message for a move that is not supported
   *
   * @param a
   *            Value of input
   * @param b
   *            Value of input
   * @param target
   *            Value of target for move
   */
  private void moveNotSupported(int a, int b, Position target) {
    Logger.warn("Move not supported:" + this.can_move_absolute + ":" + this.can_move_relative + ":"
                + this.can_move_continuous + " (" + a + "," + b + ")" + " to [" + target.getX() + ";" + target.getY()
                + "]");
  }

  /**
   * Class to manage Authentication of HTTP requests
   *
   * @author Corne
   */
  class MyAuthenticator extends Authenticator {
    String user;
    String password;

    MyAuthenticator(String username, String password) {
      this.user = username;
      this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
      String promptString = getRequestingPrompt();
      Logger.trace(promptString);
      String hostname = getRequestingHost();
      Logger.trace(hostname);
      InetAddress ipaddr = getRequestingSite();
      Logger.trace(ipaddr);
      int port = getRequestingPort();

      return new PasswordAuthentication(user, password.toCharArray());
    }
  }

  /**
   * Threads that frequently sends inquiries to all registered cameras.
   *
   */
  class CameraStateUpdater implements Runnable {

    @Override
    public void run() {

      try {
        Logger.trace("Requesting camera position update");
        notifyCameraListeners();
      } catch (Exception e) {
        throw new IllegalStateException("Exception running camera state updater", e);
      }
    }
  }

}
