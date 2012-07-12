package cv.lecturesight.ui;

public class DisplayRegistration {

  private static int nextId = 0;
  private final int id;
  private final DisplayWindow window;
  private String sid;

  public DisplayRegistration(String sid, DisplayWindow window) {
    this.sid = sid;
    this.id = nextId++;
    this.window = window;
  }

  public int getID() {
    return id;
  }

  public String getSID() {
    return sid;
  }

  public DisplayWindow getWindow() {
    return window;
  }
}
