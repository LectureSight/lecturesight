package cv.lecturesight.cameraoperator.simple;

import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.util.Log;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.cameraoperator.simple", immediate = true)
@Service
public class SimpleCameraOperator implements CameraOperator {

  Log log = new Log("Simple Camera Operator");
  
  @Reference
  ObjectTracker tracker;
  
  @Reference
  CameraSteeringWorker camera;
  
  int interval = 1000;

  protected void activate(ComponentContext cc) throws Exception {
    start();
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated");
  }

  @Override
  public void start() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void stop() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void reset() {
    log.info("Reset not implemented");
  }
}
