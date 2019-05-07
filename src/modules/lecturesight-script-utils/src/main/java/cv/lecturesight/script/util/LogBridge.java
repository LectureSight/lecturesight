package cv.lecturesight.script.util;

import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptingService;

import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

public class LogBridge implements ScriptBridge {

  @Setter
  ScriptingService engine;

  protected void activate(ComponentContext cc) {
    engine.registerSerivceObject("Log", this, null);
  }

  public void debug(String msg) {
    Logger.debug(msg);
  }

  public void info(String msg) {
    Logger.info(msg);
  }

  public void warn(String msg) {
    Logger.warn(msg);
  }

  public void error(String msg) {
    Logger.error(msg);
  }
}
