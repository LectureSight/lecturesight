/* Copyright (C) 2012 Benjamin Wulff
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package cv.lecturesight.decorator.manager;

import cv.lecturesight.decorator.api.DecoratorManager;
import cv.lecturesight.decorator.api.ObjectDecorator;
import cv.lecturesight.objecttracker.TrackerObject;
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
import org.pmw.tinylog.Logger;

public class DecoratorManagerImpl implements DecoratorManager,EventHandler {

  static final String OSGI_EVENT_REGISTERED = "org/osgi/framework/ServiceEvent/REGISTERED";
  static final String OSGI_EVENT_UNREGISTERED = "org/osgi/framework/ServiceEvent/UNREGISTERING";
  static final String SERVICE_PROPKEY_NAME = "lecturesight.decorator.name";
  static final String SERVICE_PROPKEY_CALLON = "lecturesight.decorator.callon";
  static final String SERVICE_PROPKEY_PRODUCES = "lecturesight.decorator.produces";
  
  private ComponentContext cc;
  private Map<CallType, List<ObjectDecorator>> decorators = new EnumMap<CallType, List<ObjectDecorator>>(CallType.class);
  {
    for (CallType t : CallType.values()) {
      decorators.put(t, new LinkedList<ObjectDecorator>());
    }
  }
  
  protected void activate(ComponentContext cc) {
    this.cc = cc;
    
    // scan for already installed Decorators
    try {
      ServiceReference[] refs = cc.getBundleContext().getServiceReferences(ObjectDecorator.class.getName(), null);
      if (refs != null) {
        for (int i = 0; i < refs.length; i++) {
          ServiceReference ref = refs[i];
          if (referencesDecorator(ref)) {
            installDecorator(ref);
          }
        }
      }
    } catch (Exception e) {
      Logger.error("Error during scanning for plugins", e);
    }
    
    // listen to bundle un-/register events
    String[] topics = new String[]{OSGI_EVENT_REGISTERED, OSGI_EVENT_UNREGISTERED};
    Dictionary<String, Object> props = new Hashtable<String, Object>();
    props.put(EventConstants.EVENT_TOPIC, topics);
    cc.getBundleContext().registerService(EventHandler.class.getName(), this, props);
    Logger.info("Listening for Decorators");
  }
  
  @Override
  public void applyDecorators(CallType type, TrackerObject obj) {
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
      Logger.info("Decorator installed: " + name + " (calltype=" + typeS + ")");
    } catch (Exception e) {
      Logger.error("Error while installing Decorator.", e);
    }
  }

  private void removeDecorator(ServiceReference ref) {
    try {
      String typeS = (String) ref.getProperty(SERVICE_PROPKEY_CALLON);
      CallType type = CallType.valueOf(typeS);
      ObjectDecorator decorator = (ObjectDecorator) cc.getBundleContext().getService(ref);
      decorators.get(type).remove(decorator);
    } catch (Exception e) {
      Logger.error("Error while uninstalling Decorator.", e);
    }
  }
}
