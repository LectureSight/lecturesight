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
import cv.lecturesight.util.conf.Configuration;
import java.io.*;
import java.util.*;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name="lecturesight.profile.manager", immediate=true)
@Service()
public class SceneProfileManagerImpl implements SceneProfileManager, ArtifactInstaller {

  static final String PROPKEY_PROFILE = "active.profile";
  static final String FILEEXT_PROFILE = ".scn";
  private Log log = new Log("Scene Profile Manager");
  @Reference
  private Configuration config;
  private ProfileStore profiles = new ProfileStore();
  private SceneProfile defaultProfile, activeProfile;
  private Set<SceneProfileListener> subscribers = new HashSet<SceneProfileListener>();
  private String configuredProfile;

  protected void activate(ComponentContext cc) throws Exception {
    // create system default profile
    defaultProfile = new SceneProfile("default", "System default profile");
    defaultProfile.name = "default";
    profiles.put(defaultProfile);
    
    // setting defaultProfile as default profile 
    // as long as profile artifacts have not been loaded
    activeProfile = defaultProfile;   
    
    // get name of configured profile
    configuredProfile = config.get(PROPKEY_PROFILE);
    log.info("Activated. Configured scene profile is: " + configuredProfile);
  }

  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated.");
  }

  @Override
  public SceneProfile getActiveProfile() {
    return activeProfile.clone();
  }

  @Override
  public void setActiveProfile(SceneProfile profile) {
    activeProfile = profile.clone();
    notifySubscribersActivated(profile);
    log.info("Activated scene profile: " + activeProfile.name);
  }

  @Override
  public List<SceneProfile> getProfiles() {
    return profiles.getAll();
  }
  
  @Override
  public void putProfile(SceneProfile profile) {
    if (profiles.hasProfile(profile)) {
      profiles.put(profile);
      notifySubscribersUpdated(profile);
    } else {
      profiles.put(profile);
      notifySubscribersInstalled(profile);
    }
    if (profile.equals(activeProfile)) {
      setActiveProfile(profile);
    }
  }
  
  @Override
  public void removeProfile(SceneProfile profile) {
    String filename = profiles.getFilename(profile);
    if (filename == null) {
      profiles.remove(profile);
      notifySubscribersRemoved(profile);
      if (profile.equals(activeProfile)) {
        setActiveProfile(defaultProfile);
      }
    } else {
      throw new RuntimeException("Cannot remove profile " + profile.name + " because it was installed from file. Delete " + filename);
    }
  }

  @Override
  public void saveProfile(SceneProfile profile) {
    // the system default profile cannot be saved to file
    if (profile.equals(defaultProfile)) {
      throw new IllegalArgumentException("The profile cannot be saved since it is the default profile.");
    }
    
    // save the profile if it was installed from file
    String filename = profiles.getFilename(profile);
    if (filename != null) {
      try {
        File outfile = new File(filename);
        log.info("Writing scene profile \"" + profile.name + "\" to " + outfile.getAbsolutePath());
        OutputStream out = new FileOutputStream(outfile);
        SceneProfileSerializer.serialize(profile, out);
        out.close();
      } catch (Exception e) {
        throw new RuntimeException("Failed to save profile! ", e);
      }
    } else {
      throw new IllegalArgumentException("The profile cannot be saved since it has no artifact origin.");
    }
  }
  
  
 // <editor-fold defaultstate="collapsed" desc="Event Notification Stuff">   
  @Override
  public void registerProfileListener(SceneProfileListener listener) {
    subscribers.add(listener);
  }

  @Override
  public void unregisterProfileListener(SceneProfileListener listener) {
    subscribers.remove(listener);
  }

  private void notifySubscribersActivated(SceneProfile profile) {
    for (SceneProfileListener listener : subscribers) {
      listener.profileActivated(profile);
    }
  }  
  
  private void notifySubscribersInstalled(SceneProfile profile) {
    for (SceneProfileListener listener : subscribers) {
      listener.profileInstalled(profile);
    }
  }
  
  private void notifySubscribersUpdated(SceneProfile profile) {
    for (SceneProfileListener listener : subscribers) {
      listener.profileUpdated(profile);
    }
  }
  
  private void notifySubscribersRemoved(SceneProfile profile) {
    for (SceneProfileListener listener : subscribers) {
      listener.profileRemoved(profile);
    }
  }
 // </editor-fold>
  
  
 // <editor-fold defaultstate="collapsed" desc="ArtifactInstaller Methods"> 
  @Override
  public boolean canHandle(File file) {
    return file.isFile() && file.getName().toLowerCase().endsWith(FILEEXT_PROFILE);
  }
  
  @Override
  public void install(File file) throws Exception {
    String filename = file.getAbsolutePath();
    SceneProfile profile = SceneProfileSerializer.deserialize(new FileInputStream(file));
    profiles.putWithFilename(filename, profile);
    log.info("Installed scene profile \"" + profile.name + "\" from " + filename);
    notifySubscribersInstalled(profile);
    
    // test if the installed artifact contains the active profile, activate it if so
    if (configuredProfile.equals(profile.name)) {
      setActiveProfile(profile);
    }
  }

  @Override
  public void update(File file) throws Exception {
    String filename = file.getAbsolutePath();
    SceneProfile profile = SceneProfileSerializer.deserialize(new FileInputStream(file));
    profiles.putWithFilename(filename, profile);
    log.info("Updated scene profile \"" + profile.name + "\" from " + filename);
    notifySubscribersUpdated(profile);
    
    // test if active profile was updated
    if (activeProfile.equals(profile)) {
      setActiveProfile(profile);
    }
  }

  @Override
  public void uninstall(File file) throws Exception {
    String filename = file.getAbsolutePath();
    
    if (profiles.hasFilename(filename)) {
      SceneProfile profile = profiles.getByFilename(filename);
      profiles.remove(profile);
      log.info("Removed scene profile " + filename);
      notifySubscribersRemoved(profile);
      
      // test if active profile was removed, activated default profile if so
      if (activeProfile.equals(profile)) {
        setActiveProfile(defaultProfile);
      }
    }
  }
// </editor-fold>

}
