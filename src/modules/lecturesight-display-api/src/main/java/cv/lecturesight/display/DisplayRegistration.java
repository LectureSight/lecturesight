package cv.lecturesight.display;

public class DisplayRegistration {

  private static int nextId = 0;
  private final int id;
  private final Display window;
  private String sid;

  public DisplayRegistration(String sid, Display window) {
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

  public Display getWindow() {
    return window;
  }
}
