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
package cv.lecturesight.regiontracker.impl;

import cv.lecturesight.regiontracker.Region;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.util.HashSet;
import java.util.Set;

public class RegionImpl implements Region {

  long firstSeenTS, lastMoveTS;
  BoundingBox bbox = null;
  Position centroid = null;
  int weight = 0;
  int label;
  double tr_x = 0;
  double tr_y = 0;
  double axis = 0;
  Region group = null;
  Set<Region> members = new HashSet<Region>();
  boolean splitter = false;

  public RegionImpl(int label, long time, Position centroid, BoundingBox bbox, int weight) {
    this.firstSeenTS = time;
    this.lastMoveTS = time;
    this.label = label;
    this.centroid = centroid;
    this.bbox = bbox;
    this.weight = weight;
  }
  
  @Override
  public void update(int label, long time, Position centroid, BoundingBox bbox, int weight) {
    this.label = label;
    if (this.centroid != centroid) {
      lastMoveTS = time;
    }
    this.centroid = centroid;
    this.bbox = bbox;
    this.weight = weight;
  }
  
  @Override
  public int getLabel() {
    return label;
  }
  
  public void setAxis(double dist) {
    axis = dist;
  }
  
  public double getAxis() {
    return axis;
  }

  @Override
  public long getFirstSeenTime() {
    return firstSeenTS;
  }

  @Override
  public long getLastMoveTime() {
    return lastMoveTS;
  }
  
  @Override
  public BoundingBox getBoundingBox() {
    return bbox;
  }

  @Override
  public Position getCentroid() {
    return centroid;
  }

  @Override
  public int getWeight() {
    return weight;
  }

  @Override
  public boolean isSplitter() {
    return splitter;
  }
  
  @Override
  public boolean isGroup() {
    return !members.isEmpty();
  }

  @Override
  public boolean isGroupMember() {
    return group != null;
  }

  @Override
  public Region getGroup() {
    return group;
  }

  @Override
  public Set<Region> getGroupMembers() {
    Set<Region> out = new HashSet<Region>();
    out.addAll(members);
    return out;
  }
  
  @Override
  public String toString() {
    return new StringBuilder()
            .append("[weight=")
            .append(weight)
            .append(" box=")
            .append(bbox)
            .append(" pos=")
            .append(centroid)
            .append("]")
            .toString();
  }
}
