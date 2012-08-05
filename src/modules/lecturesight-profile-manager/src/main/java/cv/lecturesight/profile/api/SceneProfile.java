package cv.lecturesight.profile.api;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "cv.lecturesight.profile")
public class SceneProfile {

  @XmlElement(name = "id")
  private String uid;
  
  @XmlElement(name = "name")
  private String name;
  
  @XmlElementWrapper(name = "ignoreAreas")
  @XmlElement(name = "area")
  private ArrayList<Area> ignoreAreas;
  
  @XmlElementWrapper(name = "importantAreas")
  @XmlElement(name = "area")
  private ArrayList<Area> importantAreas;
  
  @XmlElementWrapper(name = "triggerAreas")
  @XmlElement(name = "area")
  private ArrayList<Area> triggerAreas;
  
  @XmlElementWrapper(name = "override")
  @XmlElement(name = "config")
  private ArrayList<ConfigParameter> configuration;
  
  public SceneProfile() {
    this.uid = "default";
    this.name = "name";
  }
  
  public SceneProfile(String id, String name) {
    this.uid = id;
    this.name = name;
  }

  public String getId() {
    return uid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ArrayList<Area> getIgnoreAreas() {
    return ignoreAreas;
  }

  public ArrayList<Area> getImportantAreas() {
    return importantAreas;
  }

  public ArrayList<Area> getTriggerAreas() {
    return triggerAreas;
  }
  
  public List<ConfigParameter> getConfigOverride() {
    return configuration;
  }
}
