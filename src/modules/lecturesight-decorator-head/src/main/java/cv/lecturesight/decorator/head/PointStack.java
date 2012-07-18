/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cv.lecturesight.decorator.head;

/**
 *
 * @author Alex
 */
public class PointStack {
  
  Point [] elements;
  
  public PointStack() {
    elements = new Point [0];
  }
  
  public void push(Point e) {
    Point [] new_elements = new Point [elements.length+1];
    new_elements[0] = e;
    System.arraycopy(elements, 0, new_elements, 1, new_elements.length - 1);
    elements = new_elements;
  }
  
  public Point pop() {
    Point e = elements[0];
    Point [] new_elements = new Point [elements.length-1];
    System.arraycopy(elements, 0, new_elements, 1, elements.length - 1);
    elements = new_elements;
    return e;
  }

  public Point index(int n) {
    return elements[n];
  }

  public int length() {
    return elements.length;
  }

}