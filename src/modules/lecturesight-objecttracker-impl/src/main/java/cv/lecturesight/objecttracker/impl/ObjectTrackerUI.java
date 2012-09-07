package cv.lecturesight.objecttracker.impl;

import cv.lecturesight.display.DisplayService;
import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.objecttracker.ObjectTracker;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.objecttracker.impl.ui", immediate = true)
@Service
@Properties({
  @Property(name = "lecturesight.gui.title", value = "Object Tracker")
})
public class ObjectTrackerUI implements UserInterface {

  @Reference
  ObjectTracker oTracker;
  @Reference
  DisplayService dsps;
  private JPanel ui;

  protected void activate(ComponentContext cc) {
    ui = new ObjectTrackerUIPanel(dsps.getDisplayBySID("input"), oTracker);
  }

  @Override
  public JPanel getPanel() {
    return ui;
  }
}
