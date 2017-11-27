package cv.lecturesight.script.util.geometry;

public class TrackerTarget {

  public boolean isTracked() {
    return false;
  }

  public long firstSeen() {
    return 0;
  }

  public long lastSeen() {
    return 0;
  }

  public long lastMoved() {
    return 0;
  }

  public Position centroid() {
    return null;
  }

  public Area bbox() {
    return null;
  }

  public void onMove(Object callback) {

  }

  public Object getProperty(String key) {
    return null;
  }
}
