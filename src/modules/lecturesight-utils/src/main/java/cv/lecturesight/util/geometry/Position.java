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

public class Position implements Cloneable {

  private int x;
  private int y;

  public Position() {
    this.x = 0;
    this.y = 0;
  }

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public double distance(Position other) {
    return Math.sqrt(Math.pow(Math.abs(x - other.x), 2)
                     + Math.pow(Math.abs(y - other.y), 2));
  }

  @Override
  public String toString() {
    return new StringBuilder()
    .append(x)
    .append(",")
    .append(y)
    .toString();
  }

  @Override
  public Position clone() {
    return new Position(x,y);
  }

  public Position flip(boolean xflip, boolean yflip) {
    return new Position(xflip ? (-1 * x) : x, yflip ? (-1 * y) : y);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Position) {
      Position other = (Position)o;
      return other.x == this.x && other.y == this.y;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return x * 31 + y;
  }

}
