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
