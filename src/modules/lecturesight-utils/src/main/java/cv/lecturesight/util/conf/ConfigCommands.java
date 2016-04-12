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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

public class ConfigCommands {

  public static final String[] commands = {"show", "set", "load", "save", "defaults"};
  ConfigurationServiceImpl config;

  public ConfigCommands(ConfigurationServiceImpl config) {
    this.config = config;
  }

  public void show(String[] args) {
    String filter = getFilterString(args);
    Map<String, String> sysProps = getProperties(config.config, filter);
    displayProperties(sysProps);
  }

  public void defaults(String[] args) {
    String filter = getFilterString(args);
    Map<String, String> defProps = getProperties(config.defaults, filter);
    displayProperties(defProps);
  }

  public void set(String[] args) {
    if (args.length == 2) {
      String key = args[0];
      String val = args[1];
      try {
        config.config.setProperty(key, val);
        config.notifyListeners();
      } catch (Exception e) {
        println("Unable to set property " + key + " to " + val);
      }
    } else {
      println("Usage: set <key> <value>");
    }
  }

  public void load(String[] args) {
    if (args.length == 1) {
      String filename = args[0];
      try {
        File file = new File(filename);
        filename = file.getAbsolutePath();
        config.loadSystemConfiguration(new FileInputStream(file));
      } catch (Exception e) {
        println("Unable to load configuration from " + filename);
        String msg = e.getMessage();
        if (msg != null) {
          println(msg);
        }
      }
    } else {
      println("Usage: load <filename>");
    }
  }

  public void save(String[] args) {
    if (args.length == 1) {
      String filename = args[0];
      try {
        config.saveSystemConfiguration(new FileOutputStream(new File(filename)));
      } catch (Exception e) {
        println("Unable to save configuration." + e.getMessage());
      }
    } else {
      println("Usage: save <filename>");
    }
  }

  private Map<String, String> getProperties(Properties props, String prefix) {
    boolean asterisk = prefix.trim().equals("*");
    HashMap<String, String> out = new HashMap<String, String>();
    for (Iterator<String> keys = props.stringPropertyNames().iterator(); keys.hasNext();) {
      String key = keys.next();
      if (asterisk || key.startsWith(prefix)) {
        out.put(key, props.getProperty(key));
      }
    }
    return out;
  }

  private String getFilterString(String[] args) {
    String filter = "*";
    if (args.length > 0) {
      filter = args[0].trim();
      if (!filter.equals("*") && filter.endsWith("*")) {
        filter = filter.substring(1, filter.length() - 1);
      }
    }
    return filter;
  }

  private void displayProperties(Map<String, String> props) {
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    SortedSet<String> keys = new TreeSet<String>();
    keys.addAll(props.keySet());
    for (Iterator<String> it = keys.iterator(); it.hasNext();) {
      String key = it.next();
      String val = props.get(key);
      sb.append(key);
      sb.append(" = ");
      sb.append(val);
      sb.append("\n");
    }
    sb.append("\n");
    System.out.println(sb.toString());  // PrintWriter to System.out wouldn't hurt
  }

  private void println(String msg) {
    System.out.println(msg);
  }
}
