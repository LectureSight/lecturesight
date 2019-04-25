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

import static cv.lecturesight.profile.api.Zone.Type.PERSON;

import cv.lecturesight.profile.api.SceneProfile;
import cv.lecturesight.profile.api.SceneProfileListener;
import cv.lecturesight.profile.api.SceneProfileManager;
import cv.lecturesight.profile.api.SceneProfileSerializer;
import cv.lecturesight.profile.api.Zone;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.conf.ConfigurationService;

import lombok.Setter;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class SceneProfileManagerImpl implements SceneProfileManager, ArtifactInstaller {

  static final String PROPKEY_PROFILE = "active.profile";
  static final String FILEEXT_PROFILE = ".scn";
  @Setter
  private Configuration config;
  @Setter
  private ConfigurationService configService;
  private ProfileStore profiles = new ProfileStore();
  private SceneProfile defaultProfile;
  private SceneProfile activeProfile;
  private SceneProfile loadProfile;
  private Set<SceneProfileListener> subscribers = new HashSet<SceneProfileListener>();
  private String configuredProfile;
  private boolean active = true;

  protected void activate(ComponentContext cc) throws Exception {
    // make sure profile directory existis
    File profileDir = new File(System.getProperty("user.dir") + File.separator + "profiles");
    if (!profileDir.exists()) {
      Logger.info("Profile directory not existing. Attempting to create " + profileDir.getAbsolutePath());
      try {
        profileDir.mkdir();
      } catch (Exception e) {
        Logger.error("Failed to create profile directory. ", e);
      }
    }

    // create system default profile
    defaultProfile = new SceneProfile("default", "System default profile", 640, 360);
    defaultProfile.name = "default";
    profiles.put(defaultProfile);

    // get name of configured profile
    configuredProfile = config.get(PROPKEY_PROFILE);

    // load all the profiles
    File[] files = profileDir.listFiles();

    for (File file : files) {
      if (file.isFile() && canHandle(file)) {
        install(file);
      }
    }

    // setting defaultProfile as active profile
    // if the configured profile has not been specified or is not available
    if (activeProfile == null) {
      activeProfile = defaultProfile;
    }

    Logger.info("Activated. Configured scene profile is: " + activeProfile.name);
  }

  protected void deactivate(ComponentContext cc) {
    active = false;
    Logger.info("Deactivated.");
  }

  @Override
  public SceneProfile getActiveProfile() {
    return activeProfile.clone();
  }

  @Override
  public void setActiveProfile(SceneProfile profile) {
    activeProfile = profile.clone();
    notifySubscribersActivated(profile);
    Logger.info("Activated scene profile: " + activeProfile.name);

    // if profile has a person zone then adjust video analysis parameters accordingly
    Zone person = findPersonZone(activeProfile);
    if (person != null) {
      Logger.info("Adjusting video analysis parameters according to person area from profile.");
      double width = person.width;
      double height = person.height;
      double whole = width * height;
      double avg_weight = (3.0/4.0) * whole;

      Properties conf = configService.getSystemConfiguration();

      double minweight = (1.0/10) * avg_weight;
      minweight = minweight < 20 ? 20 : minweight;
      conf.setProperty("cv.lecturesight.videoanalysis.foreground.ccl.blobsize.min", Integer.toString((int)minweight));
      conf.setProperty("cv.lecturesight.videoanalysis.foreground.ccl.blobsize.max", Integer.toString((int)whole));

      double minheight = (1.0/10) * height;
      conf.setProperty("cv.lecturesight.regiontracker.height.min", Integer.toString((int)minheight));
      conf.setProperty("cv.lecturesight.regiontracker.height.max", Integer.toString((int)(height + 3 * minheight)));

      double minwidth = (1.0/10) * width;
      conf.setProperty("cv.lecturesight.regiontracker.width.min", Integer.toString((int)minwidth));
      conf.setProperty("cv.lecturesight.regiontracker.width.max", Integer.toString((int)(minwidth + 2 * minwidth)));

      conf.setProperty("cv.lecturesight.regiontracker.size.min", Integer.toString((int)((1.0/10) * whole)));
      conf.setProperty("cv.lecturesight.regiontracker.size.max", Integer.toString((int)whole));

      conf.setProperty("cv.lecturesight.objecttracker.simple.width.min", Integer.toString((int)((1.0/4) * width)));
      conf.setProperty("cv.lecturesight.objecttracker.simple.width.max", Integer.toString((int)(width + 2 * minwidth)));
      conf.setProperty("cv.lecturesight.objecttracker.simple.height.min", Integer.toString((int)((1.0/4) * height)));
      conf.setProperty("cv.lecturesight.objecttracker.simple.height.max", Integer.toString((int)(height + 3 * minheight)));

      conf.setProperty("cv.lecturesight.objecttracker.simple.template.width", Integer.toString((int)((1.0/4) * height)));
      conf.setProperty("cv.lecturesight.objecttracker.simple.template.height", Integer.toString((int)(height + 3 * minheight)));

      configService.notifyListeners();
    }
  }

  /** Returns the first occurrence of a Zone of Type PERSON or null if such a
   * zone is not in the profile.
   *
   * @param profile
   * @return Person Zone
   */
  private Zone findPersonZone(SceneProfile profile) {
    for (Zone z : profile.getAllZones()) {
      if (z.getType().equals(PERSON)) {
        return z;
      }
    }
    return null;
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
        Logger.info("Writing scene profile \"" + profile.name + "\" to " + outfile.getAbsolutePath());
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
    if (!active) return;

    String filename = file.getAbsolutePath();
    try {
      SceneProfile profile = SceneProfileSerializer.deserialize(new FileInputStream(file));
      profiles.putWithFilename(filename, profile);
      Logger.info("Installed scene profile \"" + profile.name + "\" from " + filename);
      notifySubscribersInstalled(profile);

      // test if the installed artifact contains the active profile, activate it if so
      if (configuredProfile.equals(profile.name)) {
        setActiveProfile(profile);
      }
    } catch (Exception e) {
      Logger.warn("Ignoring invalid scene profile in " + filename + ": " + e.getMessage());
    }
  }

  @Override
  public void update(File file) throws Exception {
    if (!active) return;

    String filename = file.getAbsolutePath();
    SceneProfile profile = SceneProfileSerializer.deserialize(new FileInputStream(file));
    profiles.putWithFilename(filename, profile);
    Logger.info("Updated scene profile \"" + profile.name + "\" from " + filename);
    notifySubscribersUpdated(profile);

    // test if active profile was updated
    if (activeProfile.equals(profile)) {
      setActiveProfile(profile);
    }
  }

  @Override
  public void uninstall(File file) throws Exception {
    if (!active) return;

    String filename = file.getAbsolutePath();

    if (profiles.hasFilename(filename)) {
      SceneProfile profile = profiles.getByFilename(filename);
      profiles.remove(profile);
      Logger.info("Removed scene profile " + filename);
      notifySubscribersRemoved(profile);

      // test if active profile was removed, activated default profile if so
      if (activeProfile.equals(profile)) {
        setActiveProfile(defaultProfile);
      }
    }
  }
  // </editor-fold>

}
