package cv.lecturesight.framesource.kinect;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSourceException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.openkinect.freenect.Context;
import org.openkinect.freenect.DepthFormat;
import org.openkinect.freenect.DepthHandler;
import org.openkinect.freenect.Device;
import org.openkinect.freenect.Freenect;
import org.openkinect.freenect.LogHandler;
import org.openkinect.freenect.LogLevel;
import org.openkinect.freenect.Resolution;
import org.openkinect.freenect.VideoFormat;
import org.openkinect.freenect.VideoHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pmw.tinylog.Logger;

public class Activator implements BundleActivator, LogHandler {

  final Log log = new Log("Kinect FrameSource");
  Log freenectLog;
  Context context;
  final static List<DeviceConsumption> consumers = new ArrayList<DeviceConsumption>();
  FrameGrabberFactory factoryRGB;
  FrameGrabberFactory factoryDepth;

  @Override
  public void start(BundleContext bc) throws Exception {

    // init Freenect context
    try {
      context = Freenect.createContext();
    } catch (IllegalStateException e) {
      String msg = "Failed to initialize Freenect context! ";
      Logger.error(msg, e);
      throw new FrameSourceException(msg, e);
    }

    // init logging pass-thru for Freenect if configured
    String propval = System.getProperty(Constants.PROPKEY_FREENECT_LOG_ACTIVE);
    if (propval != null) {
      propval = propval.trim().toLowerCase();
      if (propval.equals("true") || propval.equals("on")) {
        freenectLog = new Log("Freenect");
        context.setLogHandler(this);

        // apply log level if configured
        propval = System.getProperty(Constants.PROPKEY_FREENECT_LOG_LEVEL);
        if (propval != null) {
          propval = propval.trim().toUpperCase();
          try {
            LogLevel ll = LogLevel.valueOf(propval);
            setFreenectLogLevel(ll);
          } catch (Exception e) {
            Logger.warn("Failed to parse Freenect log level. " + e.getMessage());
            setFreenectLogLevel(LogLevel.WARNING);
          }
        } else {
          setFreenectLogLevel(LogLevel.WARNING);
        }
      }
    }

    // search for Kinect devices
    int numd = context.numDevices();
    if (numd > 0) {
      for (int i = 0; i < numd; i++) {
        consumers.add(i, new DeviceConsumption());
      }
    } else {
      throw new FrameSourceException("No Kinect device found! ");
    }

    // set up frame grabber factories
    // RGB
    factoryRGB = new KinectVisualFrameGrabberFactory(this);
    Hashtable props = new Hashtable();
    props.put("cv.lecturesight.framesource.name", "Kinect RGB sensor");
    props.put("cv.lecturesight.framesource.type", "kinect-visual");
    bc.registerService(FrameGrabberFactory.class.getName(), factoryRGB, props);

    // depth
    FrameGrabberFactory depth = new KinectDepthFrameGrabberFactory(this);
    props = new Hashtable();
    props.put("cv.lecturesight.framesource.name", "Kinect Depth Sensor");
    props.put("cv.lecturesight.framesource.type", "kinect-depth");
    bc.registerService(FrameGrabberFactory.class.getName(), depth, props);

    Logger.info("Activated. Found " + context.numDevices() + " Kinect device(s).");
  }

  private void setFreenectLogLevel(LogLevel ll) {
    Logger.info("Setting Freenect log level to: " + ll.name());
    context.setLogLevel(ll);
  }

  @Override
  public void stop(BundleContext bc) throws Exception {
    if (context != null) {
      for (DeviceConsumption dc : consumers) {
        if (dc.consumerDepth != null) {
          factoryDepth.destroyFrameGrabber(dc.consumerDepth);
        }
        if (dc.consumerRGB != null) {
          factoryRGB.destroyFrameGrabber(dc.consumerRGB);
        }
        if (dc.device != null) {
          try {
            dc.device.close();
          } catch (Exception e) {
            Logger.warn(e.getClass().getSimpleName() + " while closing device. " + e.getMessage());
          }
        }
      }
    }
    Logger.info("Deactivated");
  }

  public boolean hasConsumerRGB(int device) {
    try {
      return consumers.get(device).consumerRGB != null;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean hasConsumerDepth(int device) {
    try {
      return consumers.get(device).consumerDepth != null;
    } catch (Exception e) {
      return false;
    }
  }

  public FrameGrabber getConsumerRGB(int device) {
    try {
      return consumers.get(device).consumerRGB;
    } catch (Exception e) {
      return null;
    }
  }

  public FrameGrabber getConsumerDepth(int device) {
    try {
      return consumers.get(device).consumerDepth;
    } catch (Exception e) {
      return null;
    }
  }

  public void setConsumerRGB(int device, FrameGrabber consumer) throws FrameSourceException {
    try {
      DeviceConsumption dc = consumers.get(device);
      if (consumer != null) {
        if (dc.device == null) {
          dc.device = openDevice(device);
        }
        dc.consumerRGB = consumer;
        dc.device.startVideo((VideoHandler)consumer);
      } else {
        dc.consumerRGB = null;
        dc.device.stopVideo();
        if (dc.consumerDepth == null && dc.consumerRGB == null) {
          closeDevice(device);
        }
      }
      dc.device.startVideo((KinectVisualFrameGrabber)consumer);
      dc.consumerRGB = consumer;
    } catch (Exception e) {
      Logger.error("Failed to initialize RGB consumer on device " + device, e);
    }
  }

  public void setConsumerDepth(int device, FrameGrabber consumer) {
    try {
      DeviceConsumption dc = consumers.get(device);
      if (consumer != null) {
        if (dc.device == null) {
          dc.device = openDevice(device);
        }
        dc.consumerDepth = consumer;
        dc.device.startDepth((DepthHandler)consumer);
      } else {
        dc.consumerRGB = null;
        dc.device.stopDepth();
        if (dc.consumerDepth == null && dc.consumerRGB == null) {
          closeDevice(device);
        }
      }
      dc.device.startVideo((KinectVisualFrameGrabber)consumer);
      dc.consumerRGB = consumer;
    } catch (Exception e) {
      Logger.error("Failed to initialize depth consumer on device " + device, e);
    }
  }

  private Device openDevice(int index) throws FrameSourceException {
    try {
      Device dev = context.openDevice(index);

      // TODO set video and depth format!
      dev.setDepthFormat(DepthFormat.MM, Resolution.LOW);
      dev.setVideoFormat(VideoFormat.RGB, Resolution.LOW);

      Logger.info("Opended Kinect #" + index);
      return dev;
    } catch (IllegalStateException e) {
      throw new FrameSourceException("Failed to open device. ", e);
    }
  }

  private void closeDevice(int index) {
    DeviceConsumption dc = consumers.get(index);
    if (dc.consumerDepth == null && dc.consumerRGB == null) {
      dc.device.close();
    } else {
      throw new IllegalStateException("Device has still consumers attached to it!");
    }
  }

  @Override
  public void onMessage(Device device, LogLevel ll, String msg) {
    switch (ll) {
      case FATAL:
      case ERROR:
        Logger.error(formatLogMessage(device, msg));
        break;
      case WARNING:
      case NOTICE:
        Logger.warn(formatLogMessage(device, msg));
        break;
      case INFO:
        Logger.info(formatLogMessage(device, msg));
        break;
      case DEBUG:
      case SPEW:
      case FLOOD:
        Logger.debug(formatLogMessage(device, msg));
        break;
    }
  }

  private String formatLogMessage(Device dev, String msg) {
    return "#" + dev.getDeviceIndex() + " " + msg;
  }

  private class DeviceConsumption {
    Device device = null;
    FrameGrabber consumerRGB = null;
    FrameGrabber consumerDepth = null;
  }
}
