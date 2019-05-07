package cv.lecturesight.cameraoperator.scripted;

import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.scripting.api.ScriptBridge;
import cv.lecturesight.scripting.api.ScriptingService;
import cv.lecturesight.util.conf.Configuration;

import org.apache.felix.fileinstall.ArtifactInstaller;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ScriptedCameraOperator implements CameraOperator, ScriptingService, ArtifactInstaller {

  @Setter
  Configuration config;               // service configuration

  File scriptFile = null;             // script file when loaded
  File configFile = null;             // config file for script

  ScriptWorker scriptWorker = null;   // worker executing the script
  Thread workerThread = null;         // thread handle for worker's execution

  List<BridgeRegistration> serviceObjects;  // collection of registetred service objects
  // that are made available in the script scope

  long lastStart = 0L;

  public ScriptedCameraOperator() {
    serviceObjects = new LinkedList<BridgeRegistration>();
  }

  protected void activate(ComponentContext cc) throws Exception {
    Logger.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    stop();
    Logger.info("Deactivated");
  }

  /** Readies the script for execution if a script file is loaded.
   *
   */
  @Override
  public void start() {
    if (scriptFile == null) {
      throw new IllegalStateException("Cannot start, no script source present.");
    }
    Logger.info("Attempting to start script worker.");
    try {
      scriptWorker = new ScriptWorker(scriptFile.getName());

      // make config object
      if (configFile != null) {
        Properties props = parseConfigFile(configFile);
        scriptWorker.setScriptConfig(props);
      }

      // equip engine with bridge objects
      for (BridgeRegistration reg : serviceObjects) {

        // load required imports into script scope
        for (String pkg : reg.imports) {
          scriptWorker.addImport(pkg);
        }

        // add bridge object to script scope
        scriptWorker.addScriptObject(reg.identifier, reg.bridgeObject);
      }

      // load and evaluate script
      // throws IllegalStateException
      scriptWorker.load(scriptFile);

      // create script worker thread and start it
      workerThread = new Thread(scriptWorker);
      workerThread.start();

      // save timestamp of script launch
      lastStart = System.currentTimeMillis();

    } catch (Exception e) {
      Logger.error("Failed to instantiate script worker.", e);
      throw new IllegalStateException(e);
    }
  }

  /** Stops script execution. The method tries first to stop the script worker
   * gracefully requesting it to exit / interrupting it in a wait state, if it
   * is still running after a configurable amount of time the thread is killed.
   *
   */
  @Override
  public void stop() {
    if (scriptWorker != null) {
      Logger.info("Attempting to stop script worker.");

      // try to gracefully stop the interpreter thread
      scriptWorker.stop();
      workerThread.interrupt();    // TODO better scrWorker.interrupt() ??

      // wait grace period
      try {
        Thread.sleep(config.getLong(Constants.PROPKEY_TIMEOUT));
      } catch (InterruptedException e) {
        Logger.warn("Interruped while waiting for interpreter to exit.");
      }

      // if worker was not stopped, hard kill it. Using deprecated Thread.stop()
      // here as there isn't really any other way.
      if (!scriptWorker.isStopped()) {
        Logger.warn("Script worker did not stop, hard killing thread.");
        workerThread.stop();
      }
    } else {
      Logger.warn("stop() called but nothing to stop.");
    }
  }

  @Override
  public void reset() {
    stop();
    start();
  }

  @Override
  public boolean isRunning() {
    return (scriptWorker != null && !scriptWorker.isStopped());
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

  /** Loads the properties from the specified file into a <code>Properties</code>
   * object.
   *
   * @param f
   * @return
   */
  private Properties parseConfigFile(File f) {
    Properties cfg = new Properties();
    try {
      cfg.load(new FileReader(f));
    } catch (Exception e) {
      Logger.error("Failed to load configuration file for script. ", e);
    }
    return cfg;
  }

  /** Returns the configured script file directory path.
   *
   * @return script file directory path
   */
  public String getScriptDir() {
    String sd = config.get(Constants.PROPKEY_SCRIPTDIR);
    if (sd.startsWith("/")) {
      return sd;
    } else {
      String cwd = System.getProperty("user.dir");
      return cwd + File.separator + sd;
    }
  }

  /** Returns the full path of the configured operator script.
   *
   * @return full path of operator script
   */
  public String getScriptFilePath() {
    String scriptDir = getScriptDir();
    return scriptDir + File.separator + config.get(Constants.PROPKEY_SCRIPTFILE);
  }

  /** Returns the full path of the configuration file for the configured script
   * file.
   *
   * @return path of config file
   */
  public String getConfigFilePath() {
    String scriptDir = getScriptDir();
    String filename = config.get(Constants.PROPKEY_SCRIPTFILE);
    filename = filename.replace(".js", ".conf");
    return scriptDir + File.separator + filename;
  }

  @Override
  public long getTimeOfStart() {
    return lastStart;
  }

  // _____________________ Methods from ScriptingService _______________________
  @Override
  public void registerSerivceObject(String identifier, ScriptBridge serviceObject, String[] requiredImports) {
    if (requiredImports == null) {
      requiredImports = new String[0];
    }
    BridgeRegistration reg = new BridgeRegistration(identifier, serviceObject, requiredImports);
    serviceObjects.add(reg);
  }

  @Override
  public void invokeCallback(Object function, Object[] args) {
    if (scriptWorker != null && !scriptWorker.isStopped()) {
      scriptWorker.invokeCallback(function, args);
    } else {
      Logger.warn("invokeCallback() called but no script running.");
    }
  }

  @Override
  public void invokeMethod(String method, Object... args) {
    if (scriptWorker != null && !scriptWorker.isStopped()) {
      scriptWorker.invokeMethod(method, args);
    } else {
      Logger.warn("invokeMethod() called but no script running.");
    }
  }

  @Override
  public void invokeMethod(Object method, Object... args) {
    if (scriptWorker != null && !scriptWorker.isStopped()) {
      scriptWorker.invokeCallback(method, args);
    } else {
      Logger.warn("invokeMethod() called but no script running.");
    }
  }

  @Override
  public Object invokeFunction(String function, Object... args) {
    throw new UnsupportedOperationException("invokeFunction() is not implemented.");
  }

  @Override
  public List<TrackerObject> getFramedTargets() {
    return Collections.emptyList();
  }

}
