package cv.lecturesight.cca;

import com.nativelibs4java.opencl.CLImage2D;

public interface ConnectedComponentService {

  ConnectedComponentLabeler createLabeler(CLImage2D binaryImage);
  ConnectedComponentLabeler createLabeler(CLImage2D binaryImage, int maxBlobs, int minSize, int maxSize);
  BoundingBoxFinder createBoundingBoxFinder(ConnectedComponentLabeler ccl);
  CentroidFinder createCentroidFinder(ConnectedComponentLabeler ccl);
  
}
