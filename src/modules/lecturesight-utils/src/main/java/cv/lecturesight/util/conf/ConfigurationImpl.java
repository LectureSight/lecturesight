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

import java.util.Properties;

public class ConfigurationImpl implements Configuration {

  String bundleName;
  Properties props;

  public ConfigurationImpl(String bundleName, Properties props) {
    this.bundleName = bundleName;
    this.props = props;
  }

  @Override
  public void set(String key, String value) {
    key = ensureBundleName(key);
    props.setProperty(key, value);
  }

  @Override
  public String get(String key) throws IllegalArgumentException {
    key = ensureBundleName(key);
    String value = props.getProperty(key);
    if (value != null) {
      return value.trim();
    } else {
      throw new IllegalArgumentException("Unknown configuration key: " + key);
    }
  }

  @Override
  public boolean getBoolean(String key) throws IllegalArgumentException {
    String val = get(key);
    return ("true".equalsIgnoreCase(val)
            || "yes".equalsIgnoreCase(val)
            || "on".equals(val));
  }

  @Override
  public byte getByte(String key) throws IllegalArgumentException {
    return Byte.parseByte(get(key));
  }

  @Override
  public short getShort(String key) throws IllegalArgumentException {
    return Short.parseShort(get(key));
  }

  @Override
  public int getInt(String key) throws IllegalArgumentException {
    return Integer.parseInt(get(key));
  }

  @Override
  public long getLong(String key) throws IllegalArgumentException {
    return Long.parseLong(get(key));
  }

  @Override
  public float getFloat(String key) throws IllegalArgumentException {
    return Float.parseFloat(get(key));
  }

  @Override
  public double getDouble(String key) throws IllegalArgumentException {
    return Double.parseDouble(get(key));
  }

  public String[] getList(String key) {
    String value = get(key);
    String[] list = value.split(",");
    return list;
  }

  private String ensureBundleName(String name) {
    if (name.startsWith(bundleName)) {
      return name;
    } else {
      return bundleName + "." + name;
    }
  }
}
