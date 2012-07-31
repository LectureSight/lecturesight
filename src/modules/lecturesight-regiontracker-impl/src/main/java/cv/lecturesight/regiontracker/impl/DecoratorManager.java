package cv.lecturesight.regiontracker.impl;

import cv.lecturesight.regiontracker.ObjectDecorator;
import cv.lecturesight.regiontracker.Region;
import cv.lecturesight.util.Log;
import java.util.Dictionary;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class DecoratorManager implements EventHandler {

  static final String OSGI_EVENT_REGISTERED = "org/osgi/framework/ServiceEvent/REGISTERED";
  static final String OSGI_EVENT_UNREGISTERED = "org/osgi/framework/ServiceEvent/UNREGISTERING";
  static final String SERVICE_PROPKEY_NAME = "lecturesight.decorator.name";
  static final String SERVICE_PROPKEY_CALLON = "lecturesight.decorator.callon";
  static final String SERVICE_PROPKEY_PRODUCES = "lecturesight.decorator.produces";

  public enum CallType {
    EACHFRAME, ONAPPEAR
  }
  
  private Log log = new Log("Object Decorator Manager");
  private ComponentContext cc;
  private Map<CallType, List<ObjectDecorator>> decorators = new EnumMap<CallType, List<ObjectDecorator>>(CallType.class);
  {
    for (CallType t : CallType.values()) {
      decorators.put(t, new LinkedList<ObjectDecorator>());
    }
  }

  public DecoratorManager(ComponentContext cc) {
    this.cc = cc;
    // listen to bundle un-/register events
    String[] topics = new String[]{OSGI_EVENT_REGISTERED, OSGI_EVENT_UNREGISTERED};
    Dictionary<String, Object> props = new Hashtable<String, Object>();
    props.put(EventConstants.EVENT_TOPIC, topics);
    cc.getBundleContext().registerService(EventHandler.class.getName(), this, props);
    log.info("listening for ObjectDecorators");
  }
  
  public void applyDecorators(CallType type, Region obj) {
    for (Iterator<ObjectDecorator> it = decorators.get(type).iterator(); it.hasNext(); ) {
      it.next().examine(obj);
    }
  }

  @Override
  public void handleEvent(Event event) {
    ServiceReference ref = (ServiceReference) event.getProperty("service");
    if (referencesDecorator(ref)) {
      if (event.getTopic().equals(OSGI_EVENT_REGISTERED)) {
        installDecorator(ref);
      } else if (event.getTopic().equals(OSGI_EVENT_UNREGISTERED)) {
        removeDecorator(ref);
      }
    }
  }

  private boolean referencesDecorator(ServiceReference ref) {
    try {
      String name = (String) ref.getProperty(SERVICE_PROPKEY_NAME);
      String callType = (String) ref.getProperty(SERVICE_PROPKEY_CALLON);
      return (name != null) && (callType != null);
    } catch (Exception e) {
      return false;
    }
  }

  private void installDecorator(ServiceReference ref) {
    try {
      String name = (String) ref.getProperty(SERVICE_PROPKEY_NAME);
      String typeS = (String) ref.getProperty(SERVICE_PROPKEY_CALLON);
      CallType type = CallType.valueOf(typeS);
      ObjectDecorator decorator = (ObjectDecorator) cc.getBundleContext().getService(ref);
      decorators.get(type).add(decorator);
      log.info("Installed Decorator " + name + " of type " + typeS);
    } catch (Exception e) {
      log.error("Error while installing ObejctDecorator.", e);
    }
  }

  private void removeDecorator(ServiceReference ref) {
    try {
      String typeS = (String) ref.getProperty(SERVICE_PROPKEY_CALLON);
      CallType type = CallType.valueOf(typeS);
      ObjectDecorator decorator = (ObjectDecorator) cc.getBundleContext().getService(ref);
      decorators.get(type).remove(decorator);
    } catch (Exception e) {
      log.error("Error while uninstalling ObejctDecorator.", e);
    }
  }
}
