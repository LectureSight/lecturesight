package cv.lecturesight.display.impl;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.display.CustomRenderer;
import cv.lecturesight.display.DisplayRegistration;
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
  private Set<DisplayRegistration> myRegs = new HashSet<DisplayRegistration>();

  public DisplayServiceImpl(OpenCLService ocl) {
    this.ocl = ocl;
  }

  @Override
  public DisplayRegistration registerDisplay(String id, String title, CLImage2D image, OCLSignal trigger) {
    DisplayWindowImpl window = new DisplayWindowImpl(title, image);
    return readyWindow(id, window, trigger);
  }

  @Override
  public DisplayRegistration registerDispaly(String id, String title, CLImage2D image, CustomRenderer renderer, OCLSignal trigger) {
    DisplayWindowImpl window = new DisplayWindowImpl(title, image, renderer);
    return readyWindow(id, window, trigger);
  }

  private DisplayRegistration readyWindow(String id, DisplayWindowImpl window, OCLSignal trigger) {
    String uid = "DisplayWindow-" + UUID.randomUUID().toString();
    OCLSignal sig = ocl.getSignal(uid);
    window.SIG_done = sig;
    window.trigger = trigger;
    window.ocl = ocl;
    DisplayRegistration reg = new DisplayRegistration(id, window);
    DisplayServiceFactory.displays.put(reg.getID(), reg);
    myRegs.add(reg);
    if (DisplayServiceFactory.autoShow.contains(id)) {
      window.show();
    }
    return reg;
  }

  @Override
  public Display getWindowByNumber(int id) {
    if (DisplayServiceFactory.displays.containsKey(id)) {
      return DisplayServiceFactory.displays.get(id).getWindow();
    } else {
      throw new IllegalArgumentException("Unknown display id: " + id);
    }
  }

  @Override
  public Display getWindowById(String id) {
    for (Iterator<DisplayRegistration> it = DisplayServiceFactory.displays.values().iterator(); it.hasNext();) {
      DisplayRegistration reg = it.next();
      if (reg.getSID().equalsIgnoreCase(id)) {
        return reg.getWindow();
      }
    }
    throw new IllegalArgumentException("Unknown display id: " + id);
  }

  @Override
  public Set<DisplayRegistration> getAllDisplays() {
    return myRegs;
  }
}
