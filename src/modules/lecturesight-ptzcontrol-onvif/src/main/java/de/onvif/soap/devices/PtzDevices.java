package de.onvif.soap.devices;

import java.net.ConnectException;
import java.util.List;

import javax.xml.soap.SOAPException;

import org.onvif.ver10.schema.FloatRange;
import org.onvif.ver10.schema.PTZConfiguration;
import org.onvif.ver10.schema.PTZNode;
import org.onvif.ver10.schema.PTZPreset;
import org.onvif.ver10.schema.PTZSpaces;
import org.onvif.ver10.schema.PTZSpeed;
import org.onvif.ver10.schema.PTZStatus;
import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Profile;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;

import org.onvif.ver20.ptz.wsdl.AbsoluteMove;
import org.onvif.ver20.ptz.wsdl.AbsoluteMoveResponse;
import org.onvif.ver20.ptz.wsdl.ContinuousMove;
import org.onvif.ver20.ptz.wsdl.ContinuousMoveResponse;
import org.onvif.ver20.ptz.wsdl.GetNode;
import org.onvif.ver20.ptz.wsdl.GetNodeResponse;
import org.onvif.ver20.ptz.wsdl.GetNodes;
import org.onvif.ver20.ptz.wsdl.GetNodesResponse;
import org.onvif.ver20.ptz.wsdl.GetPresets;
import org.onvif.ver20.ptz.wsdl.GetPresetsResponse;
import org.onvif.ver20.ptz.wsdl.GetStatus;
import org.onvif.ver20.ptz.wsdl.GetStatusResponse;
import org.onvif.ver20.ptz.wsdl.GotoHomePosition;
import org.onvif.ver20.ptz.wsdl.GotoHomePositionResponse;
import org.onvif.ver20.ptz.wsdl.GotoPreset;
import org.onvif.ver20.ptz.wsdl.GotoPresetResponse;
import org.onvif.ver20.ptz.wsdl.RelativeMove;
import org.onvif.ver20.ptz.wsdl.RelativeMoveResponse;
import org.onvif.ver20.ptz.wsdl.RemovePreset;
import org.onvif.ver20.ptz.wsdl.RemovePresetResponse;
import org.onvif.ver20.ptz.wsdl.SetHomePosition;
import org.onvif.ver20.ptz.wsdl.SetHomePositionResponse;
import org.onvif.ver20.ptz.wsdl.SetPreset;
import org.onvif.ver20.ptz.wsdl.SetPresetResponse;
import org.onvif.ver20.ptz.wsdl.Stop;
import org.onvif.ver20.ptz.wsdl.StopResponse;

import de.onvif.soap.OnvifDevice;
import de.onvif.soap.SOAP;

public class PtzDevices {
	private OnvifDevice onvifDevice;
	private SOAP soap;

	public PtzDevices(OnvifDevice onvifDevice) {
		this.onvifDevice = onvifDevice;
		this.soap = onvifDevice.getSoap();
	}

	public boolean isPtzOperationsSupported(String profileToken) {
		return getPTZConfiguration(profileToken) != null;
	}

	/**
	 * @param profileToken
	 * @return If is null, PTZ operations are not supported
	 */
	public PTZConfiguration getPTZConfiguration(String profileToken) {
		if (profileToken == null || profileToken.equals("")) {
			return null;
		}
		Profile profile = onvifDevice.getDevices().getProfile(profileToken);
		if (profile == null) {
			throw new IllegalArgumentException("No profile available for token: " + profileToken);
		}
		if (profile.getPTZConfiguration() == null) {
			return null; // no PTZ support
		}

		return profile.getPTZConfiguration();
	}

	public List<PTZNode> getNodes() {
		GetNodes request = new GetNodes();
		GetNodesResponse response = new GetNodesResponse();

		try {
			response = (GetNodesResponse) soap.createSOAPDeviceRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return null;
		}

		if (response == null) {
			return null;
		}

		return response.getPTZNode();
	}

	public PTZNode getNode(String profileToken) {
		return getNode(getPTZConfiguration(profileToken));
	}

	public PTZNode getNode(PTZConfiguration ptzConfiguration) {
		GetNode request = new GetNode();
		GetNodeResponse response = new GetNodeResponse();

		if (ptzConfiguration == null) {
			return null; // no PTZ support
		}
		request.setNodeToken(ptzConfiguration.getNodeToken());

		try {
			response = (GetNodeResponse) soap.createSOAPDeviceRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return null;
		}

		if (response == null) {
			return null;
		}

		return response.getPTZNode();
	}

	public FloatRange getPanSpaces(String profileToken) {
		PTZNode node = getNode(profileToken);

		PTZSpaces ptzSpaces = node.getSupportedPTZSpaces();
		return ptzSpaces.getAbsolutePanTiltPositionSpace().get(0).getXRange();
	}

	public FloatRange getTiltSpaces(String profileToken) {
		PTZNode node = getNode(profileToken);

		PTZSpaces ptzSpaces = node.getSupportedPTZSpaces();
		return ptzSpaces.getAbsolutePanTiltPositionSpace().get(0).getYRange();
	}

	public FloatRange getZoomSpaces(String profileToken) {
		PTZNode node = getNode(profileToken);

		PTZSpaces ptzSpaces = node.getSupportedPTZSpaces();
		return ptzSpaces.getAbsoluteZoomPositionSpace().get(0).getXRange();
	}

	/**
	 * Does this camera profile support absolute movement
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @return True if absolute move is supported
	 */
	public boolean isAbsoluteMoveSupported(String profileToken) {
		Profile profile = onvifDevice.getDevices().getProfile(profileToken);
		try {
			if (profile.getPTZConfiguration().getDefaultAbsolutePantTiltPositionSpace() != null) {
				return true;
			}
		} catch (NullPointerException e) {
		}
		return false;
	}

	/**
	 * Operation to move pan,tilt or zoom to a absolute destination.
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @param x
	 *            Pan-Position
	 * @param y
	 *            Tilt-Position
	 * @param zoom
	 *            Zoom
	 * @see getPanSpaces(), getTiltSpaces(), getZoomSpaces()
	 * @return True if move successful
	 * @throws SOAPException
	 */
	public boolean absoluteMove(String profileToken, float x, float y, float zoom) throws SOAPException {
		PTZNode node = getNode(profileToken);
		if (node != null) {
			FloatRange xRange = node.getSupportedPTZSpaces().getAbsolutePanTiltPositionSpace().get(0).getXRange();
			FloatRange yRange = node.getSupportedPTZSpaces().getAbsolutePanTiltPositionSpace().get(0).getYRange();
			FloatRange zRange = node.getSupportedPTZSpaces().getAbsoluteZoomPositionSpace().get(0).getXRange();

			if (zoom < zRange.getMin() || zoom > zRange.getMax()) {
				throw new IllegalArgumentException("Bad value for zoom: " + zoom);
			}
			if (x < xRange.getMin() || x > xRange.getMax()) {
				throw new IllegalArgumentException("Bad value for pan:/x " + x);
			}
			if (y < yRange.getMin() || y > yRange.getMax()) {
				throw new IllegalArgumentException("Bad value for tilt/y: " + y);
			}
		}

		AbsoluteMove request = new AbsoluteMove();
		AbsoluteMoveResponse response = new AbsoluteMoveResponse();

		Vector2D panTiltVector = new Vector2D();
		panTiltVector.setX(x);
		panTiltVector.setY(y);
		Vector1D zoomVector = new Vector1D();
		zoomVector.setX(zoom);

		PTZVector ptzVector = new PTZVector();
		ptzVector.setPanTilt(panTiltVector);
		ptzVector.setZoom(zoomVector);
		request.setPosition(ptzVector);

		request.setProfileToken(profileToken);

		try {
			response = (AbsoluteMoveResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException e) {
			throw e;
		} catch (ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}
	
	/**
	 * Operation to move pan,tilt or zoom to a absolute destination at the specified speed.
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @param x
	 * 			Pan-Position
	 * @param y
	 * 			Tilt-Position
	 * @param zoom
	 * 			Zoom
	 * @param pan_speed
	 * 			Pan-Speed			
	 * @param tilt_speed
	 * 			Tilt-Speed
	 * @param zoom_speed
	 * 			Zoom-Speed
	 * 
	 * @see org.onvif.ver10.schema.PTZSpaces: getPanSpaces(), getTiltSpaces(), getZoomSpaces(), getPanTiltSpeedSpace(), getZoomSpeedSpace()
	 * @return True if move successful
	 * @throws SOAPException
	 */
	public boolean absoluteMove(String profileToken, float x, float y, float zoom, float pan_speed, float tilt_speed, float zoom_speed) throws SOAPException {
		PTZNode node = getNode(profileToken);
		if (node != null) {
			FloatRange xRange = node.getSupportedPTZSpaces().getAbsolutePanTiltPositionSpace().get(0).getXRange();
			FloatRange yRange = node.getSupportedPTZSpaces().getAbsolutePanTiltPositionSpace().get(0).getYRange();
			FloatRange zRange = node.getSupportedPTZSpaces().getAbsoluteZoomPositionSpace().get(0).getXRange();

			FloatRange panTiltSpeedRange = node.getSupportedPTZSpaces().getPanTiltSpeedSpace().get(0).getXRange();
			FloatRange zoomSpeedRange = node.getSupportedPTZSpaces().getZoomSpeedSpace().get(0).getXRange();

			if (zoom < zRange.getMin() || zoom > zRange.getMax()) {
				throw new IllegalArgumentException("Bad value for zoom: " + zoom);
			}
			if (x < xRange.getMin() || x > xRange.getMax()) {
				throw new IllegalArgumentException("Bad value for pan/x: " + x);
			}
			if (y < yRange.getMin() || y > yRange.getMax()) {
				throw new IllegalArgumentException("Bad value for tilt/y: " + y);
			}
					 
			if (pan_speed < panTiltSpeedRange.getMin() || pan_speed > panTiltSpeedRange.getMax()) {
				throw new IllegalArgumentException("Bad value for pan_speed: " + zoom);
			}
			if (tilt_speed < panTiltSpeedRange.getMin() || tilt_speed > panTiltSpeedRange.getMax()) {
				throw new IllegalArgumentException("Bad value for tilt_speed: " + tilt_speed);
			}
			if (zoom_speed < zoomSpeedRange.getMin() || zoom_speed > zoomSpeedRange.getMax()) {
				throw new IllegalArgumentException("Bad value for zoom_speed: " + zoom_speed);
			}
		}

		AbsoluteMove request = new AbsoluteMove();
		AbsoluteMoveResponse response = new AbsoluteMoveResponse();

		Vector2D panTiltVector = new Vector2D();
		panTiltVector.setX(x);
		panTiltVector.setY(y);
		Vector1D zoomVector = new Vector1D();
		zoomVector.setX(zoom);

		PTZVector ptzVector = new PTZVector();
		ptzVector.setPanTilt(panTiltVector);
		ptzVector.setZoom(zoomVector);

		Vector2D panTiltSpeed = new Vector2D();
		panTiltSpeed.setX(pan_speed);
		panTiltSpeed.setY(tilt_speed);
		Vector1D zoomSpeed = new Vector1D();
		zoomSpeed.setX(zoom_speed);
		
		PTZSpeed speed = new PTZSpeed();
		speed.setPanTilt(panTiltSpeed);
		speed.setZoom(zoomSpeed);
		
		request.setPosition(ptzVector);
		request.setSpeed(speed);

		request.setProfileToken(profileToken);

		try {
			response = (AbsoluteMoveResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException e) {
			throw e;
		} catch (ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}
	
	/**
	 * Operation to move pan,tilt or zoom to a absolute destination at the specified speed.
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @param zoom
	 * 			Zoom
	 * @see org.onvif.ver10.schema.PTZSpaces: getZoomSpaces(), getZoomSpeedSpace()
	 * @return True if zoom successful
	 * @throws SOAPException
	 */
	public boolean absoluteZoom(String profileToken, float zoom) throws SOAPException {
		PTZNode node = getNode(profileToken);
		if (node != null) {
			FloatRange zRange = node.getSupportedPTZSpaces().getAbsoluteZoomPositionSpace().get(0).getXRange();

			if (zoom < zRange.getMin() || zoom > zRange.getMax()) {
				throw new IllegalArgumentException("Bad value for zoom: " + zoom);
			}
		}

		AbsoluteMove request = new AbsoluteMove();
		AbsoluteMoveResponse response = new AbsoluteMoveResponse();

		Vector1D zoomVector = new Vector1D();
		zoomVector.setX(zoom);

		PTZVector ptzVector = new PTZVector();
		ptzVector.setZoom(zoomVector);

		request.setPosition(ptzVector);
		request.setProfileToken(profileToken);

		try {
			response = (AbsoluteMoveResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException e) {
			throw e;
		} catch (ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}
	
	/**
	 * Operation to move pan,tilt or zoom to a absolute destination at the specified speed.
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @param zoom
	 * 			Zoom
	 * @param zoom_speed
	 * 			Zoom-Speed
	 * 
	 * @see org.onvif.ver10.schema.PTZSpaces: getZoomSpaces(), getZoomSpeedSpace()
	 * @return True if zoom successful
	 * @throws SOAPException
	 */
	public boolean absoluteZoom(String profileToken, float zoom, float zoom_speed) throws SOAPException {
		PTZNode node = getNode(profileToken);
		if (node != null) {
			FloatRange zRange = node.getSupportedPTZSpaces().getAbsoluteZoomPositionSpace().get(0).getXRange();

			FloatRange zoomSpeedRange = node.getSupportedPTZSpaces().getZoomSpeedSpace().get(0).getXRange();

			if (zoom < zRange.getMin() || zoom > zRange.getMax()) {
				throw new IllegalArgumentException("Bad value for zoom: " + zoom);
			}
					 
			if (zoom_speed < zoomSpeedRange.getMin() || zoom_speed > zoomSpeedRange.getMax()) {
				throw new IllegalArgumentException("Bad value for zoom_speed: " + zoom_speed);
			}
		}

		AbsoluteMove request = new AbsoluteMove();
		AbsoluteMoveResponse response = new AbsoluteMoveResponse();

		Vector1D zoomVector = new Vector1D();
		zoomVector.setX(zoom);

		PTZVector ptzVector = new PTZVector();
		ptzVector.setZoom(zoomVector);

		Vector1D zoomSpeed = new Vector1D();
		zoomSpeed.setX(zoom_speed);
		
		PTZSpeed speed = new PTZSpeed();
		speed.setZoom(zoomSpeed);
		
		request.setPosition(ptzVector);
		request.setSpeed(speed);

		request.setProfileToken(profileToken);

		try {
			response = (AbsoluteMoveResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException e) {
			throw e;
		} catch (ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}

	/**
	 * Does this camera profile support relative movement
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @return True if relative move is supported
	 */
	public boolean isRelativeMoveSupported(String profileToken) {
		Profile profile = onvifDevice.getDevices().getProfile(profileToken);
		try {
			if (profile.getPTZConfiguration().getDefaultRelativePanTiltTranslationSpace() != null) {
				return true;
			}
		} catch (NullPointerException e) {
		}
		return false;
	}

	/**
	 * Operation for Relative Pan/Tilt and Zoom Move.
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @param x
	 *            Pan-Position
	 * @param y
	 *            Tilt-Position
	 * @param zoom
	 *            Zoom
	 * @see getPanSpaces(), getTiltSpaces(), getZoomSpaces()
	 * @return True if move successful
	 * @throws SOAPException
	 */
	public boolean relativeMove(String profileToken, float x, float y, float zoom) throws SOAPException {
		RelativeMove request = new RelativeMove();
		RelativeMoveResponse response = new RelativeMoveResponse();

		Vector2D panTiltVector = new Vector2D();
		panTiltVector.setX(x);
		panTiltVector.setY(y);
		Vector1D zoomVector = new Vector1D();
		zoomVector.setX(zoom);

		PTZVector translation = new PTZVector();
		translation.setPanTilt(panTiltVector);
		translation.setZoom(zoomVector);

		request.setProfileToken(profileToken);
		request.setTranslation(translation);

		try {
			response = (RelativeMoveResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException e) {
			throw e;
		} catch (ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}

	/**
	 * Operation for Relative Pan/Tilt and Zoom Move at the specified speed.
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @param x
	 *            Pan-Position
	 * @param y
	 *            Tilt-Position
	 * @param zoom
	 *            Zoom
	 * @param pan_speed
	 * 			Pan-Speed			
	 * @param tilt_speed
	 * 			Tilt-Speed
	 * @param zoom_speed
	 * 			Zoom-Speed            
	 * @see getPanSpaces(), getTiltSpaces(), getZoomSpaces()
	 * @return True if move successful
	 * @throws SOAPException
	 */
	public boolean relativeMove(String profileToken, float x, float y, float zoom, float pan_speed, float tilt_speed, float zoom_speed) throws SOAPException {
		RelativeMove request = new RelativeMove();
		RelativeMoveResponse response = new RelativeMoveResponse();

		Vector2D panTiltVector = new Vector2D();
		panTiltVector.setX(x);
		panTiltVector.setY(y);
		Vector1D zoomVector = new Vector1D();
		zoomVector.setX(zoom);

		PTZVector translation = new PTZVector();
		translation.setPanTilt(panTiltVector);
		translation.setZoom(zoomVector);

		Vector2D panTiltSpeed = new Vector2D();
		panTiltSpeed.setX(pan_speed);
		panTiltSpeed.setY(tilt_speed);
		Vector1D zoomSpeed = new Vector1D();
		zoomSpeed.setX(zoom_speed);
		
		PTZSpeed speed = new PTZSpeed();
		speed.setPanTilt(panTiltSpeed);
		speed.setZoom(zoomSpeed);
				
		request.setProfileToken(profileToken);
		request.setTranslation(translation);
		request.setSpeed(speed);
		
		try {
			response = (RelativeMoveResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException e) {
			throw e;
		} catch (ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}
	
	/**
	 * Does this camera profile support continuous movement
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @return True if continuous move is supported
	 */	
	public boolean isContinuosMoveSupported(String profileToken) {
		Profile profile = onvifDevice.getDevices().getProfile(profileToken);
		try {
			if (profile.getPTZConfiguration().getDefaultContinuousPanTiltVelocitySpace() != null) {
				return true;
			}
		} catch (NullPointerException e) {
		}
		return false;
	}

	/**
	 * Operation for continuous Pan/Tilt and Zoom movements. 
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @param x
	 *			Pan-Speed (negative values for left)
	 * @param y
	 *			Tilt-Speed (negative values for down)
	 * @param zoom
	 *			Zoom-Speed (negative values for zoom out)
	 * @see getPanSpaces(), getTiltSpaces(), getZoomSpaces()
	 * @return True if move successful
	 * @throws SOAPException
	 */
	public boolean continuousMove(String profileToken, float x, float y, float zoom) {
		ContinuousMove request = new ContinuousMove();
		ContinuousMoveResponse response = new ContinuousMoveResponse();

		Vector2D panTiltVector = new Vector2D();
		panTiltVector.setX(x);
		panTiltVector.setY(y);
		Vector1D zoomVector = new Vector1D();
		zoomVector.setX(zoom);

		PTZSpeed ptzSpeed = new PTZSpeed();
		ptzSpeed.setPanTilt(panTiltVector);
		ptzSpeed.setZoom(zoomVector);
		request.setVelocity(ptzSpeed);

		request.setProfileToken(profileToken);

		try {
			response = (ContinuousMoveResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}

	/**
	 * Operation for continuous zoom movement. 
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @param zoom
	 *			Zoom-Speed (negative values for zoom out)
	 * @see getZoomSpaces()
	 * @return True if zoom successful
	 * @throws SOAPException
	 */
	public boolean continuousZoom(String profileToken, float zoom) {
		ContinuousMove request = new ContinuousMove();
		ContinuousMoveResponse response = new ContinuousMoveResponse();

		Vector1D zoomVector = new Vector1D();
		zoomVector.setX(zoom);

		PTZSpeed ptzSpeed = new PTZSpeed();
		ptzSpeed.setZoom(zoomVector);
		request.setVelocity(ptzSpeed);

		request.setProfileToken(profileToken);

		try {
			response = (ContinuousMoveResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}	
	
	/**
	 * Operation to stop ongoing pan, tilt and zoom movements of absolute relative and continuous type.
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @return
	 */
	public boolean stopMove(String profileToken) {
		Stop request = new Stop();
		request.setPanTilt(true);
		request.setZoom(true);
		StopResponse response = new StopResponse();

		request.setProfileToken(profileToken);

		try {
			response = (StopResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}

	/**
	 * Operation to stop ongoing pan, tilt and zoom movements of absolute
	 * relative and continuous type. If no stop argument for pan, tilt or zoom
	 * is set, the device will stop all ongoing pan, tilt and zoom movements.
	 * 
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration
	 * @param stopPanTilt
	 * 			should the pan and tilt action be stopped
	 * @param stopZoom
	 * 			should the zoom action be stopped
	 * @return
	 */
	public boolean stopMove(String profileToken, boolean stopPanTilt, boolean stopZoom) {
		Stop request = new Stop();
		request.setPanTilt(stopPanTilt);
		request.setZoom(stopZoom);
		StopResponse response = new StopResponse();

		request.setProfileToken(profileToken);

		try {
			response = (StopResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}

	/**
	 * Move the Camera to the Home position
	 * 
	 * @param profileToken
	 * 			profileToken id string of camera to check PTZ configuration 
	 * @return
	 */
	public boolean moveHome(String profileToken) {
		GotoHomePosition request = new GotoHomePosition();
		GotoHomePositionResponse response = new GotoHomePositionResponse();

		request.setProfileToken(profileToken);

		try {
			response = (GotoHomePositionResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}

	public PTZStatus getStatus(String profileToken) {
		GetStatus request = new GetStatus();
		GetStatusResponse response = new GetStatusResponse();

		request.setProfileToken(profileToken);

		try {
			response = (GetStatusResponse) soap.createSOAPPtzRequest(request, response, false);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return null;
		}

		if (response == null) {
			return null;
		}

		return response.getPTZStatus();
	}

	public PTZVector getPosition(String profileToken) {
		PTZStatus status = getStatus(profileToken);

		if (status == null) {
			return null;
		}

		return status.getPosition();
	}

	public boolean setHomePosition(String profileToken) {
		SetHomePosition request = new SetHomePosition();
		SetHomePositionResponse response = new SetHomePositionResponse();

		request.setProfileToken(profileToken);

		try {
			response = (SetHomePositionResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}

	public List<PTZPreset> getPresets(String profileToken) {
		GetPresets request = new GetPresets();
		GetPresetsResponse response = new GetPresetsResponse();

		request.setProfileToken(profileToken);

		try {
			response = (GetPresetsResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return null;
		}

		if (response == null) {
			return null;
		}

		return response.getPreset();
	}

	public String setPreset(String presetName, String presetToken, String profileToken) {
		SetPreset request = new SetPreset();
		SetPresetResponse response = new SetPresetResponse();

		request.setProfileToken(profileToken);
		request.setPresetName(presetName);
		request.setPresetToken(presetToken);

		try {
			response = (SetPresetResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return null;
		}

		if (response == null) {
			return null;
		}

		return response.getPresetToken();
	}

	public String setPreset(String presetName, String profileToken) {
		return setPreset(presetName, null, profileToken);
	}

	public boolean removePreset(String presetToken, String profileToken) {
		RemovePreset request = new RemovePreset();
		RemovePresetResponse response = new RemovePresetResponse();

		request.setProfileToken(profileToken);
		request.setPresetToken(presetToken);

		try {
			response = (RemovePresetResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}

	public boolean gotoPreset(String presetToken, String profileToken) {
		GotoPreset request = new GotoPreset();
		GotoPresetResponse response = new GotoPresetResponse();

		request.setProfileToken(profileToken);
		request.setPresetToken(presetToken);

		try {
			response = (GotoPresetResponse) soap.createSOAPPtzRequest(request, response, true);
		} catch (SOAPException | ConnectException e) {
			e.printStackTrace();
			return false;
		}

		if (response == null) {
			return false;
		}

		return true;
	}
}
