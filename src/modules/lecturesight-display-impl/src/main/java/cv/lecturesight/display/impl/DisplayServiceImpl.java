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
package cv.lecturesight.display.impl;

import cv.lecturesight.display.Display;
import cv.lecturesight.display.DisplayRegistration;
import cv.lecturesight.display.DisplayRegistrationListener;
import cv.lecturesight.display.DisplayService;
import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.OCLSignal;

import com.nativelibs4java.opencl.CLImage2D;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class DisplayServiceImpl implements DisplayService {

  enum EventType {ADDED, REMOVED};

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
    notifyObservers(EventType.ADDED, reg);

    // register UI for new display
    DisplayUI ui = new DisplayUI(display, id);
    Dictionary<String, Object> props = new Hashtable<String, Object>();
    parent.bundleContext.registerService(UserInterface.class.getName(), ui, props);

    return reg;
  }

  // TODO implement unregisterDisplay()

  @Override
  public Display getDisplayByNumber(int id) {
    for (DisplayRegistration reg : parent.displays.keySet()) {
      if (reg.getID() == id) {
        return parent.displays.get(reg);
      }
    }
    throw new IllegalArgumentException("Unknown display id: " + id);
  }

  @Override
  public Display getDisplayBySID(String id) {
    for (DisplayRegistration reg : parent.displays.keySet()) {
      if (reg.getSID().equals(id)) {
        return parent.displays.get(reg);
      }
    }
    throw new IllegalArgumentException("Unknown display id: " + id);
  }

  @Override
  public Display getDisplayByRegistration(DisplayRegistration reg) {
    if (parent.displays.containsKey(reg)) {
      return parent.displays.get(reg);
    } else {
      throw new IllegalArgumentException("Unknown display registration");
    }
  }

  @Override
  public Set<DisplayRegistration> getDisplayRegistrations() {
    Set<DisplayRegistration> out = new HashSet<DisplayRegistration>();
    for (DisplayRegistration reg : parent.displays.keySet()) {
      out.add((DisplayRegistration)reg);
    }
    return out;
  }

  private void notifyObservers(EventType t, DisplayRegistration r) {
    for (DisplayRegistrationListener l: listeners) {
      switch (t) {
        case ADDED:
          l.displayAdded(r);
          break;
        case REMOVED:
          l.displayRemoved(r);
          break;
        default:
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

