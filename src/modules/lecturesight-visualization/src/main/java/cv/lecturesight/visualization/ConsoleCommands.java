package cv.lecturesight.visualization;

import cv.lecturesight.util.DummyInterface;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

public class ConsoleCommands implements DummyInterface {

  @Setter
  private Visualization visual;

  protected void activate(ComponentContext context) {
    Logger.info("Commands activated");
  }

  public void start(String param[]) {
    System.out.println("NOP");
  }
}
