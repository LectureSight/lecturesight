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
