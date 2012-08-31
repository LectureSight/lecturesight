package cv.lecturesight.videoanalysis.backgroundmodel.impl;

import cv.lecturesight.display.DisplayService;
import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.videoanalysis.backgroundmodel.BackgroundModel;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.videoanalysis.background.ui", immediate = true)
@Service
@Properties({
  @Property(name = "lecturesight.gui.title", value = "Background Model")
})
public class BackgroundModelUI implements UserInterface {
  
  @Reference
  DisplayService dsps;
  @Reference
  BackgroundModel service;

  JPanel ui;
          
  protected void activate(ComponentContext cc) {
    ui = new BackgroundModelUIPanel(dsps);
  }
  
  @Override
  public JPanel getPanel() {
    return ui;
  }
   
}
