package cv.lecturesight.scheduler.ical;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * A very limited ICal parser that does just the basic stuff we need for the
 * scheduler module.
 */
public final class ICalendar {

  private ICalendar() {};

  // String constants
  static final String VEVENT_BEGIN = "BEGIN:VEVENT";
  static final String VEVENT_END = "END:VEVENT";
  static final String DTSTART = "DTSTART:";
  static final String DTEND = "DTEND:";
  static final String SUMMARY = "SUMMARY:";
  static final String LOCATION = "LOCATION:";
  static final String UID = "UID:";

  // Date format used in Matterhorn ICal files
  static DateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

  /**
   * Parses the iCal delivered by <code>stream</code> and returns a <code>List</code>
   * of all sane (having start and end time) events found.
   *
   * @param stream that delivers the iCal data
   * @return List of VEvents that could be parsed
   * @throws IOException
   * @throws ICalendarException
   */
  public static List<VEvent> parseVEvents(InputStream stream) throws IOException, ICalendarException {
    List<VEvent> result = new LinkedList<VEvent>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

    String line;
    VEvent current = null;
    int lineNumber = 0;
    while ((line = reader.readLine()) != null) {
      lineNumber++;
      line = line.trim();   // just to be sure

      // VEVENT:BEGIN
      if (line.startsWith(VEVENT_BEGIN)) {
        if (current != null) {
          throw new ICalendarException("Begin of new event before end of current event at line " + lineNumber);
        }
        current = new VEvent();

        // DTSTART
      } else if (line.startsWith(DTSTART)) {
        if (current == null) {
          throw new ICalendarException("Start date without event at line " + lineNumber);
        }
        try {
          current.start = format.parse(line.substring(DTSTART.length()));
        } catch (ParseException e) {
          throw new ICalendarException("Unable to parse start date at line " + lineNumber, e);
        }

        // DTEND
      } else if (line.startsWith(DTEND)) {
        if (current == null) {
          throw new ICalendarException("End date without event at line " + lineNumber);
        }
        try {
          current.end = format.parse(line.substring(DTEND.length()));
        } catch (ParseException e) {
          throw new ICalendarException("Unable to parse start date at line " + lineNumber, e);
        }

        // SUMMARY
      } else if (line.startsWith(SUMMARY)) {
        if (current == null) {
          throw new ICalendarException("Summary without event at line " + lineNumber);
        }
        current.summary = line.substring(SUMMARY.length());

        // LOCATION
      } else if (line.startsWith(LOCATION)) {
        if (current == null) {
          throw new ICalendarException("Location without event at line " + lineNumber);
        }
        current.location = line.substring(LOCATION.length());

        // UID
      } else if (line.startsWith(UID)) {
        if (current == null) {
          throw new ICalendarException("UID without event at line " + lineNumber);
        }
        current.uid = line.substring(UID.length());

        // VEVENT:END
      } else if (line.startsWith(VEVENT_END)) {
        if (current == null) {
          throw new ICalendarException("End of event without event to end at line " + lineNumber);
        }
        if (current.start == null) {
          throw new ICalendarException("Event is missing start date at line " + lineNumber);
        }
        if (current.end == null) {
          throw new ICalendarException("Event is missing end date at line " + lineNumber);
        }
        result.add(current);
        current = null;
      }
    }

    return result;
  }
}
