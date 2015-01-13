package cv.lecturesight.cameraoperator.scripted;

import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.io.*;
import java.util.Properties;
import javax.script.ScriptEngineManager;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.cameraoperator.scripted", immediate = true)
@Service
public class ScriptedCameraOperator implements CameraOperator, ArtifactInstaller {

  Log log = new Log("Scripted Camera Operator");
  
  @Reference
  Configuration config;
  
  ScriptEngineManager engineManager;
  
  File scriptFile = null;
  File configFile = null;
  
  ScriptWorker scrWorker = null;
  Thread scrWorkerThread = null;
  
  protected void activate(ComponentContext cc) throws Exception {
    engineManager = new ScriptEngineManager();
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    stop();
    log.info("Deactivated");
  }

  @Override
  public void start() {
    if (scriptFile == null) {
      throw new IllegalStateException("Cannot start, no script source present.");
    }
    log.info("Attempting to start script worker.");
    try {
      scrWorker = new ScriptWorker(scriptFile, engineManager.getEngineByName("javascript"));
      
      // make config object
      Properties props = parseConfigFile(configFile);
      scrWorker.setScriptConfig(props);
      
      // equip engine with bridge objects
      
      scrWorkerThread = new Thread(scrWorker);
      scrWorkerThread.start();
      
    } catch (Exception e) {
      log.error("Failed to instantiate script worker.", e);
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void stop() {
    if (scrWorker != null) {
      log.info("Attempting to stop script worker.");
      
      // try to gracefully stop the interpreter thread
      scrWorker.stop();
      scrWorkerThread.interrupt();    // TODO better scrWorker.interrupt() ??
      
      // wait grace period
      try {
        Thread.sleep(config.getLong(Constants.PROPKEY_TIMEOUT));
      } catch (InterruptedException e) {
        log.warn("Interruped while waiting for interpreter to exit.");
      }
      
      // if worker was not stopped, hard kill it. using deprecated stop() here as
      // there isn't really any other way.
      if (!scrWorker.isStopped()) {
        log.warn("Interpreter did not stop while grace period, hard killing interpreter thread.");
        scrWorkerThread.stop();
      }
    } else {
      log.warn("stop() called but nothing to stop.");
    }
  }
  
  @Override
  public void reset() {
    stop();
    start();
  }

  @Override
  public void install(File file) throws Exception {
    if (file.getAbsolutePath().equals(getScriptFilePath())) {
      scriptFile = file;
      start();
    } else if (file.getAbsolutePath().equals(getScriptFilePath())) {
      configFile = file;      
    }
  }

  @Override
  public void update(File file) throws Exception {
    if (file.getAbsolutePath().equals(getScriptFilePath())) {
      scriptFile = file;
      reset();
    } else if (file.getAbsolutePath().equals(getScriptFilePath())) {
      configFile = file;
      
      // TODO update Config object in script space also for running script??
    }
  }

  @Override
  public void uninstall(File file) throws Exception {
    if (file.getAbsolutePath().equals(getScriptFilePath())) {
      scriptFile = null;
      stop();
    } else if (file.getAbsolutePath().equals(getScriptFilePath())) {
      configFile = null;
    }
  }

  @Override
  public boolean canHandle(File file) {
    String path = file.getAbsolutePath();
    return getScriptFilePath().equals(path) || getConfigFilePath().equals(path);
  }
  
  private Properties parseConfigFile(File f) {
    Properties cfg = new Properties();
    try {
      cfg.load(new FileReader(f));
    } catch (Exception e) {
      log.error("Failed to load configuration file for script. ", e);
    }
    return cfg;
  }
  
  public String getScriptDir() {
    String sd = config.get(Constants.PROPKEY_SCRIPTDIR);
    if (sd.startsWith("/")) {
      return sd;
    } else {
      String cwd = System.getProperty("user.dir");
      return cwd + File.separator + sd;
    }
  }
  
  public String getScriptFilePath() {
    String scriptDir = getScriptDir();
    return scriptDir + File.separator + config.get(Constants.PROPKEY_SCRIPTFILE);
  }
  
  public String getConfigFilePath() {
    String scriptDir = getScriptDir();
    String filename = config.get(Constants.PROPKEY_SCRIPTFILE);
    filename = filename.replace(".js", ".conf");
    return scriptDir + File.separator + filename;
  }
}
