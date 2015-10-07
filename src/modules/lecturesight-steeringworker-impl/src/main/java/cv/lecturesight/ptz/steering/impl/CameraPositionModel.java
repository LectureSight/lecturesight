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

import cv.lecturesight.util.geometry.NormalizedPosition;
import cv.lecturesight.util.geometry.Position;

public class CameraPositionModel {

  // scene limits for normalization
  private final int pan_min;
  private final int pan_max;
  private final int tilt_min;
  private final int tilt_max;

  private Position camera_pos = new Position(0, 0);   // camera position in camera coordinates
  private Position target_pos = new Position(0, 0);   // target position in camera coordinates
  private NormalizedPosition target_posn = new NormalizedPosition(0.0f, 0.0f);  // camera position in normalized coordinates
  private NormalizedPosition camera_posn = new NormalizedPosition(0.0f, 0.0f);  // target position in normalized coordinates

  public CameraPositionModel(int pan_min, int pan_max, int tilt_min, int tilt_max) {
    this.pan_max = pan_max;
    this.pan_min = pan_min;
    this.tilt_max = tilt_max;
    this.tilt_min = tilt_min;
  }

  /**
   * Translates camera coordinates to normalized coordinates (-1 to 1)
   *
   * @param pos camera coordinates
   * @return normalized coordinates
   */
  public NormalizedPosition toNormalizedCoordinates(Position pos) {
    NormalizedPosition out = new NormalizedPosition(0.0f, 0.0f);
    float x = pos.getX();
    float y = pos.getY();

    out.setX((x - pan_min) / (pan_max - pan_min) * 2 - 1);
    out.setY((y - tilt_min) / (tilt_max - tilt_min) * 2 - 1);

    return out;
  }

  /**
   * Translates normalized coordinates (-1 to 1) to camera coordinates.
   *
   * @param pos normalized coordinates
   * @return camera coordinates
   */
  public Position toCameraCoordinates(NormalizedPosition pos) {
    Position out = new Position(0, 0);
    float x = pos.getX();
    float y = pos.getY();

    out.setX( (int) ( (x+1) * (pan_max - pan_min) * 0.5 + pan_min));
    out.setY( (int) ( (y+1) * (tilt_max - tilt_min) * 0.5 + tilt_min));

    return out;
  }

  public synchronized void setCameraPositionNorm(NormalizedPosition posn) {
    camera_posn = posn;
    camera_pos = toCameraCoordinates(posn);
  }

  public synchronized void setCameraPosition(Position pos) {
    camera_posn = toNormalizedCoordinates(pos);
    camera_pos = pos;
  }

  public synchronized void setTargetPositionNorm(NormalizedPosition posn) {
    target_posn = posn;
    target_pos = toCameraCoordinates(posn);
  }

  public synchronized void setTargetPosition(Position pos) {
    target_posn = toNormalizedCoordinates(pos);
    target_pos = pos;
  }

  public synchronized Position getCameraPosition() {
    return camera_pos.clone();
  }

  public synchronized NormalizedPosition getCameraPositionNorm() {
    return camera_posn.clone();
  }

  public synchronized Position getTargetPosition() {
    return target_pos.clone();
  }

  public synchronized NormalizedPosition getTargetPositionNorm() {
    return target_posn.clone();
  }
}
