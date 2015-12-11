package cv.lecturesight.onvif.service;

public class CameraState {

	  CameraPosition position = new CameraPosition();

	  public boolean isMoving() {
	    return false;
	  }

	  public CameraPosition currentPosition() {
	    return position;
	  }
	}

