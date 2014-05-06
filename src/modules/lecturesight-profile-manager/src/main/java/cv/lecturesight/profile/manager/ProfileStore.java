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
import java.util.HashMap;
import java.util.LinkedList;

/** Utility class that stores SceneProfiles.
 *
 * @author wulff
 */
public class ProfileStore {

  private HashMap<String, SceneProfile> fileMap = new HashMap<String, SceneProfile>();
  private LinkedList<SceneProfile> profiles = new LinkedList<SceneProfile>();
  
  boolean hasProfile(String name) {
    for (SceneProfile p : profiles) {
      if (p.name.equals(name)) {
        return true;
      }
    }
    return false;
  }
  
  boolean hasProfile(SceneProfile profile)  {
    return profiles.contains(profile);
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
        return p;
      }
    }
    return null;
  }
  
  SceneProfile getByFilename(String filename) {  
    if (fileMap.containsKey(filename)) {
      return fileMap.get(filename);
    } else {
      return null;
    }
  }
  
  void put(SceneProfile profile) {
    profiles.add(profile);
  }

  void putWithFilename(String filename, SceneProfile profile) {
    profiles.add(profile);
    fileMap.put(filename, profile);
  }
  
  void remove(SceneProfile profile) {
    profiles.remove(profile);
  }
    
  void removeByFilename(String filename) {
    if (fileMap.containsKey(filename)) {
      SceneProfile p = fileMap.get(filename);
      profiles.remove(p);
      fileMap.remove(filename);
    }
  }
  
  SceneProfile[] getAll() {
    return (SceneProfile[])profiles.toArray();
  }

  /**
  private SceneProfile deepCopy(SceneProfile in) {
    
  }**/
}
