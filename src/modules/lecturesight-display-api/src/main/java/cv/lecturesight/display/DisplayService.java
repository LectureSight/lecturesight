package cv.lecturesight.display;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.opencl.api.OCLSignal;
import java.util.Set;

public interface DisplayService {

  DisplayRegistration registerDisplay(String sid, CLImage2D image, OCLSignal trigger);
  Set<DisplayRegistration> getDisplayRegistrations();
  Display getDisplayByRegistration(DisplayRegistration reg);
  Display getDisplayByNumber(int num);
  Display getDisplayBySID(String sid);
  void addRegistrationListener(DisplayRegistrationListener listener);
  void removeRegistrationListener(DisplayRegistrationListener listener);

}
