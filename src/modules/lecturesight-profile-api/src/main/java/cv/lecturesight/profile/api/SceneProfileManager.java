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

import java.util.List;

public interface SceneProfileManager {

  /** Sets the active profile. The active profile is the profile the system uses
   * in operation. When the active profile has been changed, this method should
   * be called again in order for the changes to take effect.
   *
   * @param profile
   */
  void setActiveProfile(SceneProfile profile);

  /** Returns the active profile.
   *
   * @return profile that is currently active
   */
  SceneProfile getActiveProfile();

  /** Returns a list of all installed profiles.
   *
   * @return list of installed profiles
   */
  List<SceneProfile> getProfiles();

  /** Stores a profile.
   *
   * @param profile to install
   */
  void putProfile(SceneProfile profile);

  /** Removes a profile from system.
   *
   * @param profile to remove
   */
  void removeProfile(SceneProfile profile);

  /** Save a profile to the file it was loaded from.
   *
   * @param profile to save
   */
  void saveProfile(SceneProfile profile);

  /** Register a <code>SceneProfileListener</code> that is notified when the
   * active profile changes.
   *
   * @param listener to register
   */
  void registerProfileListener(SceneProfileListener listener);

  /** Unregister a <code>SceneProfileListener</code>. If <code>listener</code>
   * was not previously registered, this method fails quietly.
   *
   * @param listener
   */
  void unregisterProfileListener(SceneProfileListener listener);

}
