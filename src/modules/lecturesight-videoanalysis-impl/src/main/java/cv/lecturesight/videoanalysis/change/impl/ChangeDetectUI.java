package cv.lecturesight.videoanalysis.change.impl;

import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.util.conf.Configuration;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.videoanalysis.change.ui", immediate = true)
@Service
@Properties({
  @Property(name = "lecturesight.gui.title", value = "Change Detection")
})
public class ChangeDetectUI implements UserInterface {

  @Reference
  Configuration config;
  
  private JPanel ui;
  
  protected void activate(ComponentContext cc) {
    ui = new ChangeDetectorUIFrame(this);
  }
  
  void setThreshold(int thresh) {
    config.set(Constants.PROPKEY_THRESH, Integer.toString(thresh));
  }
  
  @Override
  public JPanel getPanel() {
    return ui;
  }
  
}
