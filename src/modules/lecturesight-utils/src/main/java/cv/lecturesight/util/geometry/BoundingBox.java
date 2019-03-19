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

public class BoundingBox implements Cloneable {

  private Position min;
  private Position max;

  public BoundingBox() {
    this.min = new Position();
    this.max = new Position();
  }

  public BoundingBox(Position min, Position max) {
    this.min = min;
    this.max = max;
  }

  public int getWidth() {
    return max.getX() - min.getX();
  }

  public int getHeight() {
    return max.getY() - min.getY();
  }

  public Position getMin() {
    return min;
  }

  public void setMin(Position min) {
    this.min = min;
  }

  public Position getMax() {
    return max;
  }

  public void setMax(Position max) {
    this.max = max;
  }

  @Override
  public String toString() {
    return new StringBuilder()
    .append(min)
    .append("-")
    .append(max)
    .toString();
  }

  @Override
  public BoundingBox clone() {
    return new BoundingBox(min, max);
  }

  public boolean contains(Position p) {
    if (p.getX() < max.getX() && p.getX() > min.getX()
     && p.getY() < max.getY() && p.getY() > min.getY()) {
      return true;
    }
    return false;
  }

}
