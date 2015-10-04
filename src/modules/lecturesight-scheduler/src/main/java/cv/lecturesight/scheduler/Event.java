package cv.lecturesight.scheduler;

/** 
 * A class representing a point in time and an action that should happen at
 * this instant.
 */
public class Event implements Comparable {

  // Types of actions
  enum Action {
    START_TRACKING,
    STOP_TRACKING,
    START_CAMERACONTROL,
    STOP_CAMERACONTROL
  }
  
  Long time;        // time of the event
  Action action;    // action that should be executed

  /** 
   * Returns new Event instance, sets time and action
   *
   * @param time time of the event
   * @param action action that should be executed
   */
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
    int time_c = time.compareTo(((Event)other).time);

    if (time_c == 0)
	return action.compareTo(((Event)other).action);
    else
    	return time_c;
  }
}
