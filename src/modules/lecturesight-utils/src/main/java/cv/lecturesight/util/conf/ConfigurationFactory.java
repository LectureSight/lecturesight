package cv.lecturesight.util.conf;

import cv.lecturesight.util.Log;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class ConfigurationFactory implements ServiceFactory {

  final static String DEFAULT_CONF_PATH = "conf";               // bundle resource dir
  final static String DEFAULT_CONF_NAME = "default.properties"; // default config file name
  private static Log log = new Log("ConfigurationFactory");     // Logger
  static Properties configProps;                               // properties system config
  static Properties defaultProps;                              // default properties

  public ConfigurationFactory(Properties props, Properties defaults) {
    configProps = props;
    defaultProps = defaults;
  }

  @Override
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    String bundleName = bundle.getSymbolicName();     // get caller bundles name

    // try to find default config in caller bundle
    Enumeration res = bundle.findEntries(DEFAULT_CONF_PATH, DEFAULT_CONF_NAME, false);
    if (res != null && res.hasMoreElements()) {
      log.debug("Found default config in bundle " + bundleName);
      try {
        URL configUrl = (URL) res.nextElement();        // get URL of config file
        Properties bundleDefaults = new Properties();
        bundleDefaults.load(configUrl.openStream());    // load defaults from file
        
        // add bundle defaults to default properties
        for (Iterator<String> keys = bundleDefaults.stringPropertyNames().iterator(); keys.hasNext();) {
          String key = keys.next();
          defaultProps.setProperty(key, bundleDefaults.getProperty(key));
        }

      } catch (IOException e) {
        log.warn("Failed to load bundles default configuration: " + e.getMessage());
      }
    } else {
      log.debug("No default configuration found in bundle " + bundle.getSymbolicName());
    }

    return new ConfigurationImpl(bundleName, configProps);  // create config object
  }

  @Override
  public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
    ConfigurationImpl config = (ConfigurationImpl)service;
    for (Iterator<String> keys = defaultProps.stringPropertyNames().iterator(); keys.hasNext();) {
      String key = keys.next();
      if (key.startsWith(config.bundleName)) {
        defaultProps.remove(key);
      }
    }
  }
}
