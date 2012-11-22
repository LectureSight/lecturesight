package cv.lecturesight.cca.impl;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.cca.BoundingBoxFinder;
import cv.lecturesight.cca.CentroidFinder;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.cca.ConnectedComponentService;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 */
@Component(name="lecturesight.caa",immediate=true)
@Service
public class ConnectedComponentServiceImpl implements ConnectedComponentService {

  final static String PROPKEY_MAX_BLOBS = "blobs.max";
  final static String PROPKEY_BLOBSIZE_MIN = "blobsize.min";
  final static String PROPKEY_BLOBSIZE_MAX = "blobsize.max";
  private Log log = new Log("Connected Component Analysis Service");
  @Reference
  private Configuration config;
  @Reference
  private OpenCLService ocl;

  protected void activate(ComponentContext cc) {
    log.info("Activated");
  }

  @Override
  public ConnectedComponentLabeler createLabeler(CLImage2D binaryImage) {
    ConnectedComponentLabelerImpl ccl = new ConnectedComponentLabelerImpl(binaryImage, ocl,
            config.getInt(PROPKEY_MAX_BLOBS), config.getInt(PROPKEY_BLOBSIZE_MIN), config.getInt(PROPKEY_BLOBSIZE_MAX));
    return ccl;
  }

  @Override
  public ConnectedComponentLabeler createLabeler(CLImage2D binaryImage, int maxBlobs, int minSize, int maxSize) {
    ConnectedComponentLabelerImpl ccl = new ConnectedComponentLabelerImpl(binaryImage, ocl, maxBlobs, minSize, maxSize);
    return ccl;
  }

  @Override
  public BoundingBoxFinder createBoundingBoxFinder(ConnectedComponentLabeler ccl) {
    if (ccl instanceof ConnectedComponentLabelerImpl) {
      ConnectedComponentLabelerImpl ccli = (ConnectedComponentLabelerImpl) ccl;
      BoundingBoxFinderImpl bbf = new BoundingBoxFinderImpl(ccli, ocl);
      return bbf;
    } else {
      throw new IllegalArgumentException("Incompatible ConnectedComponentLabeler implementation");
    }
  }

  @Override
  public CentroidFinder createCentroidFinder(ConnectedComponentLabeler ccl) {
    if (ccl instanceof ConnectedComponentLabelerImpl) {
      ConnectedComponentLabelerImpl ccli = (ConnectedComponentLabelerImpl) ccl;
      CentroidFinder cf = new CentroidFinderImpl(ccli, ocl);
      return cf;
    } else {
      throw new IllegalArgumentException("Incompatible ConnectedComponentLabeler implementation");
    }
  }
}
