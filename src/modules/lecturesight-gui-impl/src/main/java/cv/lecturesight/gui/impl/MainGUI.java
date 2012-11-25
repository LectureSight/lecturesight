package cv.lecturesight.gui.impl;

import cv.lecturesight.display.DisplayRegistration;
import cv.lecturesight.display.DisplayRegistrationListener;
import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.util.Log;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;

@Component(name="lecturesight.gui", immediate=true)
@Service
public class MainGUI implements DisplayRegistrationListener {
  
  Log log = new Log("UI");
  UserInterfaceTracker uiTracker;
  MainGUIFrame window;
  
  protected void activate(ComponentContext cc) {
    log.info("Activated");
    window = new MainGUIFrame();
    window.setVisible(true);
    uiTracker = new UserInterfaceTracker(cc.getBundleContext());
    uiTracker.open();
  }
  
  protected void deactivate(ComponentContext cc) {
    uiTracker.close();
    log.info("Deactivated");
  }
  
  void install(UserInterface ui, String title) {
    log.info("Installing interface \"" + title + "\": " + ui);
    window.addController(ui, title);
  }
  
  void uninstall(UserInterface ui) {
    log.info("Uninstalling " + ui.getClass().getName());
    window.removeController(ui);
  }

  @Override
  public void displayAdded(DisplayRegistration reg) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void displayRemoved(DisplayRegistration reg) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  private class UserInterfaceTracker extends ServiceTracker {
    
    public UserInterfaceTracker(BundleContext bc) {
      super(bc, UserInterface.class.getName(), null);
    }
    
    @Override
    public Object addingService(ServiceReference ref) {
      UserInterface ui = (UserInterface)context.getService(ref);
      String title = (String)ref.getProperty("lecturesight.gui.title");
      install(ui, title);
      return ui;
    }
    
    @Override
    public void removedService(ServiceReference ref, Object so) {
      uninstall((UserInterface)so);
    }
  }
}
