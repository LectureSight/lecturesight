package cv.lecturesight.opencl.api;

import com.nativelibs4java.opencl.CLQueue;

public interface ComputationRun {

  void launch(CLQueue queue);

  void land();

}
