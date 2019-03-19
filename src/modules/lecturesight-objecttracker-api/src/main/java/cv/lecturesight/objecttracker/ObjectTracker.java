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
package cv.lecturesight.objecttracker;

import cv.lecturesight.opencl.api.OCLSignal;

import java.util.List;
import java.util.Map;

/** Camera Operator Service API
 *
 */
public interface ObjectTracker {

  String OBJ_PROPKEY_REGION = "obj.region";
  String OBJ_PROPKEY_BBOX = "obj.bbox";
  String OBJ_PROPKEY_CENTROID = "obj.centroid";
  String OBJ_PROPKEY_CENTORID_NORM = "obj.centroid.norm";
  String OBJ_PROPKEY_WEIGHT = "obj.weight";
  String OBJ_PROPKEY_COLOR_HISTOGRAM = "color.histogram";
  String OBJ_PROPKEY_MOVEMENT = "obj.movement";
  String OBJ_PROPKEY_COLOR = "obj.color";
  String OBJ_PROPKEY_INSCENE = "obj.in_scene";

  OCLSignal getSignal();

  TrackerObject getObject(int id);

  boolean isCurrentlyTracked(TrackerObject object);

  void discardObject(TrackerObject object);

  Map<Integer, TrackerObject> getAllObjects();

  List<TrackerObject> getCurrentlyTracked();

}
