package cv.lecturesight.decorator.head;

/**
 *
 * @author Alex
 */
public class Point {

  double x;
  double y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void setPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public void setPoint(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getPoint_x() {
    return this.x;
  }

  public double getPoint_y() {
    return this.y;
  }

  @Override
  public String toString() {
    return "X: "+x+" Y: "+y;
  }

}
