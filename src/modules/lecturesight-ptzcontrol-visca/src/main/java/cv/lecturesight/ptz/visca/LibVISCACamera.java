package cv.lecturesight.ptz.visca;

public class LibVISCACamera {

  static {
    System.loadLibrary("ptzcamvisca");
  }
  private int camNo;
  private String port = "";
  private String name = "";

  public LibVISCACamera(String name) {
    this.name = name;
  }
  
  public int getCamNo() {
    return camNo;
  }

  public String getPort() {
    return port;
  }

  public String getName() {
    return name;
  }

  native boolean initialize(String port, int cam_no);

  native boolean deinitialize();

  native int getConnectedCams();

  native void setPower(boolean power);

  native void setIrLed(boolean power);
  
  native void setZoomStop();
  
  native void setZoomTeleSpeed(int speed);
  
  native void setZoomWideSpeed(int speed);

  native void setZoomValue(int zoom);

  native void setPanTiltUp(int tilt_speed);

  native void setPanTiltDown(int tilt_speed);

  native void setPanTiltLeft(int pan_speed);

  native void setPanTiltRight(int pan_speed);

  native void setPanTiltUpLeft(int pan_speed, int tilt_speed);

  native void setPanTiltUpRight(int pan_speed, int tilt_speed);

  native void setPanTiltDownLeft(int pan_speed, int tilt_speed);

  native void setPanTiltDownRight(int pan_speed, int tilt_speed);

  native void setPanTiltStop();

  native void setPanTiltAbsolutePosition(int pan_speed, int tilt_speed, int pan_position, int tilt_position);

  native void setPanTiltRelativePosition(int pan_speed, int tilt_speed, int pan_position, int tilt_position);

  native void setPanTiltHome();

  native void setPanTiltReset();

  native void setPanTiltLimitUpRight(int pan_limit, int tilt_limit);

  native void setPanTiltLimitDownLeft(int pan_limit, int tilt_limit);

  native void setPanTiltLimitDownLeftClear();

  native void setPanTiltLimitUprightClear();

  native int getVendor();

  native int getModel();

  native int getROMVersion();

  native boolean getPower();

  native boolean getIrLed();

  native int getZoomValue();

  native boolean getFocusAuto();

  native int getFocusValue();

  native int getPanMaxSpeed();

  native int getPanPosition();

  native int getTiltMaxSpeed();

  native int getTiltPosition();
}
