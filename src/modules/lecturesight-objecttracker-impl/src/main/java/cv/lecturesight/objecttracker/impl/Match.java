/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.lecturesight.objecttracker.impl;

import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.regiontracker.Region;

/**
 *
 * @author alex
 */
public class Match {
  
  TrackerObject obj;
  Region r;
  
  public Match(TrackerObject obj, Region r) {
    this.obj = obj;
    this.r = r;
  }
  
  public TrackerObject getObj() {
    return obj;
  }
  
  public Region getRegion() {
    return r;
  }
}
