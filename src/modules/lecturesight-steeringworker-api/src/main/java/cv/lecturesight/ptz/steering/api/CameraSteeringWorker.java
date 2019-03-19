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
package cv.lecturesight.ptz.steering.api;

import cv.lecturesight.util.geometry.NormalizedPosition;
import cv.lecturesight.util.geometry.Position;

/** Camera Steering Worker API
 *
 */
public interface CameraSteeringWorker {

  boolean isSteering();

  void setSteering(boolean on);

  void setInitialPosition(String presetName);

  void setInitialPosition(NormalizedPosition pos);

  void setTargetPosition(NormalizedPosition pos);

  NormalizedPosition getTargetPosition();

  Position getTargetCameraPosition();

  NormalizedPosition getActualPosition();

  Position getActualCameraPosition();

  void setZoom(float zoom);

  float getZoom();

  void setFrameWidth(float width);

  float getFrameWidth();

  void setFrameHeight(float height);

  float getFrameHeight();

  boolean movePreset(String presetName);

  void moveHome();

  boolean isMoving();

  boolean autoCalibrate();

  int getPanMin();

  int getPanMax();

  int getTiltMin();

  int getTiltMax();

  void addUISlave(UISlave slave);

  void removeUISlave(UISlave slave);
}

