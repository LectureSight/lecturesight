package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.util.DummyInterface;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.geometry.NormalizedPosition;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Component(name = "cv.lecturesight.ptz.steering.commands", immediate = true)
@Service()
@Properties({
  @Property(name = "osgi.command.scope", value = "cs"),
  @Property(name = "osgi.command.function", value = {"on", "off", "start", "stop", "restart", "move", "stopmove", "home", "ui", "update"})
})
public class ConsoleCommands implements DummyInterface {
  
  Log log = new Log("Camera Steering Commands");
  
  @Reference
  CameraSteeringWorker steerer;
  
  public void on(String[] args) {
    steerer.setSteering(true);
  }
  
  public void off(String[] args) {
    steerer.setSteering(false);
  }
  
  public void start(String[] args) {
    steerer.start();
  }
  
  public void stop(String[] args) {
    steerer.stop();
  }
  
  public void restart(String[] args) {
    steerer.stop();
    steerer.start();
  }
  
  public void move(String[] args) {
    try {
      steerer.setTargetPosition(getPosition(args));
    } catch (IllegalArgumentException e) {
      log.warn(e.getMessage());
      System.out.println("Usage: cs:move (float)x (float)y");
    }
  }
  
  private NormalizedPosition getPosition(String[] args) throws IllegalArgumentException {
    if (args.length < 2) {
      throw new IllegalArgumentException("Not enough arguments!");
    }
    try {
      float x = Float.parseFloat(args[0]);
      float y = Float.parseFloat(args[1]);
      return new NormalizedPosition(x, y);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse arguments: " + e.getMessage());
    }
  }
  
  public void stopmove(String[] args) {
    steerer.stopMoving();
  }
  
  public void home(String[] args) {
    steerer.setTargetPosition(new NormalizedPosition(0.0f, 0.0f));
  }
  
  public void ui(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: cs:ui show|hide");
    } else if (args[0].equalsIgnoreCase("show")) {
      ((CameraSteeringWorkerImpl)steerer).ui.show(true);
    } else if (args[0].equalsIgnoreCase("hide")) {
      ((CameraSteeringWorkerImpl)steerer).ui.show(false);
    }
  }
  
  public void update(String[] args) {
    ((CameraSteeringWorkerImpl)steerer).updateConfiguration();
  }
}
