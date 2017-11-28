package cv.lecturesight.ptz.steering.relativemove;

import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptingService;
import cv.lecturesight.util.geometry.NormalizedPosition;

import org.osgi.service.component.ComponentContext;

public class CameraBridge implements ScriptBridge, MovementListener {

  public class MinMaxCurrent {

    public float min;
    public float max;
    public float current;
  }

  public MinMaxCurrent panSpeed = new MinMaxCurrent();
  public MinMaxCurrent tiltSpeed = new MinMaxCurrent();

  private ScriptingService engine;
  private CameraSteeringWorkerRelativeMove camera;

  public CameraBridge(CameraSteeringWorkerRelativeMove sw, ScriptingService engine) {
    this.camera = sw;
    this.engine = engine;
    this.panSpeed.min = 1;
    this.panSpeed.max = camera.maxspeed_pan;
    this.panSpeed.current = 0;
    this.tiltSpeed.min = 1;
    this.tiltSpeed.max = camera.maxspeed_tilt;
    this.tiltSpeed.current = 0;
  }

  protected void activate(ComponentContext cc) {
    camera.addMoveListener(this);
  }

  protected void deactivate(ComponentContext cc) {
    camera.removeMoveListener(this);
  }

  public NormalizedPosition targetPos() {
    return camera.getTargetPosition();
  }

  public NormalizedPosition currentPos() {
    return camera.getActualPosition();
  }

  public float zoom() {
    return camera.getZoom();
  }

  public void zoom(float zoom) {
    camera.setZoom(zoom);
  }

  private Object startMoveCallback = null;
  private Object stopMoveCallback = null;

  public void onStartMove(Object callback) {
    this.startMoveCallback = callback;
  }

  public void onStopMove(Object callback) {
    this.stopMoveCallback = callback;
  }

  @Override
  public void moveStart(NormalizedPosition current, NormalizedPosition target) {
    if (startMoveCallback != null) {
      engine.invokeMethod(startMoveCallback, new Object[] {current, target});
    }
  }

  @Override
  public void moveStop(NormalizedPosition current, NormalizedPosition target) {
    if (stopMoveCallback != null) {
      engine.invokeMethod(stopMoveCallback, new Object[] {current, target});
    }
  }
}
