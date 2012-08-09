package cv.lecturesight.cameraoperator.simple;

import cv.lecturesight.ptz.steering.impl.CameraMovementModel;
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
    worker = new Thread(new SteeringWorker(200));
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
    float dt = 0.2f, dt2 = 0.05f, dt_xy = 0.02f;

    public SteeringWorker(int interval) {
      this.interval = interval;
    }

    @Override
    public void run() {
      while (running) {
        try {
          // get current position of camera
          try {
            model.getActualPosition().setX(camera.getPan());
            model.getActualPosition().setY(camera.getTilt());
            ui.repaint();
          } catch (Exception e) {
            log.warn("Unable to update actual postion: " + e.getMessage());
          }

          // compute raw, absolute and euclidean distance btw actual position and target position
          float dx = model.getActualPosition().getX() - model.getTargetPosition().getX();
          float dx_abs = (float)Math.abs(dx);
          float dy = model.getActualPosition().getY() - model.getTargetPosition().getY();
          float dy_abs = (float)Math.abs(dy);
          float d = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

          try {
            if (d > dt2) {
              
              if (dx_abs < dt) {
                float r = dx_abs / dt;
                camera.setPanSpeed(r);  
              } else {
                camera.setPanSpeed(1.0f);
              }
              
              if (dy_abs < dt) {
                float r = dy_abs / dt;
                camera.setTiltSpeed(r);  
              } else {
                camera.setTiltSpeed(1.0f);
              }
              
              if (dx > dt_xy && dy < dt_xy) {
                camera.moveUpLeft();
                camera.limitPanTiltDownLeft(model.getTargetPosition().getX(), -1.0f);
                camera.limitPanTiltUpRight(1.0f, model.getTargetPosition().getY());

              } else if (dx < dt_xy && dy < dt_xy) {
                camera.moveUpRight();
                camera.limitPanTiltDownLeft(-1.0f, -1.0f);
                camera.limitPanTiltUpRight(model.getTargetPosition().getX(), model.getTargetPosition().getY());

              } else if (dx > dt_xy && dy > dt_xy) {
                camera.moveDownLeft();
                camera.limitPanTiltDownLeft(model.getTargetPosition().getX(), model.getTargetPosition().getY());
                camera.limitPanTiltUpRight(1.0f, 1.0f);

              } else if (dx < dt_xy && dy > dt_xy) {
                camera.moveDownRight();
                camera.limitPanTiltDownLeft(-1.0f, model.getTargetPosition().getY());
                camera.limitPanTiltUpRight(model.getTargetPosition().getX(), 1.0f);

              } else if (dx < dt_xy) {
                camera.moveRight();
                camera.limitPanTiltDownLeft(-1.0f, model.getTargetPosition().getY());
                camera.limitPanTiltUpRight(model.getTargetPosition().getX(), model.getTargetPosition().getY());

              } else if (dx > dt_xy) {
                camera.moveLeft();
                camera.limitPanTiltDownLeft(model.getTargetPosition().getX(), model.getTargetPosition().getY());
                camera.limitPanTiltUpRight(1.0f, model.getTargetPosition().getY());

              } else if (dy < dt_xy) {
                camera.moveUp();
                camera.limitPanTiltDownLeft(model.getTargetPosition().getX(), -1.0f);
                camera.limitPanTiltUpRight(model.getTargetPosition().getX(), model.getTargetPosition().getY());

              } else if (dy > dt_xy) {
                camera.moveDown();
                camera.limitPanTiltDownLeft(model.getTargetPosition().getX(), model.getTargetPosition().getY());
                camera.limitPanTiltUpRight(model.getTargetPosition().getX(), 1.0f);

              }
            } else {
              camera.stopMove();
              //camera.move(model.getTargetPosition().getX(), model.getTargetPosition().getY());
            }

          } catch (Exception e) {
            log.warn(e.getMessage());
          }

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
