package cv.lecturesight.opencl.impl;

import com.nativelibs4java.opencl.CLContext;
import cv.lecturesight.opencl.OCLProgramStore;
import cv.lecturesight.opencl.OCLUtils;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.OCLSignalBarrier;
import cv.lecturesight.opencl.api.Triggerable;
import cv.lecturesight.opencl.impl.profiling.ProfilingComputationRun;
import cv.lecturesight.opencl.impl.profiling.ProfilingServer;
import java.util.Map;
import java.util.UUID;

// FIXME unregister all previously registered runs on shutdown!
public class OpenCLServiceImpl implements OpenCLService {

  OCLSignalDispatcher dispatcher;
  OCLExecutor executor;
  CLContext context;
  static OCLUtils utils;
  OCLProgramStore programs;
  Map<ComputationRun, OCLExecution> executions = new java.util.HashMap<ComputationRun, OCLExecution>();
  boolean doProfiling = false;
  ProfilingServer profiler;
  
  @Override
  public CLContext context() {
    return context;
  }

  @Override
  public OCLProgramStore programs() {
    return programs;
  }

  @Override
  public void immediateLaunch(ComputationRun run) {
    executor.enqueueRun(run);
  }

  @Override
  public OCLSignal getSignal(String name) {
    if (dispatcher.signalManager.signalExists(name)) {
      return dispatcher.signalManager.getSignal(name);
    } else {
      return dispatcher.signalManager.createSignal(name);
    }
  }

  @Override
  public void castSignal(OCLSignal signal) {
    dispatcher.enqueueSignal(signal);
  }

  @Override
  public void registerLaunch(OCLSignal trigger, ComputationRun run) {
    OCLExecution execution;
    if (doProfiling) {
      execution = new OCLExecution(new ProfilingComputationRun(run, profiler), executor);
    } else {
      execution = new OCLExecution(run, executor);
    }
    executions.put(run, execution);
    dispatcher.signalManager.registerWithSignal(trigger, execution);
  }

  @Override
  public void registerLaunch(OCLSignal[] triggers, ComputationRun run) {
    OCLExecution execution;
    if (doProfiling) {
      execution = new OCLExecution(new ProfilingComputationRun(run, profiler), executor);
    } else {
      execution = new OCLExecution(run, executor);
    }
    executions.put(run, execution);
    for (OCLSignal signal : triggers) {
      dispatcher.signalManager.registerWithSignal(signal, execution);
    }
  }

  @Override
  public void unregisterLaunch(OCLSignal trigger, ComputationRun run) {
    if (executions.containsKey(run)) {                                  
      dispatcher.signalManager.unregisterFromSignal(trigger, executions.get(run));
    }
  }

  @Override
  public void registerTriggerable(OCLSignal trigger, Triggerable handler) {
    dispatcher.signalManager.registerWithSignal(trigger, handler);
  }

  @Override
  public void registerTriggerable(OCLSignal[] triggers, Triggerable handler) {
    for (OCLSignal signal : triggers) {
      registerTriggerable(signal, handler);
    }
  }

  @Override
  public void unregisterTriggerable(OCLSignal trigger, Triggerable handler) {
    dispatcher.signalManager.unregisterFromSignal(trigger, handler);
  }

  @Override
  public OCLUtils utils() {
    return utils;
  }

  @Override
  public OCLSignalBarrier createSignalBarrier(OCLSignal[] triggers) {
    OCLSignal newsig = getSignal("BARRIER-" + UUID.randomUUID().toString());
    OCLSignalBarrier out = new OCLSignalBarrierImpl(triggers, newsig, dispatcher);
    registerTriggerable(triggers, out);
    return out;
  }

}
