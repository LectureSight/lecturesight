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
package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.ptz.api.PTZCameraProfile;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.NormalizedPosition;
import cv.lecturesight.util.geometry.Position;

public class CameraPositionModel {

  private PTZCamera camera;
  Configuration config;
  PTZCameraProfile camProfile;
  private Position camPosition = new Position(0, 0);
  private NormalizedPosition targetPosition = new NormalizedPosition(0.0f, 0.0f);
  private NormalizedPosition actualPosition = new NormalizedPosition(0.0f, 0.0f);
  private int pan_min;
  private int pan_max;
  private int tilt_min;
  private int tilt_max;
  boolean moving = false;
  private String status = "N/A";

  public CameraPositionModel(PTZCamera camera, Configuration config) {
    this.camera = camera;
    this.config = config;
    camProfile = camera.getProfile();
    
    // initialize limits for pan and tilt, if not configured by camera calibration
    // the limits from the camera profile are taken
    String val = config.get(Constants.PROPKEY_LIMIT_LEFT);
    if (val.isEmpty() || val.equalsIgnoreCase("none")) {
      pan_min = camProfile.getPanMin();
    } else {
      pan_min = config.getInt(Constants.PROPKEY_LIMIT_LEFT);
    }
    
    val = config.get(Constants.PROPKEY_LIMIT_RIGHT);
    if (val.isEmpty() || val.equalsIgnoreCase("none")) {
      pan_max = camProfile.getPanMax();
    } else {
      pan_max = config.getInt(Constants.PROPKEY_LIMIT_RIGHT);
    }
    
    val = config.get(Constants.PROPKEY_LIMIT_TOP);
    if (val.isEmpty() || val.equalsIgnoreCase("none")) {
      tilt_max = camProfile.getTiltMax();
    } else {
      tilt_max = config.getInt(Constants.PROPKEY_LIMIT_TOP);
    }
    
    val = config.get(Constants.PROPKEY_LIMIT_BOTTOM);
    if (val.isEmpty() || val.equalsIgnoreCase("none")) {
      tilt_min = camProfile.getTiltMin();
    } else {
      tilt_min = config.getInt(Constants.PROPKEY_LIMIT_BOTTOM);
    }
  }

  public NormalizedPosition getTargetPosition() {
    return targetPosition.clone();
  }

  public void setTargetPosition(NormalizedPosition targetPosition) {
    this.targetPosition.setX(targetPosition.getX());
    this.targetPosition.setY(targetPosition.getY());
  }

  public NormalizedPosition getActualPosition() {
    return actualPosition.clone();
  }

  public void setActualPosition(NormalizedPosition actualPosition) {
    this.actualPosition.setX(actualPosition.getX());
    this.actualPosition.setY(actualPosition.getY());
  }

  public void setMoving(boolean moving) {
    this.moving = moving;
  }

  public boolean isMoving() {
    return moving;
  }

  public String getCameraName() {
    return camera.getName();
  }

  public NormalizedPosition toNormalizedCoordinates(Position camPosition) {
    NormalizedPosition out = new NormalizedPosition(0.0f, 0.0f);
    float x = camPosition.getX();
    float y = camPosition.getY();
    // x
    if (x < 0) {
      out.setX(-1 * (x / getPan_min()));
    } else if (x > 0) {
      out.setX(x / getPan_max());
    }
    // y
    if (y < 0) {
      out.setY(-1 * (y / getTilt_min()));
    } else if (y > 0) {
      out.setY(y / getTilt_max());
    }
    return out;
  }

  public Position toCameraCoordinates(NormalizedPosition pos) {
    Position out = new Position(0, 0);
    float x = pos.getX();
    float y = pos.getY();
    // x
    if (x < 0) {
      out.setX((int) (-1 * (x * getPan_min())));
    } else if (x > 0) {
      out.setX((int) (x * getPan_max()));
    }
    // y
    if (y < 0) {
      out.setY((int) (-1 * (y * getTilt_min())));
    } else if (y > 0) {
      out.setY((int) (y * getTilt_max()));
    }
    return out;
  }

  public int getPan_min() {
    return pan_min;
  }

  public int getPan_max() {
    return pan_max;
  }

  public int getTilt_min() {
    return tilt_min;
  }

  public int getTilt_max() {
    return tilt_max;
  }

  public Position getCamPosition() {
    return camPosition;
  }

  public void setCamPosition(Position camPosition) {
    this.camPosition = camPosition;
  }

  void setStatus(String status) {
    this.status = status;
  }
  
  String getStatus() {
    return status;
  }
}
