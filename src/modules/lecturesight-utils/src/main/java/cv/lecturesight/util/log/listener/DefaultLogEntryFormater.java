package cv.lecturesight.util.log.listener;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

public class DefaultLogEntryFormater implements LogEntryFormater {

  private final static char NEWLINE = '\n';

  @Override
  public String formatEntry(LogEntry entry) {
    // print log message
    StringBuilder sb = new StringBuilder();
    sb.append(formatLevel(entry.getLevel()));
    sb.append(formatReference(entry));
    sb.append(entry.getMessage());
    //System.out.println("\033[34mThis is blue.\033[0m\n");

    // print Exception if present
    Throwable th = entry.getException();
    if (th != null) {
      sb.append(NEWLINE);
      StackTraceElement[] trace = th.getStackTrace();
      for (StackTraceElement elm : trace) {
        sb.append(elm.toString());
        sb.append(NEWLINE);
      }
      sb.append(NEWLINE);
    }

    return sb.toString();
  }

  public String formatLevel(int level) {
    switch (level) {
      case LogService.LOG_DEBUG:
        return "[ D ] ";
      case LogService.LOG_ERROR:
        return "[ E ] ";
      case LogService.LOG_INFO:
        return "[ I ] ";
      case LogService.LOG_WARNING:
        return "[ W ] ";
      default:
        return "[ ? ] ";  // should never happen
    }
  }

  public String formatReference(LogEntry entry) {
    String out = "";
    try {
      ServiceReference ref = entry.getServiceReference();
      out = (String) ref.getProperty("service.pid");
      out += " : ";
    } catch (Exception e) {
    }
    return out;
  }
}
