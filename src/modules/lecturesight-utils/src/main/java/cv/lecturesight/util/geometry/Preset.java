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

import java.util.Objects;

public class Preset extends Position implements Cloneable {

  private int zoom;
  private String name;

  public Preset() {
    this.x = 0;
    this.y = 0;
    this.zoom = 0;
    this.name = "";
  }

  public Preset(String name, int x, int y, int zoom) {
    this.x = x;
    this.y = y;
    this.zoom = zoom;
    this.name = name;
  }

  public String getName() { return name; }

  public void setName(String name) { this.name = name; }

  public int getZoom() {
    return zoom;
  }

  public void setZoom(int zoom) {
    this.zoom = zoom;
  }

  @Override
  public String toString() {
    return new StringBuilder()
    .append(name).append(":")
    .append(x).append(",").append(y)
    .append(";").append(zoom)
    .toString();
  }

  @Override
  public Preset clone() {
    return new Preset(name, x, y, zoom);
  }

  public Preset flip(boolean xflip, boolean yflip) {
    return new Preset(name, xflip ? (-1 * x) : x, yflip ? (-1 * y) : y, zoom);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Preset) {
      Preset other = (Preset) o;
      return other.x == this.x && other.y == this.y && other.zoom == this.zoom
              && (other.name != null) && (other.name.equals(name));
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, zoom, name);
  }

}
