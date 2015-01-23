package cv.lecturesight.videoanalysis.templ;

import cv.lecturesight.script.util.tracker.TrackerTarget;
import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptParent;
import cv.lecturesight.videoanalysis.VideoAnalysisService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Component(name="lecturesight.script.tracker", immediate=true)
@Service()
@Properties({
 @Property(name="bridge.name", value="Tracker"),
 @Property(name="bridge.imports", value="cv.lecturesight.script.util.tracker")      // better implement warpper classes
})
public class TrackerBridge implements ScriptBridge {

  @Reference
  VideoAnalysisService tracker;        
  
  ScriptParent parent;
  
  public class Objects {
    
    public TrackerTarget[] all() {
      return null;
    }
    
    public TrackerTarget[] tracked() {
      return null;
    }
    
    public TrackerTarget[] notTracked() {
      return null;
    }

    public TrackerTarget[] olderThan(long millis) {
      return null;
    }
    
    public TrackerTarget[] youngerThan(long millis) {
      return null;
    }
  }
  
  public Objects objects = new Objects();
  
  public class Discard {
    
    public void all() {
      
    }
    
    public void allExcept(TrackerTarget obj) {
      
    }
    
    public void allExcept(TrackerTarget[] obj) {
      
    }
    
    public void tracked() {
      
    }
    
    public void notTracked() {
      
    }
    
    public void olderThan(long millis) {
      
    }
    
    public void youngerThan(long millis) {
      
    }
  }
  
  public Discard discard = new Discard();
  
  public void onNewTarget(Object callback) {
    
  }
  
  public void onTargetLost(Object callback) {
    
  }
  
  public void onTargetRediscovered(Object callback) {
    
  }
  
  @Override
  public void setScriptParent(ScriptParent p) {
    this.parent = p;
  }
  
}
