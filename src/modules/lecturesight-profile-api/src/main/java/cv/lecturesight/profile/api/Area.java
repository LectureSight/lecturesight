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
package cv.lecturesight.profile.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="area")
public class Area implements Cloneable {
  
  @XmlAttribute(name="name")
  String name;
  
  @XmlAttribute(name="pos-x")
  public int x;
  
  @XmlAttribute(name="pos-y")
  public int y;
  
  @XmlAttribute(name="width")
  public int width;
  
  @XmlAttribute(name="height")
  public int height;
  
  public Area() {
    this.name="unnamed";
    this.x = 0;
    this.y = 0;
    this.width = 1;
    this.height = 1;
  }
  
  public Area(String name, int x, int y, int width, int height) {
    this.name = name;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
  
  public Area clone() {
    return new Area(name, x, y, width, height);
  }
}
