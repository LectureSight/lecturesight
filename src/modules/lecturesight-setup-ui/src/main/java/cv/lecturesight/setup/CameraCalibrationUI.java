package cv.lecturesight.setup;

import cv.lecturesight.display.Display;
import cv.lecturesight.display.DisplayRegistration;
import cv.lecturesight.display.DisplayService;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.framesource.FrameSourceManager;
import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.Triggerable;
import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.conf.ConfigurationService;
import java.util.Properties;
import javax.swing.JPanel;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

public class CameraCalibrationUI implements UserInterface {

  @Setter
  ConfigurationService configService;
  @Setter
  Configuration config;
  @Setter
  CameraSteeringWorker steeringWorker;
  @Setter
  PTZCamera camera;
  @Setter
  FrameSourceManager fsm;
  FrameSource productionCamera;
  @Setter
  DisplayService dsps;
  DisplayRegistration cameraDisplayRegistration;
  Display cameraDisplay;
  @Setter
  OpenCLService ocl;
  CameraCalibrationPanel panel = new CameraCalibrationPanel(this);
  boolean wasSteeringBefore = false;
  int pan_fast, pan_slow, tilt_fast, tilt_slow;
  Triggerable cameraHeartbeat;

  protected void activate(ComponentContext cc) {
    pan_fast = camera.getProfile().getPanMaxSpeed();
    pan_slow = pan_fast / 3;
    pan_slow = pan_slow > 1 ? pan_slow : 1;
    tilt_fast = camera.getProfile().getTiltMaxSpeed();
    tilt_slow = tilt_fast / 3;
    tilt_fast = tilt_fast > 1 ? tilt_fast : 1;
    Logger.info("Activated");
  }

  @Override
  public String getTitle() {
    return "Camera Calibration";
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public boolean isResizeable() {
    return true;
  }

  void takeControl() {
    // disbale control of steeringWorker if enabled, save former state
    if (steeringWorker != null) {
      wasSteeringBefore = steeringWorker.isSteering();
      steeringWorker.setSteering(false);
    }

    // activate production camera frame source, set ui class as custom renderer if configured
    if (!config.get(Constants.PROPKEY_CALIB_FRAMESOURCE).isEmpty()) {
      try {
        productionCamera = fsm.createFrameSource(config.get(Constants.PROPKEY_CALIB_FRAMESOURCE));
        cameraDisplayRegistration = dsps.registerDisplay("Production Camera",
                productionCamera.getImage(), productionCamera.getSignal());
        panel.setCameraDisplay(dsps.getDisplayByRegistration(cameraDisplayRegistration));
        cameraHeartbeat = new Triggerable() {
          @Override
          public void triggered(OCLSignal signal) {
            try {
              Thread.sleep(100);
              productionCamera.captureFrame();
            } catch (Exception e) {
              Logger.warn("Unable to capture frame. " + e.getMessage());
            }
          }
        };
        ocl.registerTriggerable(productionCamera.getSignal(), cameraHeartbeat);
      } catch (FrameSourceException e) {
        throw new IllegalStateException("Failed to open frame source: " + config.get(Constants.PROPKEY_CALIB_FRAMESOURCE));
      }
    }
  }

  void abandonControl() {
    // bring steering worker to last state
    if (steeringWorker != null) {
      steeringWorker.setSteering(wasSteeringBefore);
    }

    // destroy production camera frame source and remove video display
    if (!config.get(Constants.PROPKEY_CALIB_FRAMESOURCE).isEmpty()) {
      panel.removeCameraDisplay();
      ocl.unregisterTriggerable(productionCamera.getSignal(), cameraHeartbeat);
      try {
        fsm.destroyFrameSource(productionCamera);
      } catch (FrameSourceException e) {
        Logger.warn("Unable to destroy FrameSource for production camera. " + e.getMessage());
      }
    }
  }

  void saveParameters(int left, int right, int top, int bottom) {
    Properties sysConfig = configService.getSystemConfiguration();
    sysConfig.setProperty(Constants.PROPKEY_LIMIT_LEFT, Integer.toString(left));
    sysConfig.setProperty(Constants.PROPKEY_LIMIT_RIGHT, Integer.toString(right));
    sysConfig.setProperty(Constants.PROPKEY_LIMIT_TOP, Integer.toString(top));
    sysConfig.setProperty(Constants.PROPKEY_LIMIT_BOTTOM, Integer.toString(bottom));
    configService.notifyListeners();
  }
}
