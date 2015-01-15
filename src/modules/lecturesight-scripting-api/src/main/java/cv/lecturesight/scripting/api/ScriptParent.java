package cv.lecturesight.scripting.api;

import cv.lecturesight.util.Log;

public interface ScriptParent {

  public Log getLogger();
  
  public void invokeCallback(Object func, Object[] args);
  
  public void invokeMethod(String name, Object... args);
  
  public Object invokeFunction(String name, Object... args);
}
