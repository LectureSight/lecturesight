package cv.lecturesight.framesource.impl;

import java.util.HashMap;
import java.util.Map;

public class FrameSourceDescriptor {

  private String type;
  private String locator;
  private Map<String,String> config = new HashMap<String,String>();

  public FrameSourceDescriptor(String fsdStr) throws FrameSourceDescriptorParsingException {
    parseSourceDescriptorString(fsdStr);
  }

  private void parseSourceDescriptorString(String input) throws FrameSourceDescriptorParsingException {

    String[] parts = input.split("\\:\\/\\/", 2);
    if (parts.length == 2) {
      type = parts[0];

      // find config string
      int cfgStart = parts[1].indexOf("[");
      int cfgEnd = parts[1].indexOf("]");
      if (cfgStart != -1) {
        locator = parts[1].substring(0, cfgStart);
        if (cfgEnd != -1) {
          String confStr = parts[1].substring(cfgStart+1, cfgEnd);
          parseConfigString(confStr);
        } else {
          throw new FrameSourceDescriptorParsingException("Frame source configuration not closed");
        }
      } else {
        locator = parts[1];
      }

    } else {
      throw new FrameSourceDescriptorParsingException("Malformed frame source descriptor");
    }
  }

  public void parseConfigString(String confStr) {
    String[] parts = confStr.split("\\;");
    for (String prop : parts) {
      int eq = prop.indexOf("=");
      if (eq != -1) {
        config.put(prop.substring(0,eq), prop.substring(eq+1));
      } else {
        config.put(prop, "true");
      }
    }
  }

  public String getType() {
    return type;
  }

  public String getLocator() {
    return locator;
  }

  public Map<String,String> getConfiguration() {
    return config;
  }
}
