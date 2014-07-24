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
package cv.lecturesight.profile.manager;

import cv.lecturesight.profile.api.SceneProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/** Utility class that stores SceneProfiles.
 *
 * @author wulff
 */
public class ProfileStore {
  
  // mapping filename --> SceneProfile
  private HashMap<String, SceneProfile> fileMap = new HashMap<String, SceneProfile>();
  
  // List of SceneProfiles
  private LinkedList<SceneProfile> profiles = new LinkedList<SceneProfile>();
  
  /** Returns true if this store contains a profile with the given name.
   * 
   * @param name of the profile in question
   * @return true if profile was found
   */
  boolean hasProfile(String name) {
    for (SceneProfile p : profiles) {
      if (p.name.equals(name)) {
        return true;
      }
    }
    return false;
  }
  
  boolean hasProfile(SceneProfile profile)  {
    return hasProfile(profile.name);
  }
  
  boolean hasFilename(String filename) {
    return fileMap.containsKey(filename);
  }

  String getFilename(SceneProfile profile) {
    for (String path : fileMap.keySet()) {
      SceneProfile p = fileMap.get(path);
      if (profile.name.equals(p.name)) {
        return path;
      }
    }
    return null;
  }
  
  SceneProfile getByName(String name) {
    for (SceneProfile p : profiles) {
      if (p.name.equals(name)) {
        return p.clone();
      }
    }
    return null;
  }
  
  SceneProfile getByFilename(String filename) {  
    if (fileMap.containsKey(filename)) {
      return fileMap.get(filename).clone();
    } else {
      return null;
    }
  }
  
  void put(SceneProfile profile) {
    if (this.hasProfile(profile)) {
      this.remove(profile);
    }
    profiles.add(profile);
    for (String filename : fileMap.keySet()) {
      if (profile.equals(fileMap.get(filename))) {
        removeByFilename(filename);
        putWithFilename(filename, profile);
      }
    }
  }

  void putWithFilename(String filename, SceneProfile profile) {
    if (this.hasProfile(profile)) {
      this.remove(profile);
    }
    fileMap.put(filename, profile);
    profiles.add(profile);
  }
  
  void remove(SceneProfile profile) {
    profiles.remove(profile);
    for (String filename : fileMap.keySet()) {
      if (profile.name.equals(fileMap.get(filename).name)) {
        removeByFilename(filename);
      }
    }
  }
    
  void removeByFilename(String filename) {
    if (fileMap.containsKey(filename)) {
      SceneProfile p = fileMap.get(filename);
      profiles.remove(p);
      fileMap.remove(filename);
    }
  }
  
  List<SceneProfile> getAll() {
    ArrayList<SceneProfile> list = new ArrayList<SceneProfile>();
    for (SceneProfile p : profiles) {
      list.add(p.clone());
    }
    return list;
  }
}
