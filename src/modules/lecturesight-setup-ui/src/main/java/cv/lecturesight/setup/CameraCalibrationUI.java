package cv.lecturesight.setup;

import cv.lecturesight.gui.api.UserInterface;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Component(name = "lecturesight.cameracalibration", immediate = true)
@Service
public class CameraCalibrationUI implements UserInterface {

  CameraCalibrationPanel panel = new CameraCalibrationPanel(this);
  
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
  }
  
  void abandonControl() {
  }
}
