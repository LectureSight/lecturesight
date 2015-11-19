package cv.lecturesight.scripting.stub;

import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptingService;
import cv.lecturesight.util.Log;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.scripting.stub", immediate = true)
@Service
public class ScriptingStub implements ScriptingService {

  Log log = new Log("Scripting Service Stub");    // logger for this service
  
  protected void activate(ComponentContext cc) throws Exception {
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated");
  }

  @Override
  public void registerSerivceObject(String identifier, ScriptBridge serviceObject, String[] requiredImports) {
    log.info("Stub-registering " + identifier);
  }

  @Override
  public Log getLogger() {
    return log;
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
}
