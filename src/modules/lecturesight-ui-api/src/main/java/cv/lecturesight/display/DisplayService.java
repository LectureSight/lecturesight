package cv.lecturesight.display;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.opencl.api.OCLSignal;
import java.util.Set;

public interface DisplayService {

  DisplayRegistration registerDisplay(String id, String title, CLImage2D image, OCLSignal trigger);
  DisplayRegistration registerDispaly(String id, String title, CLImage2D image, CustomRenderer renderer, OCLSignal trigger);
  Display getWindowByNumber(int num);
  Display getWindowById(String id);
  Set<DisplayRegistration> getAllDisplays();

}
