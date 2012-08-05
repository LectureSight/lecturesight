package cv.lecturesight.profile.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="area")
class Area {
  
  @XmlAttribute(name="id")
  public String id;
  
  @XmlAttribute(name="pos-x")
  public int x;
  
  @XmlAttribute(name="pos-y")
  public int y;
  
  @XmlAttribute(name="width")
  public int width;
  
  @XmlAttribute(name="height")
  public int heigth;
}
