package cv.lecturesight.videoanalysis.templ;

import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.script.util.geometry.TrackerTarget;
import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptingService;

import lombok.Setter;
import org.osgi.service.component.ComponentContext;

public class TrackerBridge implements ScriptBridge {

  @Setter
  ObjectTracker tracker;

  @Setter
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
