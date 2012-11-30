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
import java.awt.image.BufferedImage;

/**
 *
 * @author Alex
 */
public class ClusterStack {

  PointStack assigned_points;
  Position cluster_center;

  public ClusterStack(Position p) {
    cluster_center = new Position(p.getX(), p.getY());
    assigned_points = new PointStack();
  }

  public ClusterStack(PointStack p) {
    cluster_center = new Position(0, 0);
    assigned_points = p;
    recalculate_center();
  }

  public void push(Position e) {
    assigned_points.push(e);
  }

  public Position pop() {
    return assigned_points.pop();
  }

  public Position get_center() {
    return cluster_center;
  }

  public void print_center() {
    System.out.println("Center: x "+cluster_center.getX()+" y: "+cluster_center.getY());
  }

  /**
   * recalculates the center of the stack
   * @return boolean, true iff there was a change
   */
  public boolean recalculate_center() {
    int sum_x = 0, sum_y = 0;
    int length = assigned_points.length();

    if(length >= 1) {

      for(int j = 0; j < length; j++) {
        sum_x += assigned_points.index(j).getX();
        sum_y += assigned_points.index(j).getY();
      }

      Position old_cluster_center = cluster_center;
      cluster_center = new Position(sum_x/length, sum_y/length);
      if(Helper.euclidean_distance(cluster_center,old_cluster_center) > 0.1) return true;
    }
    return false;
  }

  public void reset() {
    assigned_points = new PointStack();
  }

  public Position[] min_max() {
    Position [] boundaries = new Position [2];
    int min_x = Integer.MAX_VALUE,
        min_y = Integer.MAX_VALUE,
        max_x = 0, max_y = 0;

    for(int i = 0; i < assigned_points.length(); i++) {
      if(assigned_points.index(i).getX() < min_x)
        min_x = assigned_points.index(i).getX();
      if(assigned_points.index(i).getY() < min_y)
        min_y = assigned_points.index(i).getY();
      if(assigned_points.index(i).getX() > max_x)
        max_x = assigned_points.index(i).getX();
      if(assigned_points.index(i).getY() > max_y)
        max_y = assigned_points.index(i).getY();
    }
    boundaries[0] = new Position(min_x,min_y);
    boundaries[1] = new Position(max_x,max_y);
    return boundaries;
  }

  /**
   * Calculates the maximum distance of assigned points to the cluster center
   * @return double radius
   */
  public double radius() {
    int g = 0;
    double g1 = 0;
    for(int i = 0; i < assigned_points.length(); i++) {
      double g2 = Helper.euclidean_distance(cluster_center, assigned_points.index(i));
      if(g2 > g1) {
        g1 = g2;
        g = i;
      }
    }
    return g1;
  }


  public void print_ap(BufferedImage image) {
    for(int i = 0; i < assigned_points.length(); i++) {
      image.setRGB((int)assigned_points.index(i).getX(),
              (int)assigned_points.index(i).getY(), 0xFF0000);
    }
  }
}
