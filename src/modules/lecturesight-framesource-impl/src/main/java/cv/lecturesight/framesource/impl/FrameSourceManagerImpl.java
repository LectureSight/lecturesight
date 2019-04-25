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
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.framesource.FrameSourceManager;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.profile.api.SceneProfile;
import cv.lecturesight.profile.api.SceneProfileEventAdapter;
import cv.lecturesight.profile.api.SceneProfileManager;
import cv.lecturesight.profile.api.Zone;
import cv.lecturesight.util.conf.Configuration;

import com.nativelibs4java.opencl.CLImage2D;

import lombok.Setter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.pmw.tinylog.Logger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of Service API
 *
 */
public class FrameSourceManagerImpl implements FrameSourceManager, EventHandler {

  final static String PROPKEY_MRL = "input.mrl";
  final static String PROPKEY_INVERTED = "inverted";
  final static String PROPKEY_MAXFPS = "maxfps";
  final static String PROPKEY_SNAPSHOT_INTERVAL = "snapshot.interval";
  final static String PROPKEY_SNAPSHOT_FILE = "snapshot.file";

  final static String DISPLAYNAME_INPUT = "cam.overview.input";
  public static final String FRAMESOURCE_NAME_PROPERTY = "cv.lecturesight.framesource.name";
  public static final String FRAMESOURCE_TYPE_PROPERTY = "cv.lecturesight.framesource.type";

  static final String OSGI_EVENT_REGISTERED = "org/osgi/framework/ServiceEvent/REGISTERED";
  static final String OSGI_EVENT_UNREGISTERED = "org/osgi/framework/ServiceEvent/UNREGISTERING";
  @Setter
  private Configuration config;
  @Setter
  private OpenCLService ocl;
  @Setter
  private DisplayService dsps;
  @Setter
  private SceneProfileManager spm;
  private ComponentContext componentContext;
  private Map<String, FrameGrabberFactory> sourceTypes = new HashMap<String, FrameGrabberFactory>();
  private FrameSourceDescriptor providerMRL = null;

  private boolean inverted = false;
  int maxfps = -1;
  int snapshotInterval = 0;
  String snapshotFile;

  protected void activate(ComponentContext cc) {
    componentContext = cc;

    Logger.info("Starting....");

    inverted = config.getBoolean(PROPKEY_INVERTED);
    if (inverted) {
      Logger.info("Framesource is inverted, and will be rotated 180 degrees");
    }

    maxfps = config.getInt(PROPKEY_MAXFPS);
    if (maxfps > 0) {
      Logger.info("Framesource will be limited to " + maxfps + " fps");
    }

    snapshotInterval = config.getInt(PROPKEY_SNAPSHOT_INTERVAL);
    snapshotFile = config.get(PROPKEY_SNAPSHOT_FILE);

    if (!snapshotFile.isEmpty()) {
      Logger.info("Framesource snapshots will be saved to {} every {} seconds", snapshotFile, snapshotInterval);
    }

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
      Logger.error("Error during scanning for plugins", e);
    }

    try {
      providerMRL = new FrameSourceDescriptor(config.get(PROPKEY_MRL));
      Logger.info("Framesource selected: type " + providerMRL.getType() + " at location " + providerMRL.getLocator());
    } catch (Exception e) {
      Logger.warn("Unable to parse default source MRL. FrameSourceProvider will not be available!");
    }

    Logger.info("Activated");
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
          // create and register mask updater from scene profile
          MaskUpdater updater = new MaskUpdater(uploader);
          updater.profileActivated(spm.getActiveProfile());
          spm.registerProfileListener(updater);
        }

        newSource = new FrameSourceImpl(fsd.getType(), grabber, uploader, maxfps, snapshotInterval, snapshotFile);
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
    FrameSourceImpl fsrc = (FrameSourceImpl) frameSource;
    if (sourceTypes.containsKey(fsrc.getType())) {
      FrameGrabberFactory factory = sourceTypes.get(fsrc.getType());
      factory.destroyFrameGrabber(fsrc.frameGrabber);    // de-init the stuff that gets the frames (native libs etc.)
      fsrc.uploader.destroy();                           // free GPU buffers created by uploader
    } else {
      throw new FrameSourceException("No factory registered for type " + fsrc.getType());
    }
  }

  private FrameUploader createFrameUploader(FrameGrabber grabber) {
    FrameUploader uploader = null;
    switch (grabber.getPixelFormat()) {     // TODO replace this implementation with a plugin mechanism!
      case RGB_8BIT:
        uploader = new RGB24FrameUploader(ocl, grabber, inverted);
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
        for (String type : types.split(",")) {
          sourceTypes.remove(type.trim());
          Logger.info("Unregistered " + name);
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
    for (String type : types.split(",")) {
      FrameGrabberFactory factory = (FrameGrabberFactory) componentContext.getBundleContext().getService(ref);
      sourceTypes.put(type.trim(), factory);
      Logger.info("Registered FrameGrabberFactory " + name + " (type: " + type.trim() + ")");
      try {
        FrameSourceDescriptor fsd = new FrameSourceDescriptor(config.get(PROPKEY_MRL));
        if (fsd.getType().equals(type.trim())) {
          activateProvider(config.get(PROPKEY_MRL));
        }
      } catch (Exception e) {
        Logger.warn("Unable to check if newly installed FrameGrabberFactory fits FrameSourceProvider configuration");
      }
    }
  }

  // TODO: deactivate provider if framesource goes down
  private void activateProvider(String mrl) {
    try {
      FrameSource fs = createFrameSource(mrl);
      dsps.registerDisplay(DISPLAYNAME_INPUT, fs.getImage(), fs.getSignal());
      FrameSourceProvider pro = new FrameSourceProviderImpl(fs);
      componentContext.getBundleContext().registerService(FrameSourceProvider.class.getName(), pro, null);
    } catch (Exception e) {
      Logger.error(e, "Failed to activate FrameSourceProvider with source " + mrl);
    }
  }

  @Override
  public String getOverviewSnapshotFile() {
    return snapshotFile;
  }

  /**
   * Class responsible for catching update events on the scene profile and
   * updating the ignore mask for the framesource accordingly.
   *
   */
  class MaskUpdater extends SceneProfileEventAdapter {

    FrameUploader client;
    BufferedImage mask;

    MaskUpdater(FrameUploader uploader) {
      client = uploader;
      CLImage2D outimg = uploader.getOutputImage();
      mask = new BufferedImage((int)outimg.getWidth(), (int)outimg.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void profileActivated(SceneProfile profile) {
      Graphics2D g = mask.createGraphics();
      g.setColor(Color.WHITE);
      g.fill3DRect(0, 0, mask.getWidth(), mask.getHeight(), true);

      g.setColor(Color.BLACK);
      for (Zone zone : profile.getIgnoreZones()) {
        g.fillRect(zone.x, zone.y, zone.width, zone.height);
      }

      client.setMask(mask);
    }
  }
}
