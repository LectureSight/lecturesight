package cv.lecturesight.script.util;

import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptingService;
import cv.lecturesight.util.Log;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.script.bridge.log", immediate = true)
@Service()
public class LogBridge implements ScriptBridge {

  @Reference
  ScriptingService engine;
  Log scriptLog;
  
  protected void activate(ComponentContext cc) {
    engine.registerSerivceObject("Log", this, null);
  }
  
  public void debug(String msg) {
    engine.getLogger().debug(msg);
  }
  
  public void info(String msg) {
    engine.getLogger().info(msg);
  }
  
  public void warn(String msg) {
    engine.getLogger().warn(msg);
  }
  
  public void error(String msg) {
    engine.getLogger().error(msg);
  }
}
