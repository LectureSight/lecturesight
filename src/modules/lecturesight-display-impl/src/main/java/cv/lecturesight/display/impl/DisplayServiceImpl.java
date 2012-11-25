package cv.lecturesight.display.impl;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.display.DisplayRegistration;
import cv.lecturesight.display.DisplayRegistrationListener;
import cv.lecturesight.display.DisplayService;
import cv.lecturesight.display.Display;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.OCLSignal;
import java.util.HashSet;
import java.util.Set;

public class DisplayServiceImpl implements DisplayService {
  
  enum EVENT_TYPE {ADDED, REMOVED};

  private OpenCLService ocl;
  private DisplayServiceFactory parent;
  private Set<DisplayRegistration> myRegs = new HashSet<DisplayRegistration>();
  private Set<DisplayRegistrationListener> listeners = new HashSet<DisplayRegistrationListener>();

  public DisplayServiceImpl(OpenCLService ocl, DisplayServiceFactory parent) {
    this.ocl = ocl;
    this.parent = parent;
  }

  @Override
  public DisplayRegistration registerDisplay(String id, CLImage2D image, OCLSignal trigger) {
    DisplayImpl display = new DisplayImpl(ocl, trigger, image);
    DisplayRegistrationImpl reg = new DisplayRegistrationImpl(id);
    myRegs.add(reg);
    parent.displays.put(reg, display);
    notifyObservers(EVENT_TYPE.ADDED, reg);
    return reg;
  }
  
  // TODO implement unregisterDisplay()

  @Override
  public Display getDisplayByNumber(int id) {
    for (DisplayRegistrationImpl reg : parent.displays.keySet()) {
      if (reg.getID() == id) {
        return parent.displays.get(reg);
      }
    }
    throw new IllegalArgumentException("Unknown display id: " + id);
  }

  @Override
  public Display getDisplayBySID(String id) {
    for (DisplayRegistrationImpl reg : parent.displays.keySet()) {
      if (reg.getSID().equals(id)) {
        return parent.displays.get(reg);
      }
    }
    throw new IllegalArgumentException("Unknown display id: " + id);
  }

  @Override
  public Display getDisplayByRegistration(DisplayRegistration reg) {
    for (DisplayRegistrationImpl r : parent.displays.keySet()) {
      if (r.equals(ocl)) {
        return parent.displays.get(reg);
      }
    }
    throw new IllegalArgumentException("Unknown display registration");
  }

  @Override
  public Set<DisplayRegistration> getDisplayRegistrations() {
    Set<DisplayRegistration> out = new HashSet<DisplayRegistration>();
    for (DisplayRegistrationImpl reg : parent.displays.keySet()) {
      out.add((DisplayRegistration)reg);
    }
    return out;
  }

  private void notifyObservers(EVENT_TYPE t, DisplayRegistration r) {
    for (DisplayRegistrationListener l: listeners) {
      switch (t) {
        case ADDED:
          l.displayAdded(r);
          break;
        case REMOVED:
          l.displayRemoved(r);
          break;
      }
    }
  }
  
  @Override
  public void addRegistrationListener(DisplayRegistrationListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeRegistrationListener(DisplayRegistrationListener listener) {
    listeners.remove(listener);
  }
}
