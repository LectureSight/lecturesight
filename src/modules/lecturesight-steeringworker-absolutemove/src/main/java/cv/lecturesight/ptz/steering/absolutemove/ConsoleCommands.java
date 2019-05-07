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
package cv.lecturesight.ptz.steering.absolutemove;

import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.util.DummyInterface;
import cv.lecturesight.util.geometry.NormalizedPosition;
import lombok.Setter;
import org.pmw.tinylog.Logger;

public class ConsoleCommands implements DummyInterface {

  @Setter
  CameraSteeringWorker steerer;

  public void on(String[] args) {
    steerer.setSteering(true);
  }

  public void off(String[] args) {
    steerer.setSteering(false);
  }

  public void move(String[] args) {
    try {
      steerer.setTargetPosition(getPosition(args));
    } catch (IllegalArgumentException e) {
      Logger.warn(e.getMessage());
      System.out.println("Usage: cs:move (float)x (float)y");
    }
  }

  private NormalizedPosition getPosition(String[] args) throws IllegalArgumentException {
    if (args.length < 2) {
      throw new IllegalArgumentException("Not enough arguments!");
    }
    try {
      float x = Float.parseFloat(args[0]);
      float y = Float.parseFloat(args[1]);
      return new NormalizedPosition(x, y);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse arguments: " + e.getMessage());
    }
  }

  public void home(String[] args) {
    steerer.setTargetPosition(new NormalizedPosition(0.0f, 0.0f));
  }

  public void zoom(String[] args) {
    if (args.length == 0) {
      System.out.println("Zoom: " + steerer.getZoom());
    } else {
      try {
        float zoom = Float.parseFloat(args[0]);
        steerer.setZoom(zoom);
      } catch (NumberFormatException e) {
        System.out.println("Could not parse zoom factor.");
      }
    }
  }
}
