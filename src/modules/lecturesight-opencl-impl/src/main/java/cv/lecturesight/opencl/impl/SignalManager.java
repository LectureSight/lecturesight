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
package cv.lecturesight.opencl.impl;

import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.Triggerable;
import cv.lecturesight.opencl.impl.trigger.OCLSignalImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SignalManager {

  static final int MAX_SIGNALS = 100;
  // FIXME the two depend on each other, implementation is not thread safe!
  private List<String> signals = new ArrayList<String>();
  private List<List<Triggerable>> recievers = new ArrayList<List<Triggerable>>(MAX_SIGNALS);
  private final Object listLock = new Object();

  public boolean signalExists(String name) {
    synchronized (listLock) {
      return signals.contains(name);
    }
  }

  public OCLSignal createSignal(String name) {
    OCLSignalImpl out = null;
    synchronized (listLock) {
      if (signals.size() < MAX_SIGNALS) {
        signals.add(name);
        int index = signals.indexOf(name);
        recievers.add(new LinkedList<Triggerable>());
        out = new OCLSignalImpl(index, name);
      } else {
        throw new RuntimeException("Maximum number of signals reached!");
      }
    }
    return out;
  }

  public OCLSignal getSignal(String name) {
    OCLSignalImpl out = null;
    if (signalExists(name)) {
      int index = signals.indexOf(name);
      out = new OCLSignalImpl(index, name);
    } else {
      throw new RuntimeException("Unknown Signal " + name);   // FIXME better NoSuchElementException
    }
    return out;
  }

  public void registerWithSignal(OCLSignal signal, Triggerable task) {
    synchronized (listLock) {
      int index = lookupSignalValidate(signal);
      List<Triggerable> recs = recievers.get(index);
      if (!recs.contains(task)) {
        recs.add(task);
      }
    }
  }

  public void unregisterFromSignal(OCLSignal signal, Triggerable task) {
    synchronized (listLock) {
      int index = lookupSignalValidate(signal);
      List<Triggerable> recs = recievers.get(index);
      if (recs.contains(task)) {
        recs.remove(task);
      }
    }
  }

  public void unregisterAllFromSignal(OCLSignal signal) {
    synchronized (listLock) {
      int index = lookupSignalValidate(signal);
      recievers.get(index).clear();
    }
  }

  public void dispatch(OCLSignal signal) {
    synchronized (listLock) {
      List<Triggerable> recs = recievers.get(signal.getId());
      if (!recs.isEmpty()) {
        for (Iterator<Triggerable> i = recs.iterator(); i.hasNext();) {
          i.next().triggered(signal);
        }
      }
    }
  }

  private int lookupSignalValidate(OCLSignal signal) {
    int index = signals.indexOf(signal.getName());
    if (index != -1) {
      if (index != signal.getId()) {
        throw new IllegalArgumentException("Signal has wrong Id: " + signal.getId() + " originally registered with Id " + index);
      }
    } else {
      throw new RuntimeException("Unknown signal " + signal.getName());
    }
    return index;
  }
}
