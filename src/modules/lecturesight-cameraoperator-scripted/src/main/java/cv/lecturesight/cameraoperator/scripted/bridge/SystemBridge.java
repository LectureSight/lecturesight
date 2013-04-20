package cv.lecturesight.cameraoperator.scripted.bridge;

import cv.lecturesight.util.Log;

public class SystemBridge {
  
  private Log scriptLogger;
  
  public SystemBridge(Log scriptLogger) {
    this.scriptLogger = scriptLogger;
  }

  public void print(String message) {
    System.out.print(message);
  }
  
  public void println(String message) {
    System.out.println(message);
  }
  
  public void log(String message) {
    scriptLogger.info(message);
  }
  
  public void logWarn(String message) {
    scriptLogger.warn(message);
  }
  
  public void logError(String message) {
    scriptLogger.error(message);
  }
  
  public long time() {
    return System.currentTimeMillis();
  }
}
