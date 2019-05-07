package cv.lecturesight.onvif.service;

import cv.lecturesight.ptz.api.CameraListener;
import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.ptz.api.PTZCameraException;
import cv.lecturesight.ptz.api.PTZCameraProfile;
import cv.lecturesight.util.geometry.Position;
import cv.lecturesight.util.conf.Configuration;

import cv.lecturesight.util.geometry.Preset;
import de.onvif.soap.OnvifDevice;
import de.onvif.soap.devices.InitialDevices;
import de.onvif.soap.devices.PtzDevices;

import java.net.ConnectException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.xml.soap.SOAPException;

import org.onvif.ver10.device.wsdl.GetDeviceInformationResponse;
import org.onvif.ver10.schema.FloatRange;
import org.onvif.ver10.schema.PTZPreset;
import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;

import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

/**
 * Open Network Video Interface Forum (ONVIF: http://www.onvif.org/) is a
 * community to standardize communication between IP-based security products, in
 * this case PTZ cameras.
 * 
 * This camera implementation is based around the onvif wrapper classes written
 * by Milgo and available on GitHub at: https://github.com/milg0/onvif-java-lib
 * 
 * The communication with the camera is defined as a web service, the wrapper
 * handles most of the Simple Object Access Protocol (SOAP) messaging by using
 * Java Architecture for XML Binding (JAXB) to map the objects to Extensible
 * Markup Language (XML).
 * 
 * The Web Service Definition Language (WSDL) for the different versions and
 * devices: Device Management: 1.0 :
 * http://www.onvif.org/ver10/device/wsdl/devicemgmt.wsdl 2.0 :
 * http://www.onvif.org/ver20/ptz/wsdl/ptz.wsdl
 * 
 * Media: 1.0 : http://www.onvif.org/ver10/media/wsdl/media.wsdl 2.0 :
 * http://www.onvif.org/ver20/media/wsdl/media.wsdl
 * 
 * PTZ: 1.0 : http://www.onvif.org/onvif/ver10/ptz/wsdl/ptz.wsdl 2.0 :
 * http://www.onvif.org/ver20/ptz/wsdl/ptz.wsdl
 * 
 * All: 2.0 : http://www.onvif.org/onvif/ver20/util/operationIndex.html
 * 
 * @author Corne Oosthuizen (CILT - UCT) [2015-12]
 */
public class ONVIFCameraImpl implements PTZCamera {

	@Setter
	Configuration config; // service configuration

	String model_name;
	String profile_token; // unique identifier to communicate with ONVIF camera

	CameraState state;

	OnvifDevice onvif_device; // the connection to the onvif device
	PTZCameraProfile profile;
	PtzDevices ptz_device; // the control of ptz functions

	List<PTZPreset> presets;
	List<CameraListener> observers;

	Limits lim_pan, lim_tilt, lim_zoom;
	Limits speed_pan, speed_tilt, speed_zoom;

	FloatRange range_pan, range_tilt, range_zoom;

	// Scale values are used to scale inputs to corrected output values
	float scale_pan = 0f, scale_tilt = 0f, scale_zoom = 0f;
	float scale_pan_rev = 0f, scale_tilt_rev = 0f, scale_zoom_rev = 0f;
	float scale_pan_speed = 0f, scale_tilt_speed = 0f;
	float zoom;

	boolean can_move_absolute = false;
	boolean can_move_relative = false;
	boolean can_move_continuous = false;

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
	 * Initialize this ONVIF Camera service object and load all the appropriate
	 * details from properties.
	 *
	 * @param cc
	 *            ComponentContext
	 * @throws cv.lecturesight.ptz.api.PTZCameraException
	 */
	protected void activate(ComponentContext cc) throws PTZCameraException {
		this.state = new CameraState();
		this.last_update = 0l;

		this.model_name = "ONVIF Camera";

		// Load properties
		String host_name = config.get(Constants.PROPKEY_ONVIF_CAMERA);
		String username = config.get(Constants.PROPKEY_ONVIF_USERNAME);
		String password = config.get(Constants.PROPKEY_ONVIF_PASSWORD);

		updateInterval = config.getInt(Constants.PROPKEY_UPDATER_INTERVAL);

		lim_pan = new Limits(config.getInt(Constants.PROFKEY_PAN_MIN), config.getInt(Constants.PROFKEY_PAN_MAX));
		lim_tilt = new Limits(config.getInt(Constants.PROFKEY_TILT_MIN), config.getInt(Constants.PROFKEY_TILT_MAX));
		lim_zoom = new Limits(config.getInt(Constants.PROFKEY_ZOOM_MIN), config.getInt(Constants.PROFKEY_ZOOM_MAX));

		speed_pan = new Limits(0, config.getInt(Constants.PROFKEY_PAN_MAXSPEED));
		speed_tilt = new Limits(0, config.getInt(Constants.PROFKEY_TILT_MAXSPEED));
		speed_zoom = new Limits(0, config.getInt(Constants.PROFKEY_ZOOM_MAXSPEED));

		if (host_name.length() > 0) {

			Logger.info("ONVIF connecting to " + host_name + " [ " + username + " : " + password + " ]");

			try {

				this.onvif_device = new OnvifDevice(host_name, username, password);

				if (this.onvif_device != null) {

					List<org.onvif.ver10.schema.Profile> profiles = onvif_device.getDevices().getProfiles();
					this.profile_token = profiles.get(0).getToken();

					this.ptz_device = onvif_device.getPtz();
					this.presets = this.getCameraPresets();

					InitialDevices initial_Device = onvif_device.getDevices();

					GetDeviceInformationResponse response = initial_Device.getDeviceInformation();
					this.model_name = response.getModel();

					if (this.ptz_device.isPtzOperationsSupported(profile_token)) {

						this.can_move_absolute = this.ptz_device.isAbsoluteMoveSupported(profile_token);
						this.can_move_relative = this.ptz_device.isRelativeMoveSupported(profile_token);
						this.can_move_continuous = this.ptz_device.isContinuosMoveSupported(profile_token);

						this.range_pan = this.ptz_device.getPanSpaces(profile_token);
						this.range_tilt = this.ptz_device.getTiltSpaces(profile_token);
						this.range_zoom = this.ptz_device.getZoomSpaces(profile_token);

						this.scale_pan = (float) (this.range_pan.getMax() - this.range_pan.getMin())
								/ (float) (lim_pan.max - lim_pan.min);
						this.scale_tilt = (float) (this.range_tilt.getMax() - this.range_tilt.getMin())
								/ (float) (lim_tilt.max - lim_tilt.min);
						this.scale_zoom = (float) (this.range_zoom.getMax() - this.range_zoom.getMin())
								/ (float) (lim_zoom.max - lim_zoom.min);

						this.scale_pan_rev = (float) (lim_pan.max - lim_pan.min)
								/ (float) (this.range_pan.getMax() - this.range_pan.getMin());
						this.scale_tilt_rev = (float) (lim_tilt.max - lim_tilt.min)
								/ (float) (this.range_tilt.getMax() - this.range_tilt.getMin());
						this.scale_zoom_rev = (float) (lim_zoom.max - lim_zoom.min)
								/ (float) (this.range_zoom.getMax() - this.range_zoom.getMin());

						// Documented max speed for pan and tilt is 1
						this.scale_pan_speed = (float) (1 / (float) (speed_pan.max - speed_pan.min));
						this.scale_tilt_speed = (float) (1 / (float) (speed_tilt.max - speed_tilt.min));

						Logger.trace("Profile: " + "Pan: " + lim_pan + " " + speed_pan + " " + range_pan + " | Tilt: "
								+ lim_tilt + " " + speed_tilt + " " + range_tilt + " | Zoom: " + lim_zoom + " "
								+ speed_zoom + " " + range_zoom);

						Logger.trace(" Scalar: " + scale_pan + " " + scale_tilt + " " + scale_zoom + " | "
								+ scale_pan_rev + " " + scale_tilt_rev + " " + scale_zoom_rev + " | " + scale_pan_speed
								+ " " + scale_tilt_speed);

						this.profile = new PTZCameraProfile(response.getManufacturer(), response.getModel(),
								lim_pan.min, lim_pan.max, speed_pan.max, lim_tilt.min, lim_tilt.max, speed_tilt.max,
								lim_zoom.min, lim_zoom.max, speed_zoom.max, getPosition());
					} else {

						this.profile = new PTZCameraProfile(response.getManufacturer(), // manufacturer
								response.getModel(), // should be "ONVIF Camera"
								0, 0, 0, // pan min, max, speed
								0, 0, 0, // tilt min, max, speed
								0, 0, 0, // zoom min, max, speed
								new Position(0, 0));
					}

					this.observers = new LinkedList<CameraListener>();

					// Start update thread
					updaterHandle = executor.scheduleWithFixedDelay(new CameraStateUpdater(), updateInterval,
							updateInterval, TimeUnit.MILLISECONDS);

				} else {
					String msg = "Could not connect to camera - null object.";
					Logger.debug(msg);
					throw new PTZCameraException(msg);
				}

			} catch (ConnectException e) {

				String msg = "Could not connect to ONVIF Camera - connection error.";
				Logger.debug(msg);

				throw new PTZCameraException(msg);
			} catch (SOAPException e) {

				e.printStackTrace();
				String msg = "SOAPException during service activation.";
				Logger.debug(msg);

				throw new PTZCameraException(msg);
			}
		} else {

			String msg = "Could not connect to ONVIF Camera - no ip address.";
			Logger.debug(msg);

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
	}

	/**
	 * Return the name of the ONVIF camera
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

		Logger.trace("Stop movement");

		if (this.ptz_device != null) {
			this.ptz_device.stopMove(this.profile_token);
		}
	}

	/**
	 * Move the camera to home position
	 */
	@Override
	public void moveHome() {

		Logger.trace("Move Home");

		if (this.ptz_device != null) {
			this.ptz_device.moveHome(this.profile_token);
		}
	}

	/**
	 * Move the camera to a set preset in the preset list using the index of the
	 * preset
	 * 
	 * @param preset_index
	 *            The number of the preset in the preset list to move camera to
	 */
	private void movePreset(int preset_index) {

		if (this.presets != null && !this.presets.isEmpty() && (this.presets.size() < preset_index)
				&& (preset_index >= 0)) {

			PTZPreset current = this.presets.get(preset_index);
			Vector2D pan_tilt = current.getPTZPosition().getPanTilt();
			Vector1D zoom = current.getPTZPosition().getZoom();

			try {
				Logger.trace("Move preset index [" + preset_index + "]");
				this.ptz_device.absoluteMove(this.profile_token, pan_tilt.getX(), pan_tilt.getY(), zoom.getX());
			} catch (SOAPException e) {
				Logger.error("movePreset: " + e.getMessage());
			}
		}
	}

	/**
	 * Move the camera to a set preset using the name of that preset
	 *
	 * @param preset
	 *            The name of the preset to move the camera to
	 */
	@Override
	public boolean movePreset(String preset) {

		PTZPreset found = null;

		for (int i = 0; i < this.presets.size(); i++) {
			PTZPreset now = (PTZPreset) this.presets.get(i);

			if (now.getName().equals(preset))
				found = now;
		}

		if (found != null) {

			Vector2D pan_tilt = found.getPTZPosition().getPanTilt();
			Vector1D zoom = found.getPTZPosition().getZoom();

			try {
				Logger.trace("Move preset " + preset);
				this.ptz_device.absoluteMove(this.profile_token, pan_tilt.getX(), pan_tilt.getY(), zoom.getX());
				return true;
			} catch (SOAPException e) {
				Logger.error("movePreset: " + e.getMessage());
			}
		} else {
			Logger.debug("Preset not found: " + preset);
		}

		return false;
	}

	/**
	 * Tilt the camera up at tiltSpeed
	 * 
	 * @param tiltSpeed
	 *            The speed of the tilt motion
	 */
	@Override
	public void moveUp(int tiltSpeed) {

		if ((this.ptz_device != null) && this.can_move_continuous) {

			float b = Limits.normalize((float) tiltSpeed / (float) this.speed_tilt.max);

			Logger.trace("Move up speed " + tiltSpeed + "[" + b + "]");
			this.ptz_device.continuousMove(this.profile_token, 0f, b, 0f);
		} else {
			this.moveNotSupported(tiltSpeed);
		}
	}

	/**
	 * Tilt the camera down at tiltSpeed
	 * 
	 * @param tiltSpeed
	 *            The speed of the tilt motion
	 */
	@Override
	public void moveDown(int tiltSpeed) {

		if ((this.ptz_device != null) && this.can_move_continuous) {

			float b = Limits.normalize((float) tiltSpeed / (float) this.speed_tilt.max * -1);

			Logger.trace("Move down speed " + tiltSpeed + "[" + b + "]");
			this.ptz_device.continuousMove(this.profile_token, 0f, b, 0f);
		} else {
			this.moveNotSupported(tiltSpeed);
		}
	}

	/**
	 * Pan the camera left at panSpeed
	 * 
	 * @param panSpeed
	 *            The speed of the panning motion
	 */
	@Override
	public void moveLeft(int panSpeed) {

		if ((this.ptz_device != null) && this.can_move_continuous) {

			float a = Limits.normalize((float) panSpeed / (float) this.speed_pan.max * -1);

			Logger.trace("Move left speed " + panSpeed + "[" + a + "]");
			this.ptz_device.continuousMove(this.profile_token, a, 0f, 0f);
		} else {
			this.moveNotSupported(panSpeed);
		}
	}

	/**
	 * Pan the camera right at panSpeed
	 * 
	 * @param panSpeed
	 *            The speed of the panning motion
	 */
	@Override
	public void moveRight(int panSpeed) {

		if ((this.ptz_device != null) && this.can_move_continuous) {

			float a = Limits.normalize((float) panSpeed / (float) this.speed_pan.max);

			Logger.trace("Move right speed " + panSpeed + "[" + a + "]");
			this.ptz_device.continuousMove(this.profile_token, a, 0f, 0f);
		} else {
			this.moveNotSupported(panSpeed);
		}
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

		if ((this.ptz_device != null) && this.can_move_continuous) {

			float a = Limits.normalize((float) panSpeed / (float) this.speed_pan.max * -1);
			float b = Limits.normalize((float) tiltSpeed / (float) this.speed_tilt.max);

			Logger.trace("Move up-left " + panSpeed + "/" + tiltSpeed + "[" + a + ", " + b + "]");
			this.ptz_device.continuousMove(this.profile_token, a, b, 0f);
		} else {
			this.moveNotSupported(panSpeed, tiltSpeed);
		}
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

		if ((this.ptz_device != null) && this.can_move_continuous) {

			float a = Limits.normalize((float) panSpeed / (float) this.speed_pan.max);
			float b = Limits.normalize((float) tiltSpeed / (float) this.speed_tilt.max);

			Logger.trace("Move up-right " + panSpeed + "/" + tiltSpeed + "[" + a + ", " + b + "]");
			this.ptz_device.continuousMove(this.profile_token, a, b, 0f);
		} else {
			this.moveNotSupported(panSpeed, tiltSpeed);
		}
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

		if ((this.ptz_device != null) && this.can_move_continuous) {

			float a = Limits.normalize((float) panSpeed / (float) this.speed_pan.max * -1);
			float b = Limits.normalize((float) tiltSpeed / (float) this.speed_tilt.max * -1);

			Logger.trace("Move down-left " + panSpeed + "/" + tiltSpeed + "[" + a + ", " + b + "]");
			this.ptz_device.continuousMove(this.profile_token, a, b, 0f);
		} else {
			this.moveNotSupported(panSpeed, tiltSpeed);
		}
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

		if ((this.ptz_device != null) && this.can_move_continuous) {

			float a = Limits.normalize((float) panSpeed / (float) this.speed_pan.max);
			float b = Limits.normalize((float) tiltSpeed / (float) this.speed_tilt.max * -1);

			Logger.trace("Move down-right " + panSpeed + "/" + tiltSpeed + "[" + a + ", " + b + "]");
			this.ptz_device.continuousMove(this.profile_token, a, b, 0f);
		} else {
			this.moveNotSupported(panSpeed, tiltSpeed);
		}
	}

	/**
	 * Move to an absolute position - target to move to and the speed to use
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

		if ((this.ptz_device != null) && this.can_move_absolute) {

			float a = Limits.normalize((float) panSpeed / (float) this.speed_pan.max);
			float b = Limits.normalize((float) tiltSpeed / (float) this.speed_tilt.max);

			float x = this.map(target.getX(), lim_pan.min, scale_pan, -1);
			float y = this.map(target.getY(), lim_tilt.min, scale_tilt, -1);
			float z = this.map((int) this.zoom, lim_zoom.min, scale_zoom, -1);

			try {
				Logger.trace("Move absolute to [" + target.getX() + ";" + target.getY() + "] at [" + a + "/" + b + "]");
				Logger.trace("    P:" + x + " (" + target.getX() + "; " + lim_pan.min + "; " + scale_pan + "; -1)");
				Logger.trace("    T:" + y + " (" + target.getY() + "; " + lim_tilt.min + "; " + scale_tilt + "; -1)");
				Logger.trace("    Z:" + z + " (" + this.zoom + "; " + lim_zoom.min + "; " + scale_zoom + "; 0)");

				this.ptz_device.absoluteMove(this.profile_token, x, y, z, a, b, 1f);
			} catch (SOAPException e) {
				Logger.error("moveAbsolute: " + e.getMessage());
			}
		} else {
			this.moveNotSupported(panSpeed, tiltSpeed);
		}
	}

	/**
	 * Move to a relative position - target to move to and the speed to use
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

		if ((this.ptz_device != null) && this.can_move_relative) {

			float a = Limits.normalize((float) panSpeed / (float) this.speed_pan.max);
			float b = Limits.normalize((float) tiltSpeed / (float) this.speed_tilt.max);

			float x = this.map(target.getX(), lim_pan.min, scale_pan, -1);
			float y = this.map(target.getY(), lim_tilt.min, scale_tilt, -1);
			float z = this.map((int) this.zoom, lim_zoom.min, scale_zoom, -1);

			try {
				Logger.trace("Move relative to [" + target.getX() + ";" + target.getY() + "] at [" + a + "/" + b + "]");
				Logger.trace("    P:" + x + " (" + target.getX() + "; " + lim_pan.min + "; " + scale_pan + "; -1)");
				Logger.trace("    T:" + y + " (" + target.getY() + "; " + lim_tilt.min + "; " + scale_tilt + "; -1)");
				Logger.trace("    Z:" + z + " (" + this.zoom + "; " + lim_zoom.min + "; " + scale_zoom + "; 0)");

				this.ptz_device.relativeMove(this.profile_token, x, y, z, a, b, 1f);
			} catch (SOAPException e) {
				Logger.error("moveAbsolute: " + e.getMessage());
			}
		} else {
			this.moveNotSupported(panSpeed, tiltSpeed);
		}
	}

	@Override
	public void clearLimits() {

		// TODO
		Logger.debug("Not Implemented: Clear camera limits");

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
	 * Set a preset at the current camera position with the provided name
	 * 
	 * @param name
	 *            Name of the preset to store current position
	 */
	public void setPreset(String name) {
		try {
			Logger.trace("setPreset: " + name);
			this.ptz_device.setPreset(name, this.profile_token);
			this.presets = this.getCameraPresets();
		} catch (Exception e) {
			Logger.error("setPreset: " + e.getMessage());
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
			Logger.trace("removePreset: " + name);
			this.ptz_device.removePreset(name, this.profile_token);
			this.presets = this.getCameraPresets();
		} catch (Exception e) {
			Logger.error("removePreset: " + e.getMessage());
		}
	}

	/**
	 * Get a list of all the available presets for this camera
	 * 
	 * @return String array of all the presets on the camera
	 */
	private List<PTZPreset> getCameraPresets() {

		try {
			Logger.trace("getPresets");
			return this.ptz_device.getPresets(profile_token);
		} catch (Exception e) {
			Logger.error("getPresets: " + e.getMessage());
		}
		
		return null;
	}

	/**
	 * Stop the camera zoom operation
	 */
	@Override
	public void stopZoom() {

		Logger.debug("Stop zoom (" + zoom + ").");

		if (this.ptz_device != null) {

			// the stop move method stops the camera movement
			// but accordingly only the zoom action
			this.ptz_device.stopMove(this.profile_token, false, true);
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

		try {

			if ((this.ptz_device != null) && this.can_move_continuous) {

				float z = Limits.normalize((float) speed / (float) this.speed_zoom.max, true);

				Logger.trace("Zoom In " + speed + "[" + z + "]");
				this.ptz_device.continuousZoom(this.profile_token, z);
			} else {
				this.moveNotSupported(speed);
			}
		} catch (Exception e) {
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

			if ((this.ptz_device != null) && this.can_move_continuous) {

				float z = Limits.normalize((float) speed / (float) this.speed_zoom.max * -1, true);

				Logger.trace("Zoom Out " + speed + "[" + z + "]");
				this.ptz_device.continuousZoom(this.profile_token, z);
			} else {
				this.moveNotSupported(speed);
			}
		} catch (Exception e) {
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

		if ((this.ptz_device != null) && this.can_move_absolute) {

			float z = this.map((int) zoom, lim_zoom.min, scale_zoom, -1);

			try {
				Logger.trace("Zoom to [" + zoom + "]");
				Logger.trace("    Z:" + z + " (" + zoom + "; " + lim_zoom.min + "; " + scale_zoom + "; 0)");

				if (this.ptz_device.absoluteZoom(this.profile_token, z)) {
				}
			} catch (SOAPException e) {
				Logger.error("zoom: " + e.getMessage());
			}
		} else {
			this.moveNotSupported(zoom);
		}
	}

	/**
	 * Return the value of the cameras current zoom level
	 * 
	 * @return Value of the current zoom level
	 */
	@Override
	public int getZoom() {
		return (int) this.zoom;
	}

	/**
	 * Return the current position of the camera
	 * 
	 * @return Position of camera.
	 */
	@Override
	public Position getPosition() {

		try {
			Logger.debug("request position");
			PTZVector vector = this.ptz_device.getPosition(this.profile_token);

			int x = (int) this.map(vector.getPanTilt().getX(), -1, scale_pan_rev, lim_pan.min);
			int y = (int) this.map(vector.getPanTilt().getY(), -1, scale_tilt_rev, lim_tilt.min);
			int z = (int) this.map(vector.getZoom().getX(), -1, scale_zoom_rev, lim_zoom.min);

			this.zoom = z;

			Logger.trace("getPosition: " + x + ";" + y + ";" + z);
			return new Position(x, y);
		} catch (Exception e) {
			Logger.error("getPosition: " + e.getMessage());
		}

		return null;
	}

	@Override
	public List<Preset> getPresets() {
		// not supported yet
		return Collections.emptyList();
	}

	/**
	 * TODO: http://www.onvif.org/ver20/imaging/wsdl
	 */
	@Override
	public void focus(int new_focus) {
		Logger.trace("Not Implemented: set focus to " + new_focus);
	}

	/**
	 * Return the focus of the camera
	 * TODO
	 * @return focus level of camera
	 */
	@Override
	public int getFocus() {
		int focus = 0;

		Logger.trace("get focus" + focus);
		return focus;
	}

       /**
         * TODO Focus mode
         */
        @Override
        public void focusMode(FocusMode mode) {
		Logger.trace("Not Implemented: set focus mode to " + mode);
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
			Logger.warn("ONVIF: null CameraListener");
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
	 * the config and then to onvif SOAP (val - in_min) * (out_max - out_min) /
	 * (in_max - in_min) + out_min
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
		// return (val - in_min) * (out_max - out_min) / (in_max - in_min) +
		// out_min;
		float x = (float) (val - in_min);
		return (x * delta) + out_min;
	}

	/**
	 * Map a value as sent through from camera operator to the range defined by
	 * the config and then to onvif SOAP (val - in_min) * (out_max - out_min) /
	 * (in_max - in_min) + out_min
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
		Logger.warn("Move not supported [" + (this.ptz_device != null ? "1" : "0") + ":" + this.can_move_absolute + ":"
				+ this.can_move_relative + ":" + this.can_move_continuous + "](" + a + ")");
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
		Logger.warn("Move not supported [" + (this.ptz_device != null ? "1" : "0") + ":" + this.can_move_absolute + ":"
				+ this.can_move_relative + ":" + this.can_move_continuous + "](" + a + ", " + b + ")");
	}

	/**
	 * Threads that frequently sends inquiries to all registered cameras.
	 *
	 */
	class CameraStateUpdater implements Runnable {

		@Override
		public void run() {

			try {
				Logger.trace("ONVIF: Requesting camera position update");
				notifyCameraListeners();
			} catch (Exception e) {
				throw new IllegalStateException("Exception running camera state updater", e);
			}
		}
	}

}
