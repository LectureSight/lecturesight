package cv.lecturesight.scripting.api;

public interface ScriptParent {
  
  public void invokeCallback(Object func, Object[] args);
  
  public void invokeMethod(String name, Object... args);
  
  public Object invokeFunction(String name, Object... args);
}
