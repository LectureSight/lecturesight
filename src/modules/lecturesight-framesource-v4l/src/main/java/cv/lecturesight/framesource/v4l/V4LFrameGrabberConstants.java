/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cv.lecturesight.framesource.v4l;

import au.edu.jcu.v4l4j.V4L4JConstants;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author ts23
 */
public class V4LFrameGrabberConstants {
  public final static String PROPKEY_FRAME_WIDTH = "resolution.width";
  public final static String PROPKEY_FRAME_HEIGHT = "resolution.height";
  public final static String PROPKEY_WIDTH = "width";
  public final static String PROPKEY_HEIGHT = "height";
  public final static String PROPKEY_STANDARD = "standard";
  public final static String PROPKEY_CHANNEL = "channel";
  public final static String PROPKEY_QUALITY = "quality";
  public final static String PROPKEY_FORMAT = "format";
  
  public final static Vector<String> PROPKEYS = new Vector<String>();
  
  public final static HashMap<Integer, String> CONTROL_TYPE_NAMES = new HashMap<Integer, String>();
  
  static {
      PROPKEYS.add(V4LFrameGrabberConstants.PROPKEY_FRAME_WIDTH);
      PROPKEYS.add(V4LFrameGrabberConstants.PROPKEY_FRAME_HEIGHT);
      PROPKEYS.add(V4LFrameGrabberConstants.PROPKEY_WIDTH);
      PROPKEYS.add(V4LFrameGrabberConstants.PROPKEY_HEIGHT);
      PROPKEYS.add(V4LFrameGrabberConstants.PROPKEY_STANDARD);
      PROPKEYS.add(V4LFrameGrabberConstants.PROPKEY_CHANNEL);
      PROPKEYS.add(V4LFrameGrabberConstants.PROPKEY_QUALITY);
      PROPKEYS.add(V4LFrameGrabberConstants.PROPKEY_FORMAT);
      CONTROL_TYPE_NAMES.put(V4L4JConstants.CTRL_TYPE_BUTTON, "CTRL_TYPE_BUTTON"); 
      CONTROL_TYPE_NAMES.put(V4L4JConstants.CTRL_TYPE_SLIDER, "CTRL_TYPE_SLIDER"); 
      CONTROL_TYPE_NAMES.put(V4L4JConstants.CTRL_TYPE_SWITCH, "CTRL_TYPE_SWITCH"); 
      CONTROL_TYPE_NAMES.put(V4L4JConstants.CTRL_TYPE_DISCRETE, "CTRL_TYPE_DISCRETE"); 
      CONTROL_TYPE_NAMES.put(V4L4JConstants.CTRL_TYPE_STRING, "CTRL_TYPE_STRING"); 
      CONTROL_TYPE_NAMES.put(V4L4JConstants.CTRL_TYPE_LONG, "CTRL_TYPE_LONG"); 
      CONTROL_TYPE_NAMES.put(V4L4JConstants.CTRL_TYPE_BITMASK,"CTRL_TYPE_BITMASK");
  };
  
}
