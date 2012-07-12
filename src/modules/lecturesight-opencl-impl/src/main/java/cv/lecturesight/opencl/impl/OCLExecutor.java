package cv.lecturesight.opencl.impl;

import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.util.Log;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** A Thread that takes care of putting the OpenCL commands of issued
 *  ComputationRuns orderly into a single CL command queue so that memory access
 *  from different runs won't collide. The run is then wrapped into a
 *  <code>CallbackExecution</code> and handed over to a thread pool. The
 *  CallbackExecution is waiting for the marker event indicating that all the
 *  OpenCL commands issued by the run have been executed. When the marker event
 *  arrives the <code>land()</code> method of the run is executed.
 */
public class OCLExecutor extends Thread {

  final static int DEFAULT_QUEUE_SIZE = 100;
  private Log log = new Log("OpenCL Executor");
  private CLQueue oclQueue;
  private final BlockingQueue<ComputationRun> taskQueue;
  private final ExecutorService callbackExecutor = Executors.newCachedThreadPool();   // TODO think about the question if this is the right type of thread pool

  public OCLExecutor(CLQueue queue) {
    this.oclQueue = queue;
    taskQueue = new ArrayBlockingQueue<ComputationRun>(DEFAULT_QUEUE_SIZE);
  }

  public OCLExecutor(CLQueue queue, int queueSize) {
    this.oclQueue = queue;
    taskQueue = new ArrayBlockingQueue<ComputationRun>(queueSize);
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        final ComputationRun run = taskQueue.take();
        if (launch(run)) {
          CLEvent marker = oclQueue.enqueueMarker();
          callbackExecutor.execute(new CallbackExecution(marker, run));
        }
      }
    } catch (InterruptedException e) {
      callbackExecutor.shutdownNow();   // TODO is it right to terminate execution this way?
    }
  }

  private boolean launch(ComputationRun run) {
    try {
      run.launch(oclQueue);
      return true;
    } catch (Exception e) {
      String warn = run.getClass().getName() + " crashed during launch due to " + e.getClass().getSimpleName();
      String msg = e.getMessage();
      if (msg != null) {
        warn += ": " + msg;
      }
      log.warn(warn);
      return false;
    }
  }

  void enqueueRun(ComputationRun run) {
    synchronized (taskQueue) {
      try {
        taskQueue.put(run);
      } catch (InterruptedException e) {
        log.warn("Interrupted during enqueueRun()");
      }
    }
  }

  public void shutdown() {
    log.info("Shutting down");
    interrupt();
  }

  private class CallbackExecution implements Runnable {

    private CLEvent event;
    private ComputationRun run;

    public CallbackExecution(CLEvent event, ComputationRun run) {
      this.event = event;
      this.run = run;
    }

    @Override
    public void run() {
      boolean landing = false;
      try {
        event.waitFor();
        landing = true;
      } catch (Exception e) {
        String warn = "Error in " + run.getClass().getName() + " while waiting for marker event due to " + e.getClass().getSimpleName();
        String msg = e.getMessage();
        if (msg != null) {
          warn += ": " + msg;
        }
        log.warn(warn);
      }
      try {
        if (landing) {
          run.land();
        }
      } catch (Exception e) {
        String warn = run.getClass().getName() + " crashed while landing due to " + e.getClass().getSimpleName();
        String msg = e.getMessage();
        if (msg != null) {
          warn += ": " + msg;
        }
        log.warn(warn);
      }
    }
  }
}
