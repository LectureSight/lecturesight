package cv.lecturesight.cameraoperator.scripted.bridge;

import cv.lecturesight.cameraoperator.scripted.bridge.classes.TrackerTarget;
import cv.lecturesight.objecttracker.ObjectTracker;

public class ObjectTrackerBridge {

  private ObjectTracker tracker;
  
  public ObjectTrackerBridge(ObjectTracker tracker) {
    this.tracker = tracker;
  }
  
  public int numTracked() {
    return 0;
  }
  
  public TrackerTarget[] allTargets() {
    return null;
  }
  
  public TrackerTarget[] trackedTargets() {
    return null;
  }
}
