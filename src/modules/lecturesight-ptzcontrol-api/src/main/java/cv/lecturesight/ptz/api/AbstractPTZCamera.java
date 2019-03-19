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
package cv.lecturesight.ptz.api;

import java.util.Properties;

public abstract class AbstractPTZCamera implements PTZCamera {

  protected PTZCameraProfile profile = null;

  protected void setCameraProfile(Properties props) {
    profile = new PTZCameraProfile(props);
  }

  protected void setCameraProfile(PTZCameraProfile profile) {
    this.profile = profile;
  }

  @Override
  public PTZCameraProfile getProfile() {
    if (profile != null) {
      return profile;
    } else {
      throw new RuntimeException("Underlying implementation did not provide a camera profile.");
    }
  }
}
