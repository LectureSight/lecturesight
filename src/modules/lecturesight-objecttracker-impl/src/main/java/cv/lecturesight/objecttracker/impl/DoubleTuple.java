/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.lecturesight.objecttracker.impl;

import cv.lecturesight.util.geometry.Position;

/**
 *
 * @author alex
 */
public class DoubleTuple {
  
  double d1;
  double d2;
  Position min;
  Position max;
  
  public DoubleTuple(double d1, double d2) {
    this.d1 = d1;
    this.d2 = d2;
  }
  
  public DoubleTuple(double d1, double d2, Position min, Position max) {
    this.d1 = d1;
    this.d2 = d2;
    this.min = min;
    this.max = max;
  }
}