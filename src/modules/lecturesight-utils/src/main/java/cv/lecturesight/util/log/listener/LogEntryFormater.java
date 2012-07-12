package cv.lecturesight.util.log.listener;

import org.osgi.service.log.LogEntry;

public interface LogEntryFormater {

  String formatEntry(LogEntry entry);

}
