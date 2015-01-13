package cv.lecturesight.cameraoperator.scripted;

public class Invocation {

  String function;
  Object[] args;
  
  public Invocation(String function, Object... args) {
    this.function = function;
    this.args = args;
  }
}
