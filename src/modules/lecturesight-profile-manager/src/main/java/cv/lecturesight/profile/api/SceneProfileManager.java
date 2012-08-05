package cv.lecturesight.profile.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface SceneProfileManager {

  void setActiveProfile(String id);
  
  SceneProfile getActiveProfile();
  
  List<SceneProfile> getProfiles();
  
  void addProfile(SceneProfile profile);
  
  void removeProfile(String id);
  
  SceneProfile loadProfile(InputStream is);
  
  String serializeProfile(SceneProfile profile);
  
  void serializeProfile(SceneProfile profile, OutputStream os);
  
  void registerProfileListener(ProfileChangeListener listener);
  
  void unregisterProfileListener(ProfileChangeListener listener);
  
}
