package cv.lecturesight.opencl.impl;

import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.Log;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class OCLSignalDispatcher extends Thread {

  static final int DEFAULT_QUEUE_SIZE = 100;
  private Log log = new Log("OpenCL Signal Dispatcher");
  SignalManager signalManager = new SignalManager();
  private BlockingQueue<OCLSignal> signalQueue;

  public OCLSignalDispatcher() {
    signalQueue = new ArrayBlockingQueue<OCLSignal>(DEFAULT_QUEUE_SIZE);
  }

  public OCLSignalDispatcher(int queueSize) {
    signalQueue = new ArrayBlockingQueue<OCLSignal>(queueSize);
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        OCLSignal signal = signalQueue.take();
        signalManager.dispatch(signal);
      }
    } catch (InterruptedException e) {
      // thread exits
    }
  }

  public synchronized void enqueueSignal(OCLSignal signal) {
    try {
      signalQueue.put(signal);
    } catch (InterruptedException e) {
      log.warn("Interrupted during enqueueSignal");
    }
  }

  public void shutdown() {
    log.info("Shutting down");
    interrupt();
  }
}
