package cv.lecturesight.cameraoperator.scripted.bridge;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ConfigurationBridge {
  
  private Properties props;
  
  public ConfigurationBridge(File file) throws ScriptBridgeException {
    try {
      props = new Properties();
      props.load(new FileInputStream(file));
    } catch (Exception e) {
      throw new ScriptBridgeException("Failed to read configuration from " + file.getName(), e);
    }
  }
  
  public boolean has(String key) {
    return props.containsKey(key);
  }
  
  public String get(String key) {
    if (props.containsKey(key)) {
      return props.getProperty(key);
    } else {
      return "";
    }
  }
  
  public void set(String key, String value) {
    props.setProperty(key, value);
  }
}
