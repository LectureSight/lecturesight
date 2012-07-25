/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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