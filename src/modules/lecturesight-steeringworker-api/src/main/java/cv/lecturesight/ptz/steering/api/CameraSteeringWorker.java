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
  
  public boolean isSteering();
  
  public void setSteering(boolean on);

  public void setInitialPosition(NormalizedPosition pos);
  
  public void setTargetPosition(NormalizedPosition pos);
  
  public NormalizedPosition getTargetPosition();
  
  public NormalizedPosition getActualPosition();
  
  public void setZoom(float zoom);
  
  public float getZoom();

  public void setFrameWidth(float width);

  public float getFrameWidth();

  public void movePreset(int preset);

  public void moveHome();
  
  public boolean isMoving();
  
  public int getPanMin();
  
  public int getPanMax();
  
  public int getTiltMin();
  
  public int getTiltMax();
  
  public Position toCameraCoordinates(NormalizedPosition posn);
  
  public void addUISlave(UISlave slave);
  
  public void removeUISlave(UISlave slave);
}
