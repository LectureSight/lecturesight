package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptParent;
import cv.lecturesight.util.geometry.NormalizedPosition;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

@Component(name = "lecturesight.script.camera", immediate = true)
@Service()
@Properties({
  @Property(name = "bridge.name", value = "Camera"),
  @Property(name = "bridge.imports", value = {"cv.lecturesight.util.geometry"})
})
public class CameraBridge implements ScriptBridge, MovementListener {

  public class MinMaxCurrent {

    public float min;
    public float max;
    public float current;
  }

  public MinMaxCurrent panSpeed = new MinMaxCurrent();
  public MinMaxCurrent tiltSpeed = new MinMaxCurrent();

  private ScriptParent parent;

  private CameraSteeringWorkerImpl steer;

  public CameraBridge(CameraSteeringWorkerImpl sw) {
    this.steer = sw;
    this.panSpeed.min = 1;
    this.panSpeed.max = steer.maxspeed_pan;
    this.panSpeed.current = 0;
    this.tiltSpeed.min = 1;
    this.tiltSpeed.max = steer.maxspeed_tilt;
    this.tiltSpeed.current = 0;
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

  public void onStartMove(Object callback) {
    this.startMovCallback = callback;
  }

  public void onStopMove(Object callback) {
    this.stopMovCallback = callback;
  }

  @Override
  public void moveStart(NormalizedPosition current, NormalizedPosition target) {
    if (startMovCallback != null) {
      parent.invokeCallback(startMovCallback, new Object[] {current, target});
    }
  }

  @Override
  public void moveStop(NormalizedPosition current, NormalizedPosition target) {
    if (stopMovCallback != null) {
      parent.invokeCallback(stopMovCallback, new Object[] {current, target});
    }
  }

  @Override
  public void setScriptParent(ScriptParent p) {
    this.parent = p;
  }
}
