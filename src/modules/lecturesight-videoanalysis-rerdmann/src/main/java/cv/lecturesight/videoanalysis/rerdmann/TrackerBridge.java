package cv.lecturesight.videoanalysis.templ;

import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.script.util.geometry.TrackerTarget;
import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptingService;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.script.bridge.tracker", immediate = true)
@Service()
public class TrackerBridge implements ScriptBridge {

  @Reference
  ObjectTracker tracker;

  @Reference
  ScriptingService engine;

  protected void activate(ComponentContext cc) {
    engine.registerSerivceObject("Camera", this, null);
  }

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
}
