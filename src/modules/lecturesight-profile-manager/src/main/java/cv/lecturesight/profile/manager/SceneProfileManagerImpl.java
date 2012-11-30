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

import cv.lecturesight.profile.api.ConfigParameter;
import cv.lecturesight.profile.api.ProfileChangeListener;
import cv.lecturesight.profile.api.SceneProfile;
import cv.lecturesight.profile.api.SceneProfileManager;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.io.*;
import java.util.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.osgi.service.component.ComponentContext;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.scr.annotations.Reference;

public class SceneProfileManagerImpl implements SceneProfileManager, ArtifactInstaller {

  static final String FILEEXT_PROFILE = ".spf";
  private Log log = new Log("Scene Profile Manager");
  @Reference
  private Configuration config;
  private Map<String, SceneProfile> profiles = new HashMap<String, SceneProfile>();
  private Map<String, String> installed = new HashMap<String, String>();
  private Set<ProfileChangeListener> subscribers = new HashSet<ProfileChangeListener>();
  private SceneProfile activeProfile = new SceneProfile("default", "default");
  private Marshaller serializer;
  private Unmarshaller deserializer;

  protected void activate(ComponentContext cc) throws Exception {
    JAXBContext context = JAXBContext.newInstance(SceneProfile.class);
    serializer = context.createMarshaller();
    deserializer = context.createUnmarshaller();
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated");
  }

  @Override
  public void registerProfileListener(ProfileChangeListener listener) {
    subscribers.add(listener);
  }
  
  @Override
  public void unregisterProfileListener(ProfileChangeListener listener) {
    subscribers.remove(listener);
  }
  
  private void notifySubscribers(SceneProfile profile) {
    for (ProfileChangeListener listener : subscribers) {
      listener.profileChanged(profile);
    }
  }

  @Override
  public void setActiveProfile(String id) {
    if (profiles.containsKey(id)) {
      activeProfile = profiles.get(id);
      for (ConfigParameter param : activeProfile.getConfigOverride()) {
        config.set(param.getKey(), param.getValue());
      }
      notifySubscribers(activeProfile);
    } else {
      log.warn("Profile " + id + " does not exist.");
    }
  }

  @Override
  public SceneProfile getActiveProfile() {
    return activeProfile;
  }

  @Override
  public List<SceneProfile> getProfiles() {
    List<SceneProfile> out = new LinkedList<SceneProfile>();
    out.addAll(profiles.values());
    return out;
  }

  @Override
  public void addProfile(SceneProfile profile) {
    profiles.put(profile.getId(), profile);
  }

  @Override
  public void removeProfile(String id) {
    if (profiles.containsKey(id)) {
      profiles.remove(id);
      if (activeProfile.getId().equals(id)) {
        log.warn("Active profile was removed! Changes to profile will not be stored from now on. Profile will stay active until other profile is choosen.");
      }
    }
  }

  @Override
  public SceneProfile loadProfile(InputStream is) throws IllegalArgumentException {
    try {
      SceneProfile out = (SceneProfile) deserializer.unmarshal(is);
      is.close();
      return out;
    } catch (JAXBException e) {
      String msg = "Unable to deserialize SceneProfile: " + e;
      log.error(msg, e);
      throw new IllegalArgumentException(msg);
    } catch (IOException e) {
      log.warn("Unable to close InputStream: " + e.getMessage());
    }
    return null;
  }

  @Override
  public String serializeProfile(SceneProfile profile) {
    try {
      StringWriter sw = new StringWriter();
      serializer.marshal(profile, sw);
      return sw.toString();
    } catch (JAXBException e) {
      String msg = "Unable to deserialize SceneProfile: " + e;
      log.warn(msg);
      throw new IllegalArgumentException(msg);
    }
  }

  @Override
  public void serializeProfile(SceneProfile profile, OutputStream os) {
    try {
      serializer.marshal(profile, os);
    } catch (JAXBException e) {
      String msg = "Unable to deserialize SceneProfile: " + e;
      log.warn(msg);
      throw new IllegalArgumentException(msg);
    }
  }

  @Override
  public void install(File file) throws Exception {
    SceneProfile profile = loadProfile(new FileInputStream(file));
    if (profile.getId().equals(activeProfile.getId())) {
      notifySubscribers(profile);
    }
    addProfile(profile);
    installed.put(file.getName(), profile.getId());
    log.info("Installed scene profile " + profile.getName() + " [" + profile.getId() + "]");
  }

  @Override
  public void update(File file) throws Exception {
    SceneProfile profile = loadProfile(new FileInputStream(file));
    if (profile.getId().equals(activeProfile.getId())) {
      notifySubscribers(profile);
    }
    profiles.put(profile.getId(), profile);
    log.info("Updated scene profile " + profile.getName() + " [" + profile.getId() + "]");
  }

  @Override
  public void uninstall(File file) throws Exception {
    String filename = file.getName();
    if (installed.containsKey(filename)) {
      String id = installed.get(filename);
      SceneProfile profile = profiles.get(id);
      removeProfile(id);
      log.info("Updated scene profile " + profile.getName() + " [" + id + "]");
    }
  }

  @Override
  public boolean canHandle(File file) {
    return file.isFile() && file.getName().toLowerCase().endsWith(FILEEXT_PROFILE);
  }

}
