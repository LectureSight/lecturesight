package cv.lecturesight.util.log.formater.ecma48;

import cv.lecturesight.util.log.listener.LogEntryFormater;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

public class ECMA48LogEntryFormater implements LogEntryFormater {

  private final static char NEWLINE = '\n';
  private StringWriter writer;

  @Override
  public String formatEntry(LogEntry entry) {
    // print log message
    writer = new StringWriter();
    int level;
    if (entry.getMessage().startsWith("BundleEvent")
            || entry.getMessage().startsWith("ServiceEvent")
            || entry.getMessage().startsWith("FrameworkEvent")) {
      level = LogService.LOG_DEBUG;
    } else {
      level = entry.getLevel();
    }

    formatLevel(level);
    formatReference(entry);
    writer.write(entry.getMessage());

    // print Exception if present
    Throwable th = entry.getException();
    if (th != null) {
      writer.write("\n\n");
      PrintWriter pw = new PrintWriter(writer);
      th.printStackTrace(pw);
    }
    writer.write(ECMA48.RESET);
    return writer.toString();
  }

  public void formatLevel(int level) {
    switch (level) {
      case LogService.LOG_DEBUG:
        buildLevelIndicator("D", ECMA48.DARKGREY);
        break;
      case LogService.LOG_ERROR:
        buildLevelIndicator("E", ECMA48.LIGHTRED);
        break;
      case LogService.LOG_INFO:
        buildLevelIndicator("i", ECMA48.RESET);
        break;
      case LogService.LOG_WARNING:
        buildLevelIndicator("W", ECMA48.YELLOW);
        break;
      default:
        buildLevelIndicator("D", ECMA48.RESET);  // should never happen
    }
  }

  public void buildLevelIndicator(String str, String cmd) {
    writer.write(ECMA48.BOLD);
    writer.write(ECMA48.DARKGREY);
    writer.write("[ ");
    writer.write(cmd);
    writer.write(ECMA48.BOLD);
    writer.write(str);
    writer.write(ECMA48.RESET);
    writer.write(ECMA48.BOLD);
    writer.write(ECMA48.DARKGREY);
    writer.write(" ] ");
    writer.write(ECMA48.RESET);
    writer.write(cmd);
  }

  public void formatReference(LogEntry entry) {
    try {
      ServiceReference ref = entry.getServiceReference();
      String pid = (String) ref.getProperty("service.pid");
      if (pid != null) {
        writer.write(pid);
        writer.write(" : ");
      }
    } catch (Exception e) {
    }
  }
}
