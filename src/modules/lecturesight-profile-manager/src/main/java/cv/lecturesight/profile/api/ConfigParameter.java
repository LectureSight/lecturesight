package cv.lecturesight.profile.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="config")
public class ConfigParameter {

  @XmlAttribute(name="key")
  private String key;
  
  @XmlAttribute(name="value")
  private String value;
  
  public ConfigParameter(String key) {
    this.key = key;
    this.value = "";
  }
  
  public ConfigParameter(String key, String value) {
    this.key = key;
    this.value = value;
  }
  
  public String getKey() {
    return key;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public String getValue() {
    return value;
  }
}
