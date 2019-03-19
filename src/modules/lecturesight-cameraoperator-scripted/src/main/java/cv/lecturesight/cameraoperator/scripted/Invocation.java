package cv.lecturesight.cameraoperator.scripted;

public abstract class Invocation {

  Object[] args;

  public Invocation(Object... args) {
    this.args = args;
  }

  public Object[] getArgsList() {
    return args;
  }

  public Object getArg(int index) {
    if (index > -1 && index < args.length) {
      return args[index];
    } else {
      return null;
    }
  }
}
