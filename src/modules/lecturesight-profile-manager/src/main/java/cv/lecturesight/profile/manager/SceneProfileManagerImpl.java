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
import cv.lecturesight.profile.api.SceneProfileListener;
import cv.lecturesight.profile.api.SceneProfileManager;
import cv.lecturesight.profile.api.SceneProfileSerializer;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.ConfigurationListener;
import cv.lecturesight.util.conf.ConfigurationService;
import java.io.*;
import java.util.*;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;

public class SceneProfileManagerImpl implements SceneProfileManager, ConfigurationListener, ArtifactInstaller {

  static final String PROPKEY_PROFILE = "cv.lecturesight.profile.active";
  static final String FILEEXT_PROFILE = ".scn";
  private Log log = new Log("Scene Profile Manager");
  @Reference
  private ConfigurationService configS;
  private Properties config;
  private ProfileStore profiles = new ProfileStore();
  private Set<SceneProfileListener> subscribers = new HashSet<SceneProfileListener>();
  private SceneProfile defaultProfile = new SceneProfile();
  private SceneProfile activeProfile = defaultProfile;
  private String activeProfilePath = null;
  private String configuredActiveProfile = "";

  protected void activate(ComponentContext cc) throws Exception {
    config = configS.getSystemDefaults();
    configuredActiveProfile = config.getProperty(PROPKEY_PROFILE);
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated");
  }

  @Override
  public SceneProfile getActiveProfile() {
    return activeProfile;
  }

  @Override
  public void setActiveProfile(SceneProfile profile) {
    activeProfilePath = profiles.getFilename(profile);
    activeProfile = profile;
    notifySubscribers(profile);
  }

  @Override
  public SceneProfile[] getProfiles() {
    return profiles.getAll();
  }
  
  @Override
  public void putProfile(SceneProfile profile) {
    profiles.put(profile);
    if (profile.name.equals(activeProfile.name)) {
      activeProfile = profile;
      notifySubscribers(profile);
    }
  }
  
  @Override
  public void removeProfile(SceneProfile profile) {
    String filename = profiles.getFilename(profile);
    if (filename == null) {
      profiles.remove(profile);
      if (profile.name.equals(activeProfile.name)) {
        activeProfile = defaultProfile;
        notifySubscribers(activeProfile);
      }
    } else {
      throw new RuntimeException("Cannot remove profile " + profile.name + " because it was installed from file. Delete " + filename);
    }
  }

  @Override
  public void registerProfileListener(SceneProfileListener listener) {
    subscribers.add(listener);
  }

  @Override
  public void unregisterProfileListener(SceneProfileListener listener) {
    subscribers.remove(listener);
  }

  private void notifySubscribers(SceneProfile profile) {
    for (SceneProfileListener listener : subscribers) {
      listener.profileActivated(profile);
    }
  }

  /* ArtifactInstaller methods */
  @Override
  public void install(File file) throws Exception {
    SceneProfile profile = SceneProfileSerializer.deserialize(new FileInputStream(file));
    profiles.putWithFilename(file.getAbsolutePath(), profile);
    log.info("Installed scene profile " + profile.name + " from " + file.getName());
  }

  @Override
  public void update(File file) throws Exception {
    SceneProfile profile = SceneProfileSerializer.deserialize(new FileInputStream(file));
    
    // check if updated file contains the active profile
    if (file.getAbsolutePath().equals(activeProfilePath)) {
      profiles.putWithFilename(file.getAbsolutePath(), profile);
      activeProfile = profile;
      notifySubscribers(profile);
    } else {
      profiles.putWithFilename(file.getAbsolutePath(), profile);
    }
    
    log.info("Updated scene profile " + profile.name + " from " + file.getName());
  }

  @Override
  public void uninstall(File file) throws Exception {
    String filename = file.getAbsolutePath();
    SceneProfile profile = SceneProfileSerializer.deserialize(new FileInputStream(file));
    if (profiles.hasFilename(filename)) {
      
      // check if deleted file contains the active profile
      if (activeProfilePath.equals(file.getAbsolutePath())) {
        profiles.removeByFilename(filename);
        activeProfile = defaultProfile;
        notifySubscribers(activeProfile);
      } else {
        profiles.removeByFilename(filename);
      }
      log.info("Removed scene profile " + filename);
    }
  }

  @Override
  public boolean canHandle(File file) {
    return file.isFile() && file.getName().toLowerCase().endsWith(FILEEXT_PROFILE);
  }

  @Override
  public void configurationChanged() {
    String s = configS.getSystemConfiguration().getProperty(PROPKEY_PROFILE);
    if (!configuredActiveProfile.equals(s)) {
      configuredActiveProfile = s;
      if (profiles.hasProfile(configuredActiveProfile)) {
        this.setActiveProfile(profiles.getByName(configuredActiveProfile));
      }
    }
  }

}
