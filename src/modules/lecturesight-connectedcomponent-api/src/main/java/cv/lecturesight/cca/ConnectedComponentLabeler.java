package cv.lecturesight.cca;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLIntBuffer;
import cv.lecturesight.opencl.api.OCLSignal;

public interface ConnectedComponentLabeler {

  enum Signal {
    START, ITERATE, FINISH, DONE
  }

  OCLSignal getSignal(Signal signal);
  void doLabels();
  CLIntBuffer getLabelBuffer();
  CLIntBuffer getIdBuffer();
  void setMinBlobSize(int numPixels);
  void setMaxBlobSize(int numPixels);
  int getNumBlobs();
  int[] getLabels();
  int[] getSizes();
  int getMaxBlobs();
  void dispose();
  void setInput(CLImage2D image);  
}
