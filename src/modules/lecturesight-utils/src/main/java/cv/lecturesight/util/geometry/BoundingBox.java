package cv.lecturesight.util.geometry;

public class BoundingBox implements Cloneable {

  private Position min, max;
  
  public BoundingBox() {
    this.min = new Position();
    this.max = new Position();
  }
  
  public BoundingBox(Position min, Position max) {
    this.min = min;
    this.max = max;
  }
  
  public int getWidth() {
    return max.getX() - min.getX();
  }
  
  public int getHeight() {
    return max.getY() - min.getY();
  }

  public Position getMin() {
    return min;
  }

  public void setMin(Position min) {
    this.min = min;
  }

  public Position getMax() {
    return max;
  }

  public void setMax(Position max) {
    this.max = max;
  }

  @Override
  public String toString() {
    return new StringBuilder()
            .append(min)
            .append("-")
            .append(max)
            .toString();
  }
  
  @Override
  public BoundingBox clone() {
    return new BoundingBox(min, max);
  }

  public boolean contains(Position p) {
    if(p.getX() < max.getX() &&
       p.getX() > min.getX() &&
       p.getY() < max.getY() &&
       p.getY() > min.getY()) {
      return true;
    }
    return false;
  }
  
}
