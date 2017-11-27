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
package cv.lecturesight.ptz.api;

import cv.lecturesight.util.geometry.Position;

public interface PTZCamera {

  enum FocusMode {
    STOP, NEAR, FAR, MANUAL, AUTO
  }

  String getName();

  PTZCameraProfile getProfile();

  void reset();

  void cancel();

  void stopMove();

  void moveHome();

  void movePreset(int preset);

  void moveUp(int tiltSpeed);

  void moveDown(int tiltSpeed);

  void moveLeft(int panSpeed);

  void moveRight(int panSpeed);

  void moveUpLeft(int panSpeed, int tiltSpeed);

  void moveUpRight(int panSpeed, int tiltSpeed);

  void moveDownLeft(int panSpeed, int tiltSpeed);

  void moveDownRight(int panSpeed, int tiltSpeed);

  void moveAbsolute(int panSpeed, int tiltSpeed, Position target);

  void moveRelative(int panSpeed, int tiltSpeed, Position target);

  void clearLimits();

  void setLimitUpRight(int pan, int tilt);

  void setLimitDownLeft(int pan, int tilt);

  Position getPosition();

  void stopZoom();

  void zoomIn(int speed);

  void zoomOut(int speed);

  void zoom(int zoom);

  int getZoom();

  void focus(int focus);

  int getFocus();

  void focusMode(FocusMode mode);

  void addCameraListener(CameraListener l);

  void removeCameraListener(CameraListener l);
}
