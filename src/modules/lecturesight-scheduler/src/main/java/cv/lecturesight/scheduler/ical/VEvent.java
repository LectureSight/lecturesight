package cv.lecturesight.scheduler.ical;

import java.util.Date;

/**
 * A class representing a VEVENT from the iCal with only the properties we are
 * interested in.
 *
 */
public class VEvent {

  Date start = null;        // start time of event
  Date end = null;          // end time of event
  String summary = "";      // summary of event, here title of the Recording
  String location = "";     // location of event, here name of Capture Agent
  String uid = "";          // unique identifier of event

  public Date getStart() {
    return start;
  }

  public Date getEnd() {
    return end;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getLocation() {
    return location;
  }

  public String getUID() {
    return uid;
  }
}
