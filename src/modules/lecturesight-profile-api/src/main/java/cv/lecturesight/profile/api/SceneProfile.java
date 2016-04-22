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
import java.util.LinkedList;
import java.util.List;

public class SceneProfile {

  public String name;   // name of the profile, at the same time unique ID

  public String description;  // human-readable description of profile

  public int width;    // width of the scene in pixels

  public int height;   // height of the scene in pixels

  public LinkedList<Zone> zones = new LinkedList<Zone>();  // profile's zones

  public SceneProfile() {
    this.name = "empty";
    this.description = "";
    this.width = 0;
    this.height = 0;
  }

  public SceneProfile(String name, String description, int width, int height) {
    this.name = name;
    this.description = description;
    this.width = width;
    this.height = height;
  }

  /** Returns true if this profile contains a Zone that is equal to <code>zone</code>.
   * 
   * @param zone
   * @return true if profile contains zone, false otherwise
   */
  public synchronized boolean containsZone(Zone zone) {
    for (Zone z : zones) {
      if (z.equals(zone)) {
        return true;
      }
    }
    return false;
  }

  /** Puts <code>zone</code> into this profile.
   * @param zone 
   */
  public synchronized void putZone(Zone zone) {
    zones.add(zone);
  }

  /** Removes <code>zone</code> from the collection of Zones.
   * 
   * @param zone Zone to be removed
   */
  public synchronized void removeZone(Zone zone) {
    if (zones.contains(zone)) {
      zones.remove(zone);
    }
  }
  
  /** Returns a deep copy of the first zone found having the specified name or 
   * null if such a zone can not be found.
   * 
   * @param name of the Zone to be retrieved
   * @return Zone or null, if Zone with specified name is not found 
   */
  public synchronized Zone getZoneByName(String name) {
    for (Zone z : zones) {
      if (z.name.equals(name)) {
        return z.clone();
      }
    }
    return null;
  }

  /** Returns an List of deep copies of all Zones in this profile.
   * 
   * @return List of all Zones in this profile
   */
  public synchronized List<Zone> getAllZones() {
    ArrayList<Zone> list = new ArrayList<Zone>();
    for (Zone z : zones) {
      list.add(z.clone());
    }
    return list;
  }

  /** Returns a List of deep copies of all Zones of Type IGNORE.
   * 
   * @return List of IGNORE Zones
   */
  public synchronized List<Zone> getIgnoreZones() {
    ArrayList<Zone> list = new ArrayList<Zone>();
    for (Zone z : zones) {
      if (z.type == Zone.Type.IGNORE) {
        list.add(z.clone());
      }
    }
    return list;
  }

  /** Returns a List of deep copies of all Zones of Type TRACK.
   * 
   * @return List of TRACK Zones.
   */
  public synchronized List<Zone> getTrackingZones() {
    ArrayList<Zone> list = new ArrayList<Zone>();
    for (Zone z : zones) {
      if (z.type == Zone.Type.TRACK) {
        list.add(z.clone());
      }
    }
    return list;
  }

  /** Returns a List of deep copies of all Zones of Type TRIGGER.
   * 
   * @return List of TRIGGER Zones
   */
  public synchronized List<Zone> getTriggerZones() {
    ArrayList<Zone> list = new ArrayList<Zone>();
    for (Zone z : zones) {
      if (z.type == Zone.Type.TRIGGER) {
        list.add(z.clone());
      }
    }
    return list;
  }

  @Override
  public synchronized SceneProfile clone() {
    SceneProfile clone = new SceneProfile(this.name, this.description, this.width, this.height);
    for (Zone zone : this.zones) {
      clone.zones.add(zone.clone());
    }
    return clone;
  }
  
  @Override
  public synchronized boolean equals(Object other) {
    SceneProfile otherProfile;

    // test if other is even a SceneProfile, cast to if so
//    if (other instanceof SceneProfile) {
//      otherProfile = (SceneProfile) other;
//    } else {
//      return false;
//    }

    // test if both have same number of zones
//    if (this.zones.size() != otherProfile.zones.size()) {
//      return false;
//    }

    // test if zones in both profiles are the same
//    for (Zone zone : otherProfile.zones) {
//      if (!this.containsZone(zone)) {
//        return false;
//      }
//    }

    return this.name.equals(((SceneProfile)other).name);
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
    return hash;
  }
}
