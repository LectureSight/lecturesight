package cv.lecturesight.ui.impl;

import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.ui.DisplayRegistration;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

public class DisplayServiceFactory implements ServiceFactory {

  final static String PROPKEY_AUTOSHOW = "autoshow";

  private Log log = new Log("Display");
  private OpenCLService ocl;
  private Configuration config;
  static Map<Integer, DisplayRegistration> displays = new HashMap<Integer, DisplayRegistration>();
  static Set<String> autoShow = new TreeSet<String>();

  protected void activate(ComponentContext cc) {
    // register config commands
    DisplayCommands commandImpl = new DisplayCommands(displays);
    Dictionary<String, Object> commands = new Hashtable<String, Object>();
    commands.put("osgi.command.scope", "display");
    commands.put("osgi.command.function", DisplayCommands.commands);
    cc.getBundleContext().registerService(DisplayCommands.class.getName(), commandImpl, commands);
    log.info("Activated");
  }

  protected void deactive(ComponentContext cc) {
    log.info("Deactivated");
  }

  @Override
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    return new DisplayServiceImpl(ocl);
  }

  @Override
  public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
    // TODO dispose all
  }

  public void setOpenCL(OpenCLService service) {
    this.ocl = service;
  }

  public void setConfiguration(Configuration config) {
    this.config = config;
    try {
      String[] list = config.getList(PROPKEY_AUTOSHOW);
      autoShow.addAll(Arrays.asList(list));
    } catch (IllegalArgumentException ex) {
      log.debug("No autoshow config found");
    }
  }
}
