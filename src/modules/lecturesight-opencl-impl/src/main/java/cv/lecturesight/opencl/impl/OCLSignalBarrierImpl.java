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
import cv.lecturesight.opencl.api.OCLSignalBarrier;

import java.util.ArrayList;
import java.util.List;

public class OCLSignalBarrierImpl implements OCLSignalBarrier {

  private final OCLSignal barrierSignal;
  private OCLSignalDispatcher dispatcher;
  private final List<Integer> waitingFor;    // TODO set to specific size?
  private List<Integer> arrived = new ArrayList<Integer>();

  public OCLSignalBarrierImpl(OCLSignal[] awaited, OCLSignal barrierSignal, OCLSignalDispatcher dispatcher) {
    this.dispatcher = dispatcher;
    this.barrierSignal = barrierSignal;
    waitingFor = new ArrayList(awaited.length);
    for (int i = 0; i < awaited.length; i++) {
      waitingFor.add(awaited[i].getId());
    }
  }

  @Override
  public void triggered(OCLSignal signal) {
    if (!arrived.contains(signal.getId())) {
      arrived.add(signal.getId());
      if (allArrived()) {
        arrived.clear();
        dispatcher.enqueueSignal(barrierSignal);
      }
    }
  }

  private boolean allArrived() {
    return arrived.containsAll(waitingFor);
  }

  @Override
  public OCLSignal getSignal() {
    return barrierSignal;
  }
}
