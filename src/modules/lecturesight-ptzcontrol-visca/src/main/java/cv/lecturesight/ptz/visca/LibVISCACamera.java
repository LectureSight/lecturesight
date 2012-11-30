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
