package cv.lecturesight.setup;

import cv.lecturesight.gui.api.UserInterface;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Component(name = "lecturesight.cameracalibration", immediate = true)
@Service
public class CameraCalibrationUI implements UserInterface {

  
  
  @Override
  public String getTitle() {
    return "Camera Calibration";
  }

  @Override
  public JPanel getPanel() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isResizeable() {
    return true;
  }
  
}
