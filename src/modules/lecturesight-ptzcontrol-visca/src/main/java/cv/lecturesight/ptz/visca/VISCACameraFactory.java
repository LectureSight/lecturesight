package cv.lecturesight.ptz.visca;

import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.util.DummyInterface;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Implementation of the API for general Pan-Tilt-Zoom-Cameras (based on VISCA Camera Protocoll).
 * This is a factory service for VISCACamera.
 * 
 * API: @see cv.lecturesight.ptz.api.PTZCamera
 */
@Component(name = "cv.lecturesight.ptz.visca", immediate = true)
@Service
public class VISCACameraFactory implements DummyInterface {

  private final static String PROPKEY_SERIALPORTS = "ports";
  private final static String SERVICE_NAME_PREFIX = "visca-";
  
  Log logger = new Log("VISCA Factory");
  
  @Reference
  Configuration config;
  
  /** OSGI Component context */
  ComponentContext context;
  /** Comm port to service map (up to 7 services, see visca spec.)*/
  Map<String, List<VISCACamera>> services = new HashMap<String, List<VISCACamera>>();
  /** Service registrations */
  List<ServiceRegistration> registrations = new LinkedList<ServiceRegistration>();

  protected void activate(ComponentContext context) {
    this.context = context;
    int portNum = 1;
    for (String device : config.getList(PROPKEY_SERIALPORTS)) {
      initPort(SERVICE_NAME_PREFIX + portNum++, device);
    }
    logger.info("Activated");
  }

  protected void deactivate(ComponentContext context) {
    // unregister services
    for (ServiceRegistration reg : registrations) {
      reg.unregister();
    }
    // close comm port connection
    for (String key : services.keySet()) {
      VISCACamera service = services.get(key).get(0);
      if (service != null) {
        if (!service.deinitialize()) {
          logger.warn("Failed to deinitialize " + service.getPort());
        }
      }
    }
    logger.info("Deactivated");
  }

  private void initPort(String portName, String device) {
    int cam = 1;
    VISCACamera service = null;
    do {
      logger.info("Initializing camera " + cam + " on " + device);
      service = new VISCACamera(portName);
      if (!service.initialize(device, cam)) {
        logger.error("Failed to initialize camera on port " + device);
        break;
      }
      registerService(portName, service);
    } while (++cam <= service.getConnectedCams());
  }

  /**
   * Register VISCACamera service with OSGI framework.
   * 
   * @param portName port name (from configuration)
   * @param service VISCACamera service
   */
  private void registerService(String portName, VISCACamera service) {
    // create service properties
    Dictionary props = new Hashtable();
    props.put("name", portName + "-" + service.getCamNo());
    props.put("type", "visca");
    props.put("port", service.getPort());
    props.put("cam", service.getCamNo());

    ServiceRegistration registration = context.getBundleContext().registerService(
            new String[]{PTZCamera.class.getName(), VISCACamera.class.getName()},
            service, props);

    if (services.containsKey(portName)) {
      services.get(portName).add(service);
    } else {
      List<VISCACamera> serviceList = new LinkedList<VISCACamera>();
      serviceList.add(service);
      services.put(portName, serviceList);
    }
    registrations.add(registration);

    logger.info("Registered camera: " + portName);
  }
}
