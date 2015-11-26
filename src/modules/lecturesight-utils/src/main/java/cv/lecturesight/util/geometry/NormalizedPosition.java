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
package cv.lecturesight.util.geometry;

public class NormalizedPosition implements Cloneable {

  private float x, y;

  public NormalizedPosition() {
    this.x = 0.0f;
    this.y = 0.0f;
  }

  public NormalizedPosition(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = clampValue(x);
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = clampValue(y);
  }

  private float clampValue(float f) {
    if (f < -1.0f) {
      f = -1.0f;
    }
    if (f > 1.0f) {
      f = 1.0f;
    }
    return f;
  }

  public double distance(NormalizedPosition other) {
    return Math.sqrt(
            Math.pow(Math.abs(getX() - other.getX()), 2)
            + Math.pow(Math.abs(getY() - other.getY()), 2));
  }

  @Override
  public String toString() {
    return String.format("%.4f,%.4f", x, y);
  }

  @Override
  public NormalizedPosition clone() {
    return new NormalizedPosition(getX(), getY());
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof Position) {
      NormalizedPosition other = (NormalizedPosition)o;
      return other.x == this.x && other.y == this.y;
    } else {
      return false;
    }
  }
}
