package cv.lecturesight.script.util;

import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptingService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.script.bridge.time", immediate = true)
@Service()
public class TimeBridge implements ScriptBridge {
  
  @Reference
  FrameSourceProvider fsp;
  FrameSource fsrc;
  
  @Reference
  ScriptingService engine;
  
  protected void activate(ComponentContext cc) {
    fsrc = fsp.getFrameSource();
    engine.registerSerivceObject("Time", this, null);
  }

  public long now() {
    return System.currentTimeMillis();
  }
  
  public long start() {
    return 0;             // TODO implement: return time of script start
  }
  
  public long currentFrame() {
    return fsrc.getFrameNumber();
  }

  public double fps() {
    return fsrc.getFPS();
  }

  public void sleep(long time, Object callback) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      engine.getLogger().warn("Time.sleep interrupted.");
    }
    engine.invokeCallback(callback, new Object[0]);
  }
}
