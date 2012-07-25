package cv.lecturesight.cca;

import com.nativelibs4java.opencl.CLBuffer;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.geometry.BoundingBox;

public interface BoundingBoxFinder {

  enum Signal {
    DONE
  }

  OCLSignal getSignal(Signal signal);
  CLBuffer<Integer> getBoxBuffer();
  BoundingBox getBox(int id);
  BoundingBox[] getAllBoxes();
  
}
