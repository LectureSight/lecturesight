package cv.lecturesight.ptz.visca;

import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.util.DummyInterface;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Implementation of the API for general Pan-Tilt-Zoom-Cameras (based on VISCA Camera Protocoll).
 * This is a factory viscaPort for VISCACamera.
 * 
 * API: @see cv.lecturesight.ptz.api.PTZCamera
 */
@Component(name = "cv.lecturesight.ptz.visca", immediate = true)
@Service
public class VISCACameraFactory implements DummyInterface {

  private final static String PROPKEY_SERIALPORTS = "ports";
  private final static String SERVICE_NAME_PREFIX = "visca-";
  private final static String PROPKEY_MODEL_ID = "camera.model.id";
  private final static String PROPKEY_MODEL_NAME = "camera.model.name";
  private final static String PROPKEY_VENDOR_NAME = "camera.vendor.name";
  
  Log log = new Log("VISCA Factory");
  
  @Reference
  Configuration config;
  
  /** OSGI Component context */
  ComponentContext context;
  /** Comm port to viscaPort map (up to 7 services, see visca spec.)*/
  Map<String, List<VISCACamera>> services = new HashMap<String, List<VISCACamera>>();
  /** Service registrations */
  List<ServiceRegistration> registrations = new LinkedList<ServiceRegistration>();
  /** Camera model properties */
  Properties defaultProfile;
  Map<Integer, Properties> cameraProfiles = new HashMap<Integer,Properties>();

  protected void activate(ComponentContext context) {
    this.context = context;
    
    // load device profiles
    Enumeration entryURLs = context.getBundleContext().getBundle().findEntries("profiles", "*.properties", false);
    while(entryURLs.hasMoreElements()) {
      URL url = (URL)entryURLs.nextElement();
      try {
        Properties props = new Properties();
        props.load(url.openStream());
        if (props.containsKey(PROPKEY_MODEL_ID)) {
          String idStr = props.getProperty(PROPKEY_MODEL_ID);
          if (idStr.equals("DEFAULT")) {
            defaultProfile = props;
            log.info("Registered default camera profile");
          } else {
            int id = new Integer(idStr);
            cameraProfiles.put(id, props);
            log.info("Registered camera profile for " + props.getProperty(PROPKEY_VENDOR_NAME) + " " + props.getProperty(PROPKEY_MODEL_NAME));
          }
        } else {
          log.warn("Camera profile " + url.toString() + " does not contain model Id!");
        }
      } catch (IOException e) {
        log.warn("Failed to load device profile from " + url.toString() + " : " + e.getMessage());
      }
    }
    
    // init devices on configured serial ports
    int portNum = 1;
    for (String device : config.getList(PROPKEY_SERIALPORTS)) {
      initPort(SERVICE_NAME_PREFIX + portNum++, device);
    }
    log.info("Activated");
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
        if (!service.camera.deinitialize()) {
          log.warn("Failed to deinitialize " + service.camera.getPort());
        }
      }
    }
    log.info("Deactivated");
  }

  private void initPort(String portName, String device) {
    int cam = 1;
    LibVISCACamera viscaPort = null;
    do {
      log.info("Initializing camera " + cam + " on " + device);
      viscaPort = new LibVISCACamera(portName);
      if (!viscaPort.initialize(device, cam)) {
        log.error("Failed to initialize camera on port " + device);
        break;
      }
      int modelId = viscaPort.getModel();
      log.info("Model ID: " + modelId);
      VISCACamera service;
      String camName = portName + "-" + viscaPort.getCamNo();
      if (cameraProfiles.containsKey(modelId)) {
        service = new VISCACamera(camName, viscaPort, cameraProfiles.get(modelId));
      } else {
        service = new VISCACamera(camName, viscaPort, defaultProfile);
      }
      registerService(portName, service);
    } while (++cam <= viscaPort.getConnectedCams());
  }

  /**
   * Register VISCACamera viscaPort with OSGI framework.
   * 
   * @param portName port name (from configuration)
   * @param viscaPort VISCACamera viscaPort
   */
  private void registerService(String portName, VISCACamera service) {
    // create viscaPort properties
    String camName = portName + "-" + service.camera.getCamNo();
    Dictionary props = new Hashtable();
    props.put("name", camName);
    props.put("type", "visca");
    props.put("port", service.camera.getPort());
    props.put("cam", service.camera.getCamNo());

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

    log.info("Registered camera " + camName + ": " + 
            service.props.getProperty(PROPKEY_VENDOR_NAME) + " " + 
            service.props.getProperty(PROPKEY_MODEL_NAME));
  }
}
