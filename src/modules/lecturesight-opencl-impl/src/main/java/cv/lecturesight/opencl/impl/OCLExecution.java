package cv.lecturesight.opencl.impl;

import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.Triggerable;

public class OCLExecution implements Triggerable {

  private ComputationRun run;
  private OCLExecutor executor;

  public OCLExecution(ComputationRun run, OCLExecutor executor) {
    this.run = run;
    this.executor = executor;
  }

  @Override
  public void triggered(OCLSignal signal) {
    executor.enqueueRun(run);
  }
}
