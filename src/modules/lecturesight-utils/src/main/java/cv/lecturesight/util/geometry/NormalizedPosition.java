package cv.lecturesight.util.geometry;

public class NormalizedPosition implements Cloneable {

  private float x, y;

  public NormalizedPosition() {
    this.x = 0.0f;
    this.y = 0.0f;
  }

  public NormalizedPosition(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = clampValue(x);
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = clampValue(y);
  }

  private float clampValue(float f) {
    if (f < -1.0f) {
      f = -1.0f;
    }
    if (f > 1.0f) {
      f = 1.0f;
    }
    return f;
  }

  public double distance(NormalizedPosition other) {
    return Math.sqrt(
            Math.pow(Math.abs(getX() - other.getX()), 2)
            + Math.pow(Math.abs(getY() - other.getY()), 2));
  }

  @Override
  public String toString() {
    return new StringBuilder().append(getX()).append(",").append(getY()).toString();
  }

  @Override
  public NormalizedPosition clone() {
    return new NormalizedPosition(getX(), getY());
  }
}
