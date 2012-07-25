package cv.lecturesight.decorator.head;

public class Helper {

  public static double euclidean_distance(Point x, Point y) {
    double diff1 = x.getPoint_x() - y.getPoint_x();
    double diff2 = x.getPoint_y() - y.getPoint_y();
    return Math.sqrt(diff1 * diff1 + diff2 * diff2);
  }
}
