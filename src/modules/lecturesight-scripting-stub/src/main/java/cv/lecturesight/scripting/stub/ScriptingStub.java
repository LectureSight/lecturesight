package cv.lecturesight.scripting.stub;

import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptingService;

import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

public class ScriptingStub implements ScriptingService {

  protected void activate(ComponentContext cc) throws Exception {
    Logger.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    Logger.info("Deactivated");
  }

  @Override
  public void registerSerivceObject(String identifier, ScriptBridge serviceObject, String[] requiredImports) {
    Logger.info("Stub-registering " + identifier);
  }

  @Override
  public void invokeCallback(Object function, Object[] args) {
    throw new UnsupportedOperationException("invokeCallback() is not implemented.");
  }

  @Override
  public void invokeMethod(String method, Object... args) {
    throw new UnsupportedOperationException("invokeMethod() is not implemented.");
  }

  @Override
  public void invokeMethod(Object method, Object... args) {
    throw new UnsupportedOperationException("invokeMethod() is not implemented.");
  }

  @Override
  public Object invokeFunction(String function, Object... args) {
    throw new UnsupportedOperationException("invokeFunction() is not implemented.");
  }

  @Override
  public long getTimeOfStart() {
    return 0L;
  }
}

