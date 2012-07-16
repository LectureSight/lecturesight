package cv.lecturesight.main;

import cv.lecturesight.util.DummyInterface;
import cv.lecturesight.util.Log;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 */
@Component(name = "lecturesight.main.commands", immediate = true)
@Service()
@Properties({
  @Property(name = "osgi.command.scope", value = "ls"),
  @Property(name = "osgi.command.function", value = {"start", "stop", "pause", "restart", "step"})
})
public class ConsoleCommands implements DummyInterface {

  Log log = new Log("HeartBeat");
  @Reference
  private HeartBeat main;

  protected void activate(ComponentContext context) {
    log.info("Commands activated");
  }

  public void start(String param[]) {
    try {
      ensureReady();
      main.go();
    } catch (RuntimeException e) {
      log.error("Failed to initialize HeartBeat: ", e);
    }
  }

  public void stop(String param[]) {
    if (main.isRunning()) {
      main.stop();
      main.deinit();
    }
  }

  public void pause(String param[]) {
    if (main.isRunning()) {
      try {
        main.stop();
      } catch (RuntimeException e) {
        log.error("Failed to pause HeartBeat", e);
      }
    }
  }

  public void restart(String param[]) {
    if (main.isRunning()) {
      try {
        main.stop();
        main.deinit();
      } catch (RuntimeException e) {
        log.error("Failed to stop HeartBeat", e);
      }
    }
    try {
      ensureReady();
      main.go();
    } catch (RuntimeException e) {
      log.error("Failed to start HeartBeat", e);
    }
  }

  public void step(String param[]) {
    int i = 1;
    if (param.length > 0) {
      try {
        i = Integer.parseInt(param[0]);
      } catch (NumberFormatException e) {
        log.warn("Usage: ls:step <number of steps>");
      }
    }
    try {
      ensureReady();
      main.step(i);
    } catch (IllegalStateException e) {
      log.error("Failed to step", e);
    }
  }

  private void ensureReady() throws RuntimeException {
    if (!main.isReady()) {
      main.init();
    }
  }
}
