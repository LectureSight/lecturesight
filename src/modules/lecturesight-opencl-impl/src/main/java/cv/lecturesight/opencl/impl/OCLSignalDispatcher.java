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

import org.pmw.tinylog.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class OCLSignalDispatcher extends Thread {

  static final int DEFAULT_QUEUE_SIZE = 100;
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
      Logger.warn("Interrupted during enqueueSignal");
    }
  }

  public void shutdown() {
    Logger.info("Shutting down");
    interrupt();
  }
}
