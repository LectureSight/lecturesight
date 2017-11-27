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

public class CoordinatesNormalization {

  private int maxX;
  private int maxY;

  public CoordinatesNormalization(int maxX, int maxY) {
    this.maxX = maxX;
    this.maxY = maxY;
  }

  public float normalizeX(int x) {
    return ((float) x / (float) maxX) * 2.0f - 1.0f;
  }

  public float normalizeY(int y) {
    return ((float) y / (float) maxY) * 2.0f - 1.0f;
  }

  public int denormalizeX(float x) {
    return (int) (((x + 1) / 2) * maxX);
  }

  public int denormalizeY(float y) {
    return (int) (((y + 1) / 2) * maxY);
  }

  public NormalizedPosition toNormalized(Position pos) {
    return new NormalizedPosition(
            normalizeX(pos.getX()),
            normalizeY(pos.getY()));
  }

  public Position fromNormalized(NormalizedPosition pos) {
    return new Position(
            denormalizeX(pos.getX()),
            denormalizeY(pos.getY()));
  }

  public int getMaxX() {
    return maxX;
  }

  public void setMaxX(int maxX) {
    if (maxX > 0) {
      this.maxX = maxX;
    } else {
      this.maxX = 1;
    }
  }

  public int getMaxY() {
    return maxY;
  }

  public void setMaxY(int maxY) {
    if (maxY > 0) {
      this.maxY = maxY;
    } else {
      this.maxY = 1;
    }
  }
}
