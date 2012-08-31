package cv.lecturesight.framesource.impl.ui;

import cv.lecturesight.display.DisplayService;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.gui.api.UserInterface;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.framesource.ui", immediate = true)
@Service
@Properties({
  @Property(name = "lecturesight.gui.title", value = "Input")
})
public class FrameSourceProviderUI implements UserInterface {
  
  @Reference
  FrameSourceProvider fsp;
  @Reference
  DisplayService dsps;
  
  JPanel ui;
  
  protected void activate(ComponentContext cc) {
    ui = new FrameSourceProviderUIPanel(dsps.getDisplayBySID("input"));
  }
  
  @Override
  public JPanel getPanel() {
    return ui;
  }
}
