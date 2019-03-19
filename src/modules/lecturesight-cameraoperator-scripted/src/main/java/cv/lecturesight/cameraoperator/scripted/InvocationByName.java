package cv.lecturesight.cameraoperator.scripted;

public class InvocationByName extends Invocation {

  String name;

  public InvocationByName(String name, Object... args) {
    super(args);
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
