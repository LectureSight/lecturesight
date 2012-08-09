package cv.lecturesight.util.geometry;

public class CoordinatesNormalization {

  private int maxX, maxY;

  public CoordinatesNormalization(int maxX, int maxY) {
    this.maxX = maxX;
    this.maxY = maxY;
  }

  public float normalizeX(int x) {
    return ((float) x / (float) maxX) * 2.0f - 1.0f;
  }

  public float normalizeY(int y) {
    return ((float) y / (float) maxY) * 2.0f - 1.0f;
  }

  public int denormalizeX(float x) {
    return (int) (((x + 1) / 2) * maxX);
  }

  public int denormalizeY(float y) {
    return (int) (((y + 1) / 2) * maxY);
  }

  public NormalizedPosition toNormalized(Position pos) {
    return new NormalizedPosition(
            normalizeX(pos.getX()),
            normalizeY(pos.getY()));
  }

  public Position fromNormalized(NormalizedPosition pos) {
    return new Position(
            denormalizeX(pos.getX()),
            denormalizeY(pos.getY()));
  }

  public int getMaxX() {
    return maxX;
  }

  public void setMaxX(int maxX) {
    if (maxX > 0) {
      this.maxX = maxX;
    } else {
      this.maxX = 1;
    }
  }

  public int getMaxY() {
    return maxY;
  }

  public void setMaxY(int maxY) {
    if (maxY > 0) {
      this.maxY = maxY;
    } else {
      this.maxY = 1;
    }
  }
}
