package cv.lecturesight.videoanalysis.foreground.impl;

import cv.lecturesight.display.DisplayService;
import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.videoanalysis.foreground.ui", immediate = true)
@Service
@Properties({
  @Property(name = "lecturesight.gui.title", value = "Foreground Model")
})
public class ForegroundModelUI implements UserInterface {
  
  @Reference
  DisplayService dsps;
  @Reference
  ForegroundService service;
  
  JPanel ui;
  
  protected void activate(ComponentContext cc) {
    ui = new ForegroundModelUIPanel(dsps);
  }

  @Override
  public JPanel getPanel() {
    return ui;
  }
}
