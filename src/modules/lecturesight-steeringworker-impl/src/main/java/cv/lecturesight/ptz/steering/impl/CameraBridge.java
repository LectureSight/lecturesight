package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptParent;
import cv.lecturesight.util.geometry.NormalizedPosition;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.script.camera", immediate = true)
@Service()
@Properties({
  @Property(name = "bridge.name", value = "Camera"),
  @Property(name = "bridge.imports", value = {"cv.lecturesight.util.geometry"})
})
public class CameraBridge implements ScriptBridge {

  public class MinMax {

    public float min;
    public float max;
  }

  public MinMax panSpeed = new MinMax();
  public MinMax tiltSpeed = new MinMax();
  public MinMax zoomSpeed = new MinMax();
  
  private ScriptParent parent;
  
  @Reference
  private CameraSteeringWorker steer;
  
  protected void activate(ComponentContext cc) {
    this.panSpeed.min = 1;
    this.panSpeed.max = ((CameraSteeringWorkerImpl)steer).maxspeed_pan;
    this.tiltSpeed.min = 1;
    this.tiltSpeed.max = ((CameraSteeringWorkerImpl)steer).maxspeed_tilt;
    this.zoomSpeed.min = 1;
    this.zoomSpeed.max = ((CameraSteeringWorkerImpl)steer).maxspeed_zoom;
  }
  
  public NormalizedPosition targetPos() {
    return steer.getTargetPosition();
  }
  
  public NormalizedPosition currentPos() {
    return steer.getActualPosition();
  }
  
  public float zoom() {
    return steer.getZoom();
  }
  
  public void zoom(float zoom) {
    steer.setZoom(zoom);
  }

  private Object startMovCallback = null;
  private Object stopMovCallback = null;
  
  public void onStartMove(Object callback) {    // TODO find right hook in SteeringWorker for this event!
    this.startMovCallback = callback;
  }
  
  public void onStopMove(Object callback) {     // TODO find right hook in SteeringWorker for this event!
    this.stopMovCallback = callback;
  }
  
  @Override
  public void setScriptParent(ScriptParent p) {
    this.parent = p;
  }
}
