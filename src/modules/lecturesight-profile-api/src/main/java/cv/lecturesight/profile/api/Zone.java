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

public class Zone implements Cloneable {
  
  public enum Type {
    SIZE, TRACK, IGNORE, TRIGGER, PERSON, CALIBRATION;
  }
  
  public String name;
  
  Type type;
  
  public int x;
  
  public int y;
  
  public int width;
  
  public int height;
    
  public Zone() {
    this.name="";
    this.type = Type.IGNORE;
    this.x = 0;
    this.y = 0;
    this.width = 1;
    this.height = 1;
  }
  
  public Zone(String name, Type type, int x, int y, int width, int height) {
    this.name = name;
    this.type = type;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
  
  public Type getType() {
    return type;
  }
  
  @Override
  public Zone clone() {
    return new Zone(name, type, x, y, width, height);
  }
  
  @Override
  public boolean equals(Object other) {
    return other instanceof Zone && hashCode() == ((Zone)other).hashCode();
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 41 * hash + (this.type != null ? this.type.hashCode() : 0);
    hash = 41 * hash + this.x;
    hash = 41 * hash + this.y;
    hash = 41 * hash + this.width;
    hash = 41 * hash + this.height;
    return hash;
  }
}
