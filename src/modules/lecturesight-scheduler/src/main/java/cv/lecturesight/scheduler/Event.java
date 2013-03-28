package cv.lecturesight.scheduler;

public class Event implements Comparable {

  enum Action {
    START_TRACKING,
    STOP_TRACKING,
    START_CAMERACONTROL,
    STOP_CAMERACONTROL
  }
  
  Long time;
  Action action;

  public Event(long time, Action action) {
    this.time = time;
    this.action = action;
  }

  public long getTime() {
    return time;
  }

  public Action getAction() {
    return action;
  }

  @Override
  public int compareTo(Object other) {
    return time.compareTo( ((Event)other).time );
  }
}
