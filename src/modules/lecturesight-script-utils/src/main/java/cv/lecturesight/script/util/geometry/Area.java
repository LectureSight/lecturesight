package cv.lecturesight.script.util.geometry;

public class Area {

  public Position min;
  public Position max;

  public float width() {
    return max.x - min.x;
  }

  public float height() {
    return max.y - min.y;
  }

  public void onEnter(Object callback) {
  }

  public void onLeave(Object callback) {
  }
}
