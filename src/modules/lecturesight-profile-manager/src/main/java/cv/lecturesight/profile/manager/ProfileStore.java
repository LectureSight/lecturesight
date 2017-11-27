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
import java.util.List;

/** Utility class that stores SceneProfiles.
 *
 * @author wulff
 */
public class ProfileStore {

  // List of SceneProfiles and (optionally) a path to an artifact origin
  private HashMap<SceneProfile, String> pl = new HashMap<SceneProfile, String>();

  /** Returns true if this store contains a profile with the given name.
   *
   * @param name of the profile in question
   * @return true if profile was found
   */
  synchronized boolean hasProfile(String name) {
    for (SceneProfile p : pl.keySet()) {
      if (p.name.equals(name)) {
        return true;
      }
    }
    return false;
  }

  synchronized boolean hasProfile(SceneProfile profile)  {
    for (SceneProfile p : pl.keySet()) {
      if (p.equals(profile)) {
        return true;
      }
    }
    return false;
  }

  synchronized boolean hasFilename(String filename) {
    return pl.values().contains(filename);
  }

  synchronized String getFilename(SceneProfile profile) {
    return pl.get(profile);
  }

  synchronized SceneProfile getByName(String name) {
    for (SceneProfile p : pl.keySet()) {
      if (p.name.equals(name)) {
        return p;
      }
    }
    return null;
  }

  synchronized SceneProfile getByFilename(String filename) {
    for (SceneProfile p : pl.keySet()) {
      if (pl.get(p).equals(filename)) {
        return p;
      }
    }
    return null;
  }

  synchronized void put(SceneProfile profile) {
    pl.put(profile, null);
  }

  synchronized void putWithFilename(String filename, SceneProfile profile) {
    pl.put(profile, filename);
  }

  synchronized void remove(SceneProfile profile) {
    pl.remove(profile);
  }

  synchronized void removeByFilename(String filename) {
    for (SceneProfile p : pl.keySet()) {
      if (pl.get(p).equals(filename)) {
        pl.remove(p);
      }
    }
  }

  synchronized List<SceneProfile> getAll() {
    ArrayList<SceneProfile> list = new ArrayList<SceneProfile>();
    for (SceneProfile p : pl.keySet()) {
      list.add(p);
    }
    return list;
  }
}
