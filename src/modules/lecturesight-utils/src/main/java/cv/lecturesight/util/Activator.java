package cv.lecturesight.util;

import cv.lecturesight.util.conf.ConfigCommands;
import cv.lecturesight.util.conf.ConfigurationFactory;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.log.listener.ConsoleOutputLogListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

public final class Activator implements BundleActivator {

  final static String LOGLISTENER_ACTIVE_PROPERTY = "cv.opentrack.util.log.console.enabled";
  final static String CONFIG_NAME = "opentrack.properties";
  final static String CONFIG_PATH_PROPERTY = "cv.opentrack.config.path";
  private ServiceRegistration consoleLogRegistration = null;
  private ServiceRegistration confFactoryReg;
  private Log log = new Log("Activator");
  private File configFile;
  private Properties defaultProperties = new Properties();
  private Properties systemProperties = new Properties(defaultProperties);

  @Override
  public void start(BundleContext context) throws Exception {

    // TODO: wrap those two into a service tracker!!
    // set LogService for OpenTrack Log
    ServiceReference logRef = context.getServiceReference(LogService.class.getName());
    if (logRef != null) {
      Log.setLogService((LogService) context.getService(logRef));
    }
    // activate log console output if configured
    String active = context.getProperty(LOGLISTENER_ACTIVE_PROPERTY);
    if (active != null && ("TRUE".equalsIgnoreCase(active) || "YES".equalsIgnoreCase(active))) {
      ConsoleOutputLogListener consoleLog = new ConsoleOutputLogListener();
      ServiceReference ref = context.getServiceReference(LogReaderService.class.getName());
      if (ref != null) {
        LogReaderService reader = (LogReaderService) context.getService(ref);
        reader.addLogListener(consoleLog);
      }
    }

    // get config file
    File configPath = getConfigPath(context);
    configFile = new File(configPath.getAbsolutePath() + File.separator + CONFIG_NAME);
    ensureFile(configFile);

    // load properties
    try {
      systemProperties.load(new FileInputStream(configFile));
      log.debug("Loaded config properties from " + configFile.getAbsolutePath());
    } catch (IOException e) {
      log.warn("Failed to load config from " + configFile.getAbsolutePath());
    }

    // register config factory
    ConfigurationFactory confFactory = new ConfigurationFactory(systemProperties, defaultProperties);
    confFactoryReg = context.registerService(Configuration.class.getName(), confFactory, null);

    // register config commands
    ConfigCommands commandImpl = new ConfigCommands(systemProperties, defaultProperties);
    Dictionary<String, Object> commands = new Hashtable<String, Object>();
    commands.put("osgi.command.scope", "config");
    commands.put("osgi.command.function", ConfigCommands.commands);
    context.registerService(ConfigCommands.class.getName(), commandImpl, commands);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    // TODO: save config properties ? 
  }

  private File getConfigPath(BundleContext bc) {

    // try to get config dir from framework property
    String path = bc.getProperty(CONFIG_PATH_PROPERTY);
    if (path != null) {
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) {
        log.debug("Using config directory specified in framework property: " + dir.getAbsolutePath());
        return dir;
      } else {
        log.warn("Config directory specified in framework property does not exist: " + dir.getAbsolutePath());
      }
    }

    // try to get config dir from system property
    path = System.getProperty(CONFIG_PATH_PROPERTY);
    if (path != null) {
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) {
        log.debug("Using config directory specified in system property: " + dir.getAbsolutePath());
        return dir;
      } else {
        log.warn("Config directory specified in framework property does not exist: " + dir.getAbsolutePath());
      }
    }

    // try to get default config dir
    path = "conf";
    if (path != null) {
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) {
        log.debug("Using default config directory: " + dir.getAbsolutePath());
        return dir;
      } else {
        log.warn("Default config directory does not exist: " + dir.getAbsolutePath());
      }
    }

    File dir = new File(".");
    log.debug("Using working directory as config directory: " + dir.getAbsolutePath());
    return dir;
  }

  private void ensureFile(File file) throws ServiceException {
    if (!file.exists()) {
      log.warn("Config file does not exist, creating " + file.getAbsolutePath());
      try {
        file.createNewFile();
      } catch (IOException ex) {
        throw new ServiceException("Failed to create config file: " + file.getAbsolutePath(), ex);
      }
    }
  }
}
