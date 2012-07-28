package cv.lecturesight.cca;

import com.nativelibs4java.opencl.CLIntBuffer;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.geometry.Position;

public interface CentroidFinder {

  enum Signal {
    DONE
  }

  OCLSignal getSignal(Signal signal);
  CLIntBuffer getCentroidBuffer();
  Position getControid(int id);
  Position[] getAllCentroids();
}
