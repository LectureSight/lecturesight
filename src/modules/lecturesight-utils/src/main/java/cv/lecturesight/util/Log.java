package cv.lecturesight.util;

import org.osgi.service.log.LogService;

public class Log {

  private static LogService service = null;
  private String moduleName;

  public Log() {
    moduleName = "unknown";
  }

  public Log(String name) {
    moduleName = name;
  }

  public void info(String message) {
    if (service != null) {
      service.log(LogService.LOG_INFO, build(message));
    } else {
      System.out.println(build(message));
    }
  }

  public void debug(String message) {
    if (service != null) {
      service.log(LogService.LOG_DEBUG, build(message));
    } else {
      System.out.println(build(message));
    }
  }

  public void warn(String message) {
    if (service != null) {
      service.log(LogService.LOG_WARNING, build(message));
    } else {
      System.out.println(build(message));
    }
  }

  public void error(String message) {
    if (service != null) {
      service.log(LogService.LOG_ERROR, build(message));
    } else {
      System.err.println(build(message));
    }
  }

  public void error(String message, Throwable th) {
    if (service != null) {
      service.log(LogService.LOG_ERROR, message, th);
    } else {
      System.err.println(build(message));
      th.printStackTrace();
    }
  }

  private String build(String message) {
    StringBuilder sb = new StringBuilder();
    sb.append(moduleName);
    sb.append(" : ");
    sb.append(message);
    return sb.toString();
  }

  static void setLogService(LogService log) {
    service = log;
  }
}
