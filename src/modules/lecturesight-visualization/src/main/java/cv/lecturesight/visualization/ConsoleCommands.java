package cv.lecturesight.visualization;

import cv.lecturesight.util.DummyInterface;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

@Component(name = "lecturesight.visualization.commands", immediate = true)
@Service()
@Properties({
  @Property(name = "osgi.command.scope", value = "vis"),
  @Property(name = "osgi.command.function", value = {"start"})
})
public class ConsoleCommands implements DummyInterface {

  @Reference
  private Visualization visual;

  protected void activate(ComponentContext context) {
    Logger.info("Commands activated");
  }

  public void start(String param[]) {
    System.out.println("NOP");
  }
}
