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
  static Properties configProps;                                // systems config properties
  static Properties defaultProps;                               // systems default properties
  ConfigurationServiceImpl configService;

  public ConfigurationFactory(Properties props, Properties defaults, ConfigurationServiceImpl cs) {
    configProps = props;
    defaultProps = defaults;
    configService = cs;
  }

  @Override
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    String bundleName = bundle.getSymbolicName();     // get caller bundles name

    // try to find default config in caller bundle
    Enumeration res = bundle.findEntries(DEFAULT_CONF_PATH, DEFAULT_CONF_NAME, false);
    if (res != null && res.hasMoreElements()) {
      log.debug("Found default configuration in bundle " + bundleName);
      try {
        URL configUrl = (URL) res.nextElement();        // get URL of config file
        Properties bundleDefaults = new Properties();
        bundleDefaults.load(configUrl.openStream());    // load defaults from file
        
        // add bundle defaults to default properties
        for (Iterator<String> keys = bundleDefaults.stringPropertyNames().iterator(); keys.hasNext();) {
          String key = keys.next();
          log.debug("Setting property " + key + " = " + bundleDefaults.getProperty(key) + " of " + bundleName);
          defaultProps.setProperty(key, bundleDefaults.getProperty(key));
        }
        configService.notifyListeners();

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
    configService.notifyListeners();
  }
}
