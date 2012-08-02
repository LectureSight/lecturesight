package cv.lecturesight.util.geometry;

public class Position implements Cloneable {
  
  private int x, y;
  
  public Position() {
    this.x = 0;
    this.y = 0;
  }
  
  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }
  
  public double distance(Position other) {
    return Math.sqrt(
            Math.pow(Math.abs(x - other.x), 2) +
            Math.pow(Math.abs(y - other.y), 2));
  }
  
  @Override
  public String toString() {
    return new StringBuilder()
            .append(x)
            .append(",")
            .append(y)
            .toString();
  }
  
  @Override
  public Position clone() {
    return new Position(x,y);
  }
}