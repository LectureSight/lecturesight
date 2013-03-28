package cv.lecturesight.scheduler.ical;

import java.util.Date;

public class VEvent {
  
  Date start = null;
  Date end = null;
  String summary = "";
  String location = "";

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
}
