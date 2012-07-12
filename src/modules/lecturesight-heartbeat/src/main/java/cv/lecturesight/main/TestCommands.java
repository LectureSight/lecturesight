package cv.lecturesight.main;

import cv.lecturesight.util.Log;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 * @scr.component name="lecturesight.main.commands" immediate="true"
 * @scr.service interface="cv.lecturesight.main.TestCommands"
 * @scr.property name="osgi.command.scope" value="ot"
 * @scr.property name="osgi.command.function" cardinality="2" values0="test" values1="sayhello"
 */
public class TestCommands {

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
