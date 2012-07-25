package cv.lecturesight.cca;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLBuffer;
import cv.lecturesight.opencl.api.OCLSignal;

public interface ConnectedComponentLabeler {

  enum Signal {
    START, ITERATE, FINISH, DONE
  }

  OCLSignal getSignal(Signal signal);
  void doLabels();
  CLBuffer<Integer> getLabelBuffer();
  CLBuffer<Integer> getIdBuffer();
  void setMinBlobSize(int numPixels);
  void setMaxBlobSize(int numPixels);
  int getNumBlobs();
  int[] getLabels();
  int[] getSizes();
  int getSize(int id);
  int getMaxBlobs();
  void dispose();
  void setInput(CLImage2D image);  
}
