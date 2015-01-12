package cv.lecturesight.cameraoperator.scripted;

import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.script.Invocable;
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

  Log log = new Log("Scripted Camera Operator");
  
  @Reference
  Configuration config;
  
  ScheduledExecutorService executor;
  ScriptEngineManager engineManager;
  
  private String operatorScriptAbsolutePath;
  private File scriptFile = null;
  private File configFile = null;
  
  protected void activate(ComponentContext cc) throws Exception {
    engineManager = new ScriptEngineManager();
    
    // look for operator script and start it if existing
    makeOperatorScriptAbsolutePath();
    File file = new File(operatorScriptAbsolutePath);
    if (file.exists()) {
      this.scriptFile = file;
      start();
    }
    
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    stop();
    log.info("Deactivated");
  }

  @Override
  public void start() {
    log.info("Starting camera operator script " + scriptFile.getName());
    if (executor == null) {
      executor = Executors.newScheduledThreadPool(1);
      try {
        // set up new script space for the source to live in
        ScriptWorker scriptWorker = new ScriptWorker(scriptFile);
        
        // inject usefull stuff into the script space
        if ((configFile = findConfigFile(scriptFile)) != null) {
//          try {
//            ConfigurationBridge configB = new ConfigurationBridge(configFile);
//            scriptWorker.injectObject("Configuration", configB);
//          } catch (ScriptBridgeException e) {
//            log.warn("Failed to load script configuration. " + e.getMessage());
//          }
        }
        
        // start script execution
        scriptWorker.init();
//        int interval = config.getInt(Constants.PROPKEY_INTERVAL);
//        executor.scheduleAtFixedRate(scriptWorker, interval, interval, TimeUnit.MILLISECONDS);
      } catch (Exception e) {
        log.error("Unable to initialize script.", e);
        stop();
      }
      log.info("Script started.");
    } else {
      log.warn("Script already running!");
    }
  }

  @Override
  public void stop() {
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
      log.info("Script stopped.");
    } else {
      log.warn("Nothing to stop.");
    }
  }
  
  @Override
  public void reset() {
    stop();
    start();
  }

  private class ScriptWorker implements Runnable {
    
    ScriptEngine engine;
    Invocable engineI;
    
    public ScriptWorker(File script) throws Exception {
      engine = engineManager.getEngineByName("javascript");    
      if (engine == null) {
        String msg = "Scripting Engine could not be initialized!";
        log.error(msg);
        throw new IllegalStateException(msg);
      }
      engineI = (Invocable)engine;
      try {
        if (script != null) {
          engine.eval(new FileReader(script));
        } else {
          String msg = "No script source present!";
          log.error(msg);
          throw new IllegalStateException(msg);
        }
      } catch (FileNotFoundException e) {
        log.error("Unable to read script source.", e);
        throw e;
      } catch (ScriptException e) {
        log.error("Error while evaluating script.", e);
        throw e;
      }
    }
    
    public void init() {
      invoke("init");
    }
    
    @Override
    public void run() {
      invoke("step");
    }
    
    private void invoke(String functionName) {
      try {
        engineI.invokeFunction(functionName);
      } catch (Exception e) {
        stop();
        log.error("Error in operator script. Script was stopped. ", e);
      }
    }
    
    public void injectObject(String name, Object o) {
      engine.put(name, o);
    }
    
    public void injectPackage(String packageName) {
      try {
        StringBuilder code = new StringBuilder();
        code.append("importPackage(Packages.").append(packageName).append(");");
        engine.eval(code.toString());
      } catch (ScriptException e) {
        stop();
        log.error("Unable to import package " + packageName + " in script. ", e);
      }
    }
  }

  @Override
  public void install(File file) throws Exception {
    log.debug("Installing " + file.getName());
    if (isOperatorScript(file) && executor == null) {
      log.info("Operator script installed, starting.");
      scriptFile = file;
      start();
    }
  }

  @Override
  public void update(File file) throws Exception {
    log.debug("Updating " + file.getName());
    if (isOperatorScript(file) && executor != null) {
      log.info("Operator script updated, restarting.");
      scriptFile = file;
      reset();
    } 
  }

  @Override
  public void uninstall(File file) throws Exception {
    log.debug("Uninstalling " + file.getName());
    if (isOperatorScript(file) && executor != null) {
      log.info("Operator script uninstalled, shutting down.");
      scriptFile = null;
      stop();
    } 
  }

  @Override
  public boolean canHandle(File file) {
    makeOperatorScriptAbsolutePath();
    return file.getAbsolutePath().equals(operatorScriptAbsolutePath);
  }
  
  private void makeOperatorScriptAbsolutePath() {
    File parent = new File(Constants.SCRIPTS_DIRNAME);
    operatorScriptAbsolutePath = parent.getAbsolutePath() + File.separator + config.get(Constants.PROPKEY_SCRIPTFILE);
  }
  
  private boolean isOperatorScript(File file) {
    return file.getAbsolutePath().equals(operatorScriptAbsolutePath);
  }
  
  private File findConfigFile(File scriptFile) {
    String scriptFileName = scriptFile.getName();
    String configFileName = scriptFileName.substring(0, scriptFileName.length()-3) + ".cfg";
    File configFile = new File(scriptFile.getParent() + File.separator + configFileName);
    if (configFile.exists()) {
      return configFile;
    } else {
      return null;
    }
  }
}
