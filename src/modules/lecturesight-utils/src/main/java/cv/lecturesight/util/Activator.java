/* Copyright (C) 2012 Benjamin Wulff
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package cv.lecturesight.util;

import cv.lecturesight.util.conf.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceRegistration;
import org.pmw.tinylog.Logger;

public final class Activator implements BundleActivator {

  final static String LOGLISTENER_ACTIVE_PROPERTY = "cv.lecturesight.util.log.console.enabled";
  final static String LOGLISTENER_COLOR_ENABLED = "cv.lecturesight.util.log.console.color";
  final static String LOGLISTENER_SUPPRESS_OSGI_EVENTS = "cv.lecturesight.util.log.suppress.osgievents";
  final static String CONFIG_NAME = "lecturesight.properties";
  final static String CONFIG_PATH_PROPERTY = "cv.lecturesight.config.path";
  private ServiceRegistration consoleLogRegistration = null;
  private ServiceRegistration confFactoryReg;
  private File configFile;
  private Properties defaultProperties = new Properties();
  private Properties systemProperties = new Properties(defaultProperties);

  @Override
  public void start(BundleContext context) throws Exception {

//    // set LogService for lecturesight Log
//    ServiceReference logRef = context.getServiceReference(LogService.class.getName());
//    if (logRef != null) {
//      Log.setLogService((LogService) context.getService(logRef));
//    }
//    // activate log console output if configured
//    String active = context.getProperty(LOGLISTENER_ACTIVE_PROPERTY);
//    if (active != null && ("TRUE".equalsIgnoreCase(active) || "YES".equalsIgnoreCase(active))) {
//      
//      ConsoleOutputLogListener consoleLog;
//      String color = context.getProperty(LOGLISTENER_COLOR_ENABLED);
//      String suppStr = context.getProperty(LOGLISTENER_SUPPRESS_OSGI_EVENTS);
//      boolean suppress = suppStr != null && ("TRUE".equalsIgnoreCase(suppStr) || "YES".equalsIgnoreCase(suppStr));
//      LogEntryFormater formater = color != null && ("TRUE".equalsIgnoreCase(color) || "YES".equalsIgnoreCase(color)) ?
//                                    new ECMA48LogEntryFormater() : new DefaultLogEntryFormater();
//      
//      consoleLog = new ConsoleOutputLogListener(formater, suppress);
//      
//      ServiceReference ref = context.getServiceReference(LogReaderService.class.getName());
//      if (ref != null) {
//        LogReaderService reader = (LogReaderService) context.getService(ref);
//        reader.addLogListener(consoleLog);
//      }
//    }

    // get config file
    File configPath = getConfigPath(context);
    configFile = new File(configPath.getAbsolutePath() + File.separator + CONFIG_NAME);
    ensureFile(configFile);

    // load properties
    try {
      systemProperties.load(new FileInputStream(configFile));
      Logger.debug("Loaded config properties from " + configFile.getAbsolutePath());
    } catch (IOException e) {
      Logger.warn("Failed to load config from " + configFile.getAbsolutePath());
    }
    
    // register config service
    ConfigurationService confService = new ConfigurationServiceImpl(context, systemProperties, defaultProperties);
    context.registerService(ConfigurationService.class.getName(), confService, null);
    context.addServiceListener((ConfigurationServiceImpl)confService);

    // register config factory
    ConfigurationFactory confFactory = new ConfigurationFactory(systemProperties, defaultProperties, (ConfigurationServiceImpl)confService);
    confFactoryReg = context.registerService(Configuration.class.getName(), confFactory, null);
    
    // register config commands
    ConfigCommands commandImpl = new ConfigCommands((ConfigurationServiceImpl)confService);
    Dictionary<String, Object> commands = new Hashtable<String, Object>();
    commands.put("osgi.command.scope", "config");
    commands.put("osgi.command.function", ConfigCommands.commands);
    context.registerService(ConfigCommands.class.getName(), commandImpl, commands);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    // TODO: save config properties ?  ...Nope :)
  }

  private File getConfigPath(BundleContext bc) {

    // try to get config dir from framework property
    String path = bc.getProperty(CONFIG_PATH_PROPERTY);
    if (path != null) {
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) {
        Logger.debug("Using config directory specified in framework property: " + dir.getAbsolutePath());
        return dir;
      } else {
        Logger.warn("Config directory specified in framework property does not exist: " + dir.getAbsolutePath());
      }
    }

    // try to get config dir from system property
    path = System.getProperty(CONFIG_PATH_PROPERTY);
    if (path != null) {
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) {
        Logger.debug("Using config directory specified in system property: " + dir.getAbsolutePath());
        return dir;
      } else {
        Logger.warn("Config directory specified in framework property does not exist: " + dir.getAbsolutePath());
      }
    }

    // try to get default config dir
    path = "conf";
    if (path != null) {
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) {
        Logger.debug("Using default config directory: " + dir.getAbsolutePath());
        return dir;
      } else {
        Logger.warn("Default config directory does not exist: " + dir.getAbsolutePath());
      }
    }

    File dir = new File(".");
    Logger.debug("Using working directory as config directory: " + dir.getAbsolutePath());
    return dir;
  }

  private void ensureFile(File file) throws ServiceException {
    if (!file.exists()) {
      Logger.warn("Config file does not exist, creating " + file.getAbsolutePath());
      try {
        file.createNewFile();
      } catch (IOException ex) {
        throw new ServiceException("Failed to create config file: " + file.getAbsolutePath(), ex);
      }
    }
  }
}
