package cv.lecturesight.main;

import cv.lecturesight.util.DummyInterface;
import cv.lecturesight.util.Log;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 */
@Component(name="lecturesight.main.commands", immediate=true)
@Service()
@Properties({
@Property(name="osgi.command.scope", value="ls"),
@Property(name="osgi.command.function", value={"test","sayhello"})  
})
public class TestCommands implements DummyInterface {

  Log log = new Log("Console Commands");

  protected void activate(ComponentContext context) {
    log.info("Activated");
  }

  public void sayhello(String param[]) {
    System.out.println("Hello!");
  }

  public void test(String param[]) {
    System.out.println("Input was: " + param.toString());
  }

}
