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
