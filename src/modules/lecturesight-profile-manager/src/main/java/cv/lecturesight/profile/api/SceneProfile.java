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
