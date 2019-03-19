package cv.lecturesight.cameraoperator.scripted;

public class InvocationByReference extends Invocation {

  Object reference;

  public InvocationByReference(Object reference, Object... args) {
    super(args);
    this.reference = reference;
  }

  public Object getReference() {
    return reference;
  }
}
