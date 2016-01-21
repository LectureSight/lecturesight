package cv.lecturesight.onvif.service;

public class CameraPosition {

	  int x,y,z;

	  public CameraPosition() {
	    this.x = 0;
	    this.y = 0;
	    this.z = -1;
	  }

	  public CameraPosition(int x, int y) {
	    this.x = x;
	    this.y = y;
	    this.z = -1;
	  }

	  public CameraPosition(int x, int y, int zoom) {
	    this.x = x;
	    this.y = y;
	    this.z = zoom;
	  }

	  public int x() {
	    return x;
	  }

	  public int y() {
	    return y;
	  }

	  public int zoom() {
	    return z;
	  }

	  public void set(int x, int y) {
	    this.x = x;
	    this.y = y;
	  }

	  public void set(int x, int y, int zoom) {
	    this.x = x;
	    this.y = y;
	    this.z = zoom;
	  }

	  void setZoom(int zoom) {
	    this.z = zoom;
	  }
	}

