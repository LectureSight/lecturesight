package cv.lecturesight.display.impl;

import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.util.Log;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

public class DisplayServiceFactory implements ServiceFactory {

  final static String PROPKEY_AUTOSHOW = "autoshow";

  private Log log = new Log("Display");
  private OpenCLService ocl;
  
  Map<DisplayRegistrationImpl, DisplayImpl> displays = new HashMap<DisplayRegistrationImpl, DisplayImpl>();

  protected void activate(ComponentContext cc) {
    registerCommands(cc);
    log.info("Activated");
  }

  private void registerCommands(ComponentContext cc) {
    DisplayCommands commandImpl = new DisplayCommands(this);
    Dictionary<String, Object> commands = new Hashtable<String, Object>();
    commands.put("osgi.command.scope", "display");
    commands.put("osgi.command.function", DisplayCommands.commands);
    cc.getBundleContext().registerService(DisplayCommands.class.getName(), commandImpl, commands);
  }
  
  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated");
  }

  @Override
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    return new DisplayServiceImpl(ocl, this);
  }

  @Override
  public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
    // TODO dispose all
  }

  public void setOpenCL(OpenCLService service) {
    this.ocl = service;
  }
}
