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
  public String get(String key) throws IllegalArgumentException {
    key = ensureBundleName(key);
    String value = props.getProperty(key);
    if (value != null) {
      return value;
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
