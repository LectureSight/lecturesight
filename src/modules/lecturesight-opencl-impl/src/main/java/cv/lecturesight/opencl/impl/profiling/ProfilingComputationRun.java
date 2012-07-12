package cv.lecturesight.opencl.impl.profiling;

import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.opencl.api.ComputationRun;

public class ProfilingComputationRun implements ComputationRun {

  private ComputationRun run;
  private ProfilingServer profiler;
  
  private String name;
  private long launchTime;
  private long landingTime;
  private long finishingTime;
  
  public ProfilingComputationRun(ComputationRun run, ProfilingServer profiler) {
    this.run = run;
    this.profiler = profiler;
    this.name = run.getClass().getName();
  }
  
  @Override
  public void launch(CLQueue queue) {
    launchTime = System.nanoTime();
    run.launch(queue);
  }

  @Override
  public void land() {
    landingTime = System.nanoTime();
    run.land();
    finishingTime = System.nanoTime();
    profiler.record(new ProfilingResult(name, launchTime, landingTime, finishingTime));
  }
}
