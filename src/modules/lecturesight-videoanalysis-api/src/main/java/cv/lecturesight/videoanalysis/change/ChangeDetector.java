package cv.lecturesight.videoanalysis.change;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.opencl.api.OCLSignal;

/** A ChangeDetector service is responsible for computing a binary map in which
 * pixels that changed since the last frame are marked. Pixels that changed must
 * have a value >0, all others zero.
 * 
 */
public interface ChangeDetector {

  enum Signal {
    DONE_DETECTION,
    DONE_CLEANING
  };

  OCLSignal getSignal(Signal signal);

  CLImage2D getChangeMapRaw();
  CLImage2D getChangeMapDilated();
}
