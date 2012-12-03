package cv.lecturesight.cameraoperator.scripted;

import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.CoordinatesNormalization;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.cameraoperator.scripted", immediate = true)
@Service
public class ScriptedCameraOperator implements CameraOperator, ArtifactInstaller {
  
  private final static String PROPKEY_INTERVAL = "interval";
  private final static String PROPKEY_SCRIPTFILE = "script";

  Log log = new Log("Scripted Camera Operator");
  
  @Reference
  Configuration config;
  
  @Reference
  ObjectTracker tracker;
  
  @Reference
  CameraSteeringWorker camera;
  
  @Reference
  FrameSourceProvider fsp;
  FrameSource fsrc;
  
  ScheduledExecutorService executor;
  CoordinatesNormalization normalizer;
  ScriptEngineManager engineManager;
  
  File scriptSource;
  
  protected void activate(ComponentContext cc) throws Exception {
    fsrc = fsp.getFrameSource();
    engineManager = new ScriptEngineManager();
    normalizer = new CoordinatesNormalization(fsrc.getWidth(), fsrc.getHeight());
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    stop();
    log.info("Deactivated");
  }

  @Override
  public void start() {
    if (executor == null) {
      executor = Executors.newScheduledThreadPool(1);
      try {
        ScriptWorker worker = new ScriptWorker(scriptSource, tracker, camera);
        int interval = config.getInt(PROPKEY_INTERVAL);
        executor.scheduleAtFixedRate(worker, 0, interval, TimeUnit.MILLISECONDS);
      } catch (Exception e) {
        log.error("Unable to initialize script.", e);
        stop();
      }
      log.info("Script started");
    } else {
      log.warn("Script already running!");
    }
  }

  @Override
  public void stop() {
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
      log.info("Script stopped");
    } else {
      log.warn("Nothing to stop");
    }
  }
  
  @Override
  public void reset() {
    stop();
    start();
  }

  private class ScriptWorker implements Runnable {
    
    ScriptEngine engine;
    
    public ScriptWorker(File script, ObjectTracker oTracker, CameraSteeringWorker steeringWorker) throws Exception {
      engine = engineManager.getEngineByName("javascript");    // build into JVM so we are not checking if an engine was returned
      engine.put("Tracker", oTracker);
      engine.put("Camera", steeringWorker);
      try {
        if (script != null) {
          engine.eval(new FileReader(script));
          engine.eval("init();");
        } else {
          log.warn("No script source!");
          throw new IllegalStateException("No script source found.");
        }
      } catch (FileNotFoundException e) {
        log.error("Unable to read script source.", e);
        throw e;
      } catch (ScriptException e) {
        log.error("Error while evaluating script.", e);
        throw e;
      }
    }
    
    @Override
    public void run() {
      try {
        engine.eval("step();");
      } catch (ScriptException e) {
        log.error("Error in operator script!", e);
        stop();
      }
    }
  }

  @Override
  public void install(File file) throws Exception {
    log.debug("Installing " + file.getName());
    if (isOperatorScript(file) && executor == null) {
      log.info("Operator script installed, starting.");
      scriptSource = file;
      start();
    }
  }

  @Override
  public void update(File file) throws Exception {
    log.debug("Updating " + file.getName());
    if (isOperatorScript(file) && executor != null) {
      log.info("Operator script updated, restarting.");
      scriptSource = file;
      reset();
    } 
  }

  @Override
  public void uninstall(File file) throws Exception {
    log.debug("Uninstalling " + file.getName());
    if (isOperatorScript(file) && executor != null) {
      log.info("Operator script uninstalled, shutting down.");
      scriptSource = null;
      stop();
    } 
  }

  @Override
  public boolean canHandle(File file) {
    return file.getName().endsWith(".js") && file.getParent().equalsIgnoreCase("scripts");  // TODO come up with better check for script dir
  }
  
  private boolean isOperatorScript(File file) {
    return file.getName().equalsIgnoreCase(config.get(PROPKEY_SCRIPTFILE));
  } 
}
