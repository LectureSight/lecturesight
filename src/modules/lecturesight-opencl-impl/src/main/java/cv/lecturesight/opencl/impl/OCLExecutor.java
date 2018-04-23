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

import cv.lecturesight.opencl.api.ComputationRun;

import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

import org.pmw.tinylog.Logger;

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
  private CLQueue oclQueue;
  private final BlockingQueue<ComputationRun> taskQueue;
  private final ExecutorService callbackExecutor = Executors.newCachedThreadPool();   // TODO think about the question if this is the right type of thread pool

  OCLExecutor(CLQueue queue) {
    this.oclQueue = queue;
    taskQueue = new ArrayBlockingQueue<ComputationRun>(DEFAULT_QUEUE_SIZE);
  }

  OCLExecutor(CLQueue queue, int queueSize) {
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
      Logger.debug("Interrupted");
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
      Logger.error(e, warn);
      return false;
    }
  }

  void enqueueRun(ComputationRun run) {
    synchronized (taskQueue) {
      try {
        taskQueue.put(run);
      } catch (InterruptedException e) {
        Logger.warn("Interrupted during enqueueRun()");
      }
    }
  }

  public void shutdown() {
    Logger.info("Shutting down");
    interrupt();
  }

  private class CallbackExecution implements Runnable {

    private CLEvent event;
    private ComputationRun run;

    CallbackExecution(CLEvent event, ComputationRun run) {
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
        Logger.error(e, warn);
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
        Logger.error(e, warn);
      }
    }
  }
}
