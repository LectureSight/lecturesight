package cv.lecturesight.util.log.listener;

import cv.lecturesight.util.log.formater.ecma48.ECMA48LogEntryFormater;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

/** A simple console output LogListener
 *
 */
public class ConsoleOutputLogListener implements LogListener {

  private LogEntryFormater formater = new ECMA48LogEntryFormater();   // TODO make this configurable

  @Override
  public void logged(LogEntry entry) {
    System.out.println(formater.formatEntry(entry));
  }
}
