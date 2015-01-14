package cv.lecturesight.script.util;

import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptParent;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name="lecturesight.script.util.time", immediate=true)
@Service()
@Properties({
 @Property(name="bridge.name", value="Time"),
 @Property(name="bridge.imports", value="")  
})
public class TimeBridge implements ScriptBridge {
  
  @Reference
  FrameSourceProvider fsp;
  FrameSource fsrc;
  
  ScriptParent parent;
  
  protected void activate(ComponentContext cc) {
    fsrc = fsp.getFrameSource();
  }

  public long now() {
    return System.currentTimeMillis();
  }
  
  public long start() {
    return 0;             // TODO implement
  }
  
  public long currentFrame() {
    return fsrc.getFrameNumber();
  }

  public double fps() {
    return fsrc.getFPS();
  }

  public void sleep(int time, Object callback) {
    try {
      Thread.sleep((long)time);
    } catch (InterruptedException e) {
      //log.warn("TimeBridge.sleep() interrupted.");
    }
    parent.invokeCallback(callback, new Object[0]);
  }

  @Override
  public void setScriptParent(ScriptParent parent) {
    this.parent = parent;
  }
}
