package cv.lecturesight.scripting.api;

import cv.lecturesight.util.Log;

public interface ScriptingService {

  /** Registers <code>serviceObject</code> with the scripting engine, making it 
   * available within the script with the specified <code>identifier</code>. 
   * All Java packaged listed in <code>requiredImports</code> will be imported
   * into the script scope before the object is made available.
   * 
   * @param identifier
   * @param serviceObject
   * @param requiredImports 
   */
  public void registerSerivceObject(String identifier, ScriptBridge serviceObject, String[] requiredImports);
  
  /** Return the script's logger so that the consumer can log messages in behalf 
   * of the script instance.
   * 
   * @return script's logger
   */
  public Log getLogger();
  
  /** Calls the specified <code>function</code> immediately with the specified
   * <code>args</code>. This method is only intended for calling a callback from
   * inside the same (script executing) thread that lead execution back into 
   * Java space (i.e. by calling a method on a service object).
   * 
   * @param function
   * @param args 
   */
  public void invokeCallback(Object function, Object[] args);
  
  /** Schedules the execution of the specified <code>method</code> by the script
   * executing thread.
   * 
   * @param method
   * @param args 
   */
  public void invokeMethod(String method, Object... args);
  
  /** Schedules the execution of the specified <code>method</code> by the script
   * executing thread.
   * 
   * @param method
   * @param args 
   */
  public void invokeMethod(Object method, Object... args);
  
  /**
   * 
   * @param function
   * @param args
   * @return 
   */
  public Object invokeFunction(String function, Object... args);
}
