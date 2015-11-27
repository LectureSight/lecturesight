package cv.lecturesight.script.util;

import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptingService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

@Component(name = "lecturesight.script.bridge.log", immediate = true)
@Service()
public class LogBridge implements ScriptBridge {

  @Reference
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
