package cv.lecturesight.decorator.head;

import java.awt.image.BufferedImage;

/**
 *
 * @author Alex
 */
public class ClusterStack {

  PointStack assigned_points;
  Point cluster_center;

  public ClusterStack(Point p) {
    cluster_center = new Point(p.getPoint_x(), p.getPoint_y());
    assigned_points = new PointStack();
  }

  public ClusterStack(PointStack p) {
    cluster_center = new Point(0.0, 0.0);
    assigned_points = p;
    recalculate_center();
  }

  public void push(Point e) {
    assigned_points.push(e);
  }

  public Point pop() {
    return assigned_points.pop();
  }

  public Point get_center() {
    return cluster_center;
  }

  public void print_center() {
    System.out.println("Center: x "+cluster_center.getPoint_x()+" y: "+cluster_center.getPoint_y());
  }

  /**
   * recalculates the center of the stack
   * @return boolean, true iff there was a change
   */
  public boolean recalculate_center() {
    double sum_x = 0, sum_y = 0;
    int length = assigned_points.length();

    if(length >= 1) {

      for(int j = 0; j < length; j++) {
        sum_x += assigned_points.index(j).getPoint_x();
        sum_y += assigned_points.index(j).getPoint_y();
      }

      Point old_cluster_center = cluster_center;
      cluster_center = new Point(sum_x/length, sum_y/length);
      if(Helper.euclidean_distance(cluster_center,old_cluster_center) > 0.1) return true;
    }
    return false;
  }

  public void reset() {
    assigned_points = new PointStack();
  }

  public Point[] min_max() {
    Point [] boundaries = new Point [2];
    double min_x = Double.MAX_VALUE, min_y = Double.MAX_VALUE, max_x = 0, max_y = 0;
    for(int i = 0; i < assigned_points.length(); i++) {
      if(assigned_points.index(i).getPoint_x() < min_x)
        min_x = assigned_points.index(i).getPoint_x();
      if(assigned_points.index(i).getPoint_y() < min_y)
        min_y = assigned_points.index(i).getPoint_y();
      if(assigned_points.index(i).getPoint_x() > max_x)
        max_x = assigned_points.index(i).getPoint_x();
      if(assigned_points.index(i).getPoint_y() > max_y)
        max_y = assigned_points.index(i).getPoint_y();
    }
    boundaries[0] = new Point(min_x,min_y);
    boundaries[1] = new Point(max_x,max_y);
    return boundaries;
  }

  public void print_ap(BufferedImage image) {
    for(int i = 0; i < assigned_points.length(); i++) {
      image.setRGB((int)assigned_points.index(i).getPoint_x(),
              (int)assigned_points.index(i).getPoint_y(), 0xFF0000);
    }
  }
}
