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
    START_OPERATOR,
    STOP_OPERATOR
  }
  
  Long time;        // time of the event
  Action action;    // action that should be executed
  String uid;       // event id for which the action is being executed

  /** 
   * Returns new Event instance, sets time and action
   *
   * @param time time of the event
   * @param action action that should be executed
   */
  public Event(long time, Action action, String uid) {
    this.time = time;
    this.action = action;
    this.uid = uid;
  }

  public long getTime() {
    return time;
  }

  public Action getAction() {
    return action;
  }

  public String getUID() {
    return uid;
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
