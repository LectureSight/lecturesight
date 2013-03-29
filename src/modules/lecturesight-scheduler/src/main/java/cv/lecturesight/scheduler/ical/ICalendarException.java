package cv.lecturesight.scheduler.ical;

/** 
 * An Exception that is thrown by <code>ICalandar</code> in case of parser errors.
 * 
 */
class ICalendarException extends Exception {

  public ICalendarException(String message) {
    super(message);
  }
  
  public ICalendarException(String message, Throwable th) {
    super(message,th);
  }
}
