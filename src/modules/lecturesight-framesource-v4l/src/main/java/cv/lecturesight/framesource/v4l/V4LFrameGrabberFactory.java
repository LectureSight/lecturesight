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
package cv.lecturesight.framesource.v4l;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.util.conf.Configuration;

import au.edu.jcu.v4l4j.Control;
import au.edu.jcu.v4l4j.ControlList;
import au.edu.jcu.v4l4j.DeviceInfo;
import au.edu.jcu.v4l4j.ImageFormat;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.exceptions.ControlException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.util.Map;
import java.util.Vector;

/** Implementation of Service API
 *
 */
public class V4LFrameGrabberFactory implements FrameGrabberFactory {

  @Setter
  private Configuration config;

  protected void activate(ComponentContext cc) {
    System.loadLibrary("libvideo.so.0");
  }

  @Override
  public FrameGrabber createFrameGrabber(String input, Map<String, String> conf) throws FrameSourceException {
    VideoDevice device = initVideoDevice(input);
    Logger.info(generateDeviceInfo(device));
    ControlList controlList = device.getControlList();
    Vector<Control> controls = (Vector <Control>) controlList.getList();

    Logger.info("provided Controls:");

    for (Control control : controls){
      String values = "";
      String val="";
      switch (control.getType()) {
        case V4L4JConstants.CTRL_TYPE_SWITCH: {
          try {
            val = " = " + control.getValue();
            values = " Values: [0 | 1] [ \"false\" | \"true\" ]";
          } catch (ControlException ex) {
            Logger.error("control.getValue() failed",ex);
          }
          break;
        }
        case V4L4JConstants.CTRL_TYPE_SLIDER: {
          try {
            val = " = " + control.getValue();
            int min = control.getMinValue();
            int max = control.getMaxValue();
            int incr = control.getStepValue();
            values = " Values: [ " + min + " .. " + max +" ] increment: " + incr;
          } catch (ControlException ex) {
            Logger.error("control.getValue() failed",ex);
          }
          break;
        }
        case V4L4JConstants.CTRL_TYPE_BUTTON: {
          val = " = 0";
          values = " [any Value will trigger]";

          break;
        }
        case V4L4JConstants.CTRL_TYPE_DISCRETE: {
          try {
            int ival = control.getValue();
            Map<String,Integer> discreteValuesMap = control.getDiscreteValuesMap();
            for (String discVal : control.getDiscreteValueNames()){
              if (discreteValuesMap.get(discVal) == ival){
                val = " = " + discVal + " (" + ival + ")";
              }
            }

            values = " Values: " + control.getDiscreteValuesMap();
          } catch (ControlException ex) {
            Logger.error("control.getStringValue() failed",ex);
          }
          break;
        }
        case V4L4JConstants.CTRL_TYPE_STRING : {
          try {

            val = " = " + control.getStringValue();
            values = " Values: " + control.getDiscreteValueNames();
          } catch (ControlException ex) {
            Logger.error("control.getValue() failed",ex);
          }
          break;
        }
        case V4L4JConstants.CTRL_TYPE_LONG :{
          try {
            val = " = " + control.getLongValue();
          } catch (ControlException ex) {
            Logger.error("control.getLongValue() failed",ex);
          }
          break;

        }
        case V4L4JConstants.CTRL_TYPE_BITMASK :{
          try {
            val = " = " + control.getValue();
          } catch (ControlException ex) {
            Logger.error("control.getValue() failed",ex);
          }
          break;
        }
        default:
          break;
      }

      Logger.info("Name: " + control.getName() + val + " Type: " + V4LFrameGrabberConstants.CONTROL_TYPE_NAMES.get(control.getType()) + values);
    }

    // HashMap<String,String> params = new HashMap<String, String>();

    int width = conf.containsKey("width") ? Integer.parseInt(conf.get("width")) : config.getInt(V4LFrameGrabberConstants.PROPKEY_FRAME_WIDTH);
    int height = conf.containsKey("height") ? Integer.parseInt(conf.get("height")) : config.getInt(V4LFrameGrabberConstants.PROPKEY_FRAME_HEIGHT);
    int videoStandard = conf.containsKey("standard") ? Integer.parseInt(conf.get("standard")) : config.getInt(V4LFrameGrabberConstants.PROPKEY_STANDARD);
    int videoChannel = conf.containsKey("channel") ? Integer.parseInt(conf.get("channel")) : config.getInt(V4LFrameGrabberConstants.PROPKEY_CHANNEL);
    int videoQuality = conf.containsKey("quality") ? Integer.parseInt(conf.get("quality")) : config.getInt(V4LFrameGrabberConstants.PROPKEY_QUALITY);
    String videoFormat = conf.containsKey("format") ? conf.get("format") : config.get(V4LFrameGrabberConstants.PROPKEY_FORMAT);

    for (String confItemKey : conf.keySet()) {
      String confItem = confItemKey.trim();
      if (V4LFrameGrabberConstants.PROPKEYS.contains(confItem)){
        continue;
      }
      Control cont = controlList.getControl(confItem);
      if (cont == null) {
        Logger.error("Ignoring Config entry " + confItem + " = " + conf.get(confItem) + " - " + confItem + " Control does not exist for device.");
      } else {
        String setVal="";
        switch (cont.getType()) {
          case V4L4JConstants.CTRL_TYPE_SWITCH :{
            String val = conf.get(confItem);
            try {
              if (("true".equalsIgnoreCase(val.trim()) || "1".equals(val.trim()))){
                cont.setValue(1);
              } else {
                cont.setValue(0);
              }

            } catch (ControlException ex) {
              Logger.error("control.setValue() failed", ex);
            }
            try {
              setVal = " = " + cont.getValue();
            } catch (ControlException ex) {
              // do nothing
            }
            break;
          }
          case V4L4JConstants.CTRL_TYPE_SLIDER : {
            int val = Integer.parseInt(conf.get(confItem));
            int min = cont.getMinValue();
            int max = cont.getMaxValue();
            int incr = cont.getStepValue();
            if ((val >= min) && (val <= max)) {
              try {
                cont.setValue(val);
              } catch (ControlException ex) {
                Logger.error("control.setValue() failed", ex);
              }
            }
            try {
              setVal = " = " + cont.getValue();
            } catch (ControlException ex) {
              // do nothing
            }
            break;
          }
          case V4L4JConstants.CTRL_TYPE_BUTTON : {
            String val = conf.get(confItem);
            try {
              cont.setValue(1);
            } catch (ControlException ex) {
              Logger.error("control.setValue() failed", ex);
            }
            setVal = " = active";
            break;
          }
          case V4L4JConstants.CTRL_TYPE_DISCRETE: {
            String val = conf.get(confItem);
            if (cont.getDiscreteValueNames().contains(val)){
              try {
                cont.setValue(cont.getDiscreteValuesMap().get(val));
              } catch (ControlException ex) {
                Logger.error("control.setStringValue() failed", ex);
              }
              setVal = " = " + val + "("+ cont.getDiscreteValuesMap().get(val) +")";
            }
            break;
          }
          case V4L4JConstants.CTRL_TYPE_STRING : {
            String val = conf.get(confItem);
            try {
              cont.setStringValue(val);
            } catch (ControlException ex) {
              Logger.error("control.setStringValue() failed", ex);
            }
            try {
              setVal = " = " + cont.getStringValue();
            } catch (ControlException ex) {
              // do nothing
            }
            break;
          }
          case V4L4JConstants.CTRL_TYPE_LONG :{
            long val = Long.getLong(conf.get(confItem));
            try {
              cont.setLongValue(val);
            } catch (ControlException ex) {
              Logger.error("control.setLongValue() failed", ex);
            }
            try {
              setVal = " = " + cont.getLongValue();
            } catch (ControlException ex) {
              // do nothing
            }
            break;
          }
          case V4L4JConstants.CTRL_TYPE_BITMASK :{
            int val = Integer.getInteger(conf.get(confItem));
            try {
              cont.setValue(val);
            } catch (ControlException ex) {
              Logger.error("control.setValue() failed", ex);
            }
            try {
              setVal = " = " + cont.getValue();
            } catch (ControlException ex) {
              // do nothing
            }
            break;
          }
          default:
            break;
        }
        Logger.info("Setting Control : {} to {}", cont.getName(), setVal);

      }
    }
    device.releaseControlList();
    return new V4LFrameGrabber(device, width, height, videoStandard, videoChannel, videoQuality, videoFormat);
  }

  private VideoDevice initVideoDevice(String name) throws FrameSourceException {
    try {
      Logger.info("Opening capture device " + name);
      VideoDevice device = new VideoDevice(name);
      Logger.info("Device name: " + device.getDeviceInfo().getName());
      if (device == null) {
        throw new FrameSourceException("Could not open capture device: " + name);
      }
      return device;
    } catch (V4L4JException ex) {
      throw new FrameSourceException("Could not open capture device " + name + ": " + ex.getMessage());
    }
  }

  private String generateDeviceInfo(VideoDevice device) throws FrameSourceException {
    try {
      StringBuilder out = new StringBuilder();
      out.append("Device supports formats: ");
      DeviceInfo info = device.getDeviceInfo();
      for (ImageFormat f : info.getFormatList().getNativeFormats()) {
        out.append(f.toNiceString());
        out.append(" ");
      }
      return out.toString();
    } catch (V4L4JException ex) {
      throw new FrameSourceException("Could not get device information: " + ex.getMessage());
    }
  }

  @Override
  public void destroyFrameGrabber(FrameGrabber fg) throws FrameSourceException {
    try {
      ((V4LFrameGrabber)fg).shutdown();
    } catch (Exception e) {
      throw new FrameSourceException("Failed to shut down V4LFrameSource. ", e);
    }
  }
}

