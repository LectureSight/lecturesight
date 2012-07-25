package cv.lecturesight.cca;

import com.nativelibs4java.opencl.CLBuffer;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.geometry.Position;

public interface CentroidFinder {

  enum Signal {
    DONE
  }

  OCLSignal getSignal(Signal signal);
  CLBuffer<Integer> getCentroidBuffer();
  Position getControid(int id);
  Position[] getAllCentroids();
}
