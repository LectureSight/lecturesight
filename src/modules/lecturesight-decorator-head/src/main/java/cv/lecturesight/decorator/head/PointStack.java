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
package cv.lecturesight.decorator.head;

import cv.lecturesight.util.geometry.Position;

/**
 *
 * @author Alex
 */
public class PointStack {
  
  Position [] elements;
  
  public PointStack() {
    elements = new Position [0];
  }
  
  public void push(Position e) {
    Position [] new_elements = new Position [elements.length+1];
    new_elements[0] = e;
    System.arraycopy(elements, 0, new_elements, 1, new_elements.length - 1);
    elements = new_elements;
  }
  
  public Position pop() {
    Position e = elements[0];
    Position [] new_elements = new Position [elements.length-1];
    System.arraycopy(elements, 0, new_elements, 1, elements.length - 1);
    elements = new_elements;
    return e;
  }

  public Position index(int n) {
    return elements[n];
  }

  public int length() {
    return elements.length;
  }

}