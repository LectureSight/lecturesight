package cv.lecturesight.object;

public interface ObjectDecorator {

  /** Examine the given <code>TrackerObject</code> and change object properties
   *  based on result.
   * 
   * @param obj 
   */
  void examine(TrackerObject obj);
}
