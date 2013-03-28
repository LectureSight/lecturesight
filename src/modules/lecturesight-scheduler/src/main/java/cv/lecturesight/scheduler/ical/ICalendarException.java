package cv.lecturesight.scheduler.ical;

class ICalendarException extends Exception {

  public ICalendarException(String message) {
    super(message);
  }
  
  public ICalendarException(String message, Throwable th) {
    super(message,th);
  }
}
