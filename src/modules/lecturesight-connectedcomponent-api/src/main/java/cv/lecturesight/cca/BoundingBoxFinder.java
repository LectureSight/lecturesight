package cv.lecturesight.cca;

import com.nativelibs4java.opencl.CLIntBuffer;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.geometry.BoundingBox;

public interface BoundingBoxFinder {

  enum Signal {
    DONE
  }

  OCLSignal getSignal(Signal signal);
  CLIntBuffer getBoxBuffer();
  BoundingBox getBox(int id);
  BoundingBox[] getAllBoxes();
  
}
