package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.ptz.steering.ui", immediate = true)
@Service
@Properties({
  @Property(name = "lecturesight.gui.title", value = "Camera Control")
})
public class CameraMovementUI implements UserInterface {

  @Reference
  CameraSteeringWorker worker;
  private JFrame window = new JFrame();
  private CameraControlPanel controlPanel;
  
  protected void activate(ComponentContext cc) {
    controlPanel = ((CameraSteeringWorkerImpl)worker).getControlPanel();
    window.setTitle("PTZ Camera Control");
    window.add(controlPanel);
    window.pack();
  }

  @Override
  public JPanel getPanel() {
    return controlPanel;
  }
}
