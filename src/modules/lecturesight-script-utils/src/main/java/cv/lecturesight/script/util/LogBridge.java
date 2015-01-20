package cv.lecturesight.script.util;

import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptParent;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

@Component(name="lecturesight.script.util.log", immediate=true)
@Service()
@Properties({
 @Property(name="bridge.name", value="Log"),
 @Property(name="bridge.imports", value="")  
})
public class LogBridge implements ScriptBridge {

  ScriptParent parent;
  
  public void debug(String msg) {
    parent.getLogger().debug(msg);
  }
  
  public void info(String msg) {
    parent.getLogger().info(msg);
  }
  
  public void warn(String msg) {
    parent.getLogger().warn(msg);
  }
  
  public void error(String msg) {
    parent.getLogger().error(msg);
  }
  
  @Override
  public void setScriptParent(ScriptParent p) {
    this.parent = p;
  }
}
