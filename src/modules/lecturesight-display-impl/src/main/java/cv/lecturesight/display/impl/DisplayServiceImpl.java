package cv.lecturesight.display.impl;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.display.CustomRenderer;
import cv.lecturesight.display.DisplayService;
import cv.lecturesight.display.Display;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.OCLSignal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class DisplayServiceImpl implements DisplayService {

  private OpenCLService ocl;
  private Set<DisplayRegistrationImpl> myRegs = new HashSet<DisplayRegistrationImpl>();

  public DisplayServiceImpl(OpenCLService ocl) {
    this.ocl = ocl;
  }

  @Override
  public DisplayRegistrationImpl registerDisplay(String id, CLImage2D image, OCLSignal trigger) {
    DisplayImpl window = new DisplayImpl(title, image);
    return readyWindow(id, window, trigger);
  }

  @Override
  public DisplayRegistrationImpl registerDispaly(String id, String title, CLImage2D image, CustomRenderer renderer, OCLSignal trigger) {
    DisplayImpl window = new DisplayImpl(title, image, renderer);
    return readyWindow(id, window, trigger);
  }

  private DisplayRegistrationImpl readyWindow(String id, DisplayImpl window, OCLSignal trigger) {
    String uid = "DisplayWindow-" + UUID.randomUUID().toString();
    OCLSignal sig = ocl.getSignal(uid);
    window.SIG_done = sig;
    window.trigger = trigger;
    window.ocl = ocl;
    DisplayRegistrationImpl reg = new DisplayRegistrationImpl(id, window);
    DisplayServiceFactory.displays.put(reg.getID(), reg);
    myRegs.add(reg);
    if (DisplayServiceFactory.autoShow.contains(id)) {
      window.show();
    }
    return reg;
  }

  @Override
  public Display getDisplayByNumber(int id) {
    if (DisplayServiceFactory.displays.containsKey(id)) {
      return DisplayServiceFactory.displays.get(id).getDisplay();
    } else {
      throw new IllegalArgumentException("Unknown display id: " + id);
    }
  }

  @Override
  public Display getDisplayBySID(String id) {
    for (Iterator<DisplayRegistrationImpl> it = DisplayServiceFactory.displays.values().iterator(); it.hasNext();) {
      DisplayRegistrationImpl reg = it.next();
      if (reg.getSID().equalsIgnoreCase(id)) {
        return reg.getDisplay();
      }
    }
    throw new IllegalArgumentException("Unknown display id: " + id);
  }

  @Override
  public Set<DisplayRegistrationImpl> getDisplayRegistrations() {
    return myRegs;
  }
}
