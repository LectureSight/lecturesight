package cv.lecturesight.cameraoperator.simple;

import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.ptz.api.PTZCamera;
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
  PTZCamera camera;
  
  private CameraMovementModel model = new CameraMovementModel();
  private CameraControlUI ui;
  private Thread worker;
  
  protected void activate(ComponentContext cc) throws Exception {
    log.info("Activated");
    ui = new CameraControlUI(model);
    ui.setVisible(true);
    worker = new Thread(new SteeringWorker(500));
    worker.start();
  }
  
  protected void deactivate(ComponentContext cc) {
    ui.setVisible(false);
    worker.interrupt();
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
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  private class SteeringWorker implements Runnable {
    
    int interval;
    boolean running = true;
    
    public SteeringWorker(int interval) {
      this.interval = interval;
    }

    @Override
    public void run() {
      while (running) {
        try {
          float diffX = model.getActualPosition().getX() - model.getTargetPosition().getX();
          float diffY = model.getActualPosition().getY() - model.getTargetPosition().getY();
          
          Thread.sleep(interval);
        } catch (InterruptedException e) {
          running = false;
        }
      }
    }
    
    public void interrupt() {
      running = false;
    }
  }
}
