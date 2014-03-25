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
package cv.lecturesight.framesource.impl;

import cv.lecturesight.display.DisplayService;
import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSourceManager;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/** Implementation of Service API
 *
 */
@Component(name="lecturesight.framesource.manager",immediate=true)
@Service
public class FrameSourceManagerImpl implements FrameSourceManager, EventHandler {

  final static String PROPKEY_MRL = "input.mrl";
  final static String PROPKEY_MASK = "input.mask";
  final static String WINDOWNAME_INPUT = "Video Input";
  public static final String FRAMESOURCE_NAME_PROPERTY = "cv.lecturesight.framesource.name";
  public static final String FRAMESOURCE_TYPE_PROPERTY = "cv.lecturesight.framesource.type";
  static final String OSGI_EVENT_REGISTERED = "org/osgi/framework/ServiceEvent/REGISTERED";
  static final String OSGI_EVENT_UNREGISTERED = "org/osgi/framework/ServiceEvent/UNREGISTERING";
  @Reference
  private Configuration config;
  @Reference
  private OpenCLService ocl;
  @Reference
  private DisplayService dsps;
  Log log = new Log("Frame Source Manager");
  private ComponentContext componentContext;
  private Map<String, FrameGrabberFactory> sourceTypes = new HashMap<String, FrameGrabberFactory>();
  private FrameSourceDescriptor providerMRL = null;

  protected void activate(ComponentContext cc) {
    componentContext = cc;

    log.info("Starting....");
    
    // scan for plugins already installed
    try {
      ServiceReference[] refs = cc.getBundleContext().getServiceReferences(FrameGrabberFactory.class.getName(), null);
      if (refs != null) {
        for (int i = 0; i < refs.length; i++) {
          ServiceReference ref = refs[i];
          if (referencesFrameGrabberFactory(ref)) {
            installFrameGrabberFactory(ref);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error during scanning for plugins", e);
    }

    // listen to bundle un-/register events
    String[] topics = new String[]{OSGI_EVENT_REGISTERED, OSGI_EVENT_UNREGISTERED};
    Dictionary<String, Object> props = new Hashtable<String, Object>();
    props.put(EventConstants.EVENT_TOPIC, topics);
    cc.getBundleContext().registerService(EventHandler.class.getName(), this, props);

    try {
      providerMRL = new FrameSourceDescriptor(config.get(PROPKEY_MRL));
    } catch (Exception e) {
      log.warn("Unable to parse default source MRL. FrameSourceProvider will not be availabel!");
    }

    log.info("Activated");
  }

  @Override
  public FrameSource createFrameSource(String input) throws FrameSourceException {
    FrameSource newSource = null;
    try {
      FrameSourceDescriptor fsd = new FrameSourceDescriptor(input);
      if (sourceTypes.containsKey(fsd.getType())) {
        FrameGrabberFactory factory = sourceTypes.get(fsd.getType());
        FrameGrabber grabber = factory.createFrameGrabber(fsd.getLocator(), fsd.getConfiguration());
        FrameUploader uploader = createFrameUploader(grabber);
        
        if (uploader == null) {
          throw new FrameSourceException("Could not create FrameUploader for pixel format " + grabber.getPixelFormat().name());
        } else {
          String maskFile = config.get(PROPKEY_MASK);
          if (!maskFile.isEmpty() && !maskFile.equalsIgnoreCase("none")) {
            try {
              BufferedImage mask = ImageIO.read(new File(maskFile));
              uploader.setMask(mask);
            } catch (IOException e) {
              log.warn("Could not load scene mask: " + maskFile + ": " + e.getMessage());
            }
          }
        }
        newSource = new FrameSourceImpl(fsd.getType(), grabber, uploader);
      } else {
        throw new FrameSourceException("No factory registered for type " + fsd.getType());
      }
    } catch (FrameSourceDescriptorParsingException e) {
      throw new FrameSourceException("Error while parsing frame source descriptor: " + e.getMessage());
    }

    return newSource;
  }
  
  @Override
  public void destroyFrameSource(FrameSource frameSource) throws FrameSourceException {
    FrameSourceImpl fsrc = (FrameSourceImpl)frameSource;
    if (sourceTypes.containsKey(fsrc.getType())) {
      FrameGrabberFactory factory = sourceTypes.get(fsrc.getType());
      factory.destroyFrameGrabber(fsrc.frameGrabber);
      fsrc.uploader.destroy();
      
      // FIXME also destroy host- and GPU-buffer !!!
    
    } else {
      throw new FrameSourceException("No factory registered for type " + fsrc.getType());
    }
  }

  private FrameUploader createFrameUploader(FrameGrabber grabber) {
    FrameUploader uploader = null;
    switch (grabber.getPixelFormat()) {     // TODO replace this implementation with a plugin mechanism!
      case RGB_8BIT:
        uploader = new RGB24FrameUploader(ocl, grabber);
        break;
      case INTENSITY_8BIT:
        uploader = new IntensityFrameUploader(ocl, grabber);
        break;
      default:
        break;
    }
    return uploader;
  }

  @Override
  public void handleEvent(Event event) {
    ServiceReference ref = (ServiceReference) event.getProperty("service");
    if (referencesFrameGrabberFactory(ref)) {
      if (event.getTopic().equals(OSGI_EVENT_REGISTERED)) {
        installFrameGrabberFactory(ref);
      } else if (event.getTopic().equals(OSGI_EVENT_UNREGISTERED)) {
        String types = (String) ref.getProperty(FRAMESOURCE_TYPE_PROPERTY);
        String name = (String) ref.getProperty(FRAMESOURCE_NAME_PROPERTY);
        for (String type: types.split(",")){
          sourceTypes.remove(type.trim());
          log.info("Unregistered " + name);
        }
      }
    }
  }

  private boolean referencesFrameGrabberFactory(ServiceReference ref) {
    try {
      String name = (String) ref.getProperty(FRAMESOURCE_NAME_PROPERTY);
      String type = (String) ref.getProperty(FRAMESOURCE_TYPE_PROPERTY);
      return (name != null) && (type != null);
    } catch (Exception e) {
      return false;
    }
  }

  private void installFrameGrabberFactory(ServiceReference ref) {
    String name = (String) ref.getProperty(FRAMESOURCE_NAME_PROPERTY);
    String types = (String) ref.getProperty(FRAMESOURCE_TYPE_PROPERTY);
    for (String type: types.split(",")){
      FrameGrabberFactory factory = (FrameGrabberFactory) componentContext.getBundleContext().getService(ref);
      sourceTypes.put(type.trim(), factory);
      log.info("Registered FrameGrabberFactory " + name + " (type: " + type.trim() + ")");
      try {
        FrameSourceDescriptor fsd = new FrameSourceDescriptor(config.get(PROPKEY_MRL));
        if (fsd.getType().equals(type.trim())) {
          activateProvider(config.get(PROPKEY_MRL));
        }
      } catch (Exception e) {
        log.warn("Unable to check if newly installed FrameGrabberFactory fits FrameSourceProvider configuration");
      }
    }
  }

  // TODO: deactivate provider if framesource goes down
  private void activateProvider(String mrl) {
    try {
      FrameSource fs = createFrameSource(mrl);
      dsps.registerDisplay(WINDOWNAME_INPUT, fs.getImage(), fs.getSignal());
      FrameSourceProvider pro = new FrameSourceProviderImpl(fs);
      componentContext.getBundleContext().registerService(FrameSourceProvider.class.getName(), pro, null);
    } catch (Exception e) {
      log.error("Failed to activate FrameSourceProvider with source " + mrl, e);
    }
  }
}
