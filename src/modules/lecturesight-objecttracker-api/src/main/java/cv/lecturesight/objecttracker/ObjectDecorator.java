package cv.lecturesight.objecttracker;

public interface ObjectDecorator {

  /** Examine the given <code>Region</code> and change object properties
   *  based on result.
   * 
   * @param object TrackerObject to examine (and augment)
   */
  void examine(TrackerObject object);
}
