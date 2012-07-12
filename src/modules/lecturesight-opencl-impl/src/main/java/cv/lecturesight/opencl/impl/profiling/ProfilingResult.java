package cv.lecturesight.opencl.impl.profiling;

class ProfilingResult {

  private String name;
  private long launchTime;
  private long ladningTime;
  private long finishingTime;
  
  public ProfilingResult(String name, long launchT, long landT, long finishT) {
    this.name = name;
    this.launchTime = launchT;
    this.ladningTime = landT;
    this.finishingTime = finishT;
  }

  public String getName() {
    return name;
  }

  public long getLaunchTime() {
    return launchTime;
  }

  public long getLadningTime() {
    return ladningTime;
  }

  public long getFinishingTime() {
    return finishingTime;
  }
}
