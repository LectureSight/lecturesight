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

/**
 * Convert between overview image co-ordinates and normalized co-ordinates.
 *
 * For example, for an overview image 640x360 pixels,
 * overview (0,0) to (639,359) is mapped to normalized (-1, 1) to (1, -1).
 *
 * Note that the normalized Y co-ordinates are inverted relative to overview image co-ordinates:
 * top is positive, bottom is negative. This aligns with camera tilt co-ordinates.
 */
public class CoordinatesNormalization {

  // Dimensions of the image in pixels
  private int width;
  private int height;

  public CoordinatesNormalization(int width, int height) {
    this.width = width;
    this.height = height;
  }

  // Range -1 ... 1 for co-ordinates from 0 ... width - 1
  public float normalizeX(int x) {
    return (x / (float) (width - 1)) * 2.0f - 1.0f;
  }

  // Range -1 ... 1 for co-ordinates from 0 ... height - 1
  public float normalizeY(int y) {
    return (y / (float) (height - 1)) * -2.0f + 1.0f;
  }

  // Range 0 ... width - 1 for co-ordinates from -1 ... 1
  public int denormalizeX(float x) {
    return (int) Math.round((x + 1) / 2f * (width - 1));
  }

  // Range 0 ... height - 1 for co-ordinates from -1 ... 1
  public int denormalizeY(float y) {
    return (int) Math.round((-1 * y + 1) / 2f * (height - 1));
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

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    if (width > 0) {
      this.width = width;
    } else {
      this.width = 1;
    }
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    if (height > 0) {
      this.height = height;
    } else {
      this.height = 1;
    }
  }
}
