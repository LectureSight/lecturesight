package cv.lecturesight.cameraoperator.scripted.bridge;

public class ScriptBridgeException extends Exception {
  
  public ScriptBridgeException(String message) {
    super(message);
  }
  
  public ScriptBridgeException(Exception e) {
    super(e);
  }
  
  public ScriptBridgeException(String message, Exception e) {
    super(message, e);
  }
}
