package cv.lecturesight.videoanalysis.backgroundmodel;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.opencl.api.OCLSignal;

public interface BackgroundModel {

  enum Signal {
    DONE_DIFF, DO_UPDATE, DONE_UPDATE
  };

  void updateBackground();

  OCLSignal getSignal(Signal signal);

  CLImage2D getDifferenceMap();

  CLImage2D getBackgroundImage();

  CLImage2D getUpdateMap();
}
