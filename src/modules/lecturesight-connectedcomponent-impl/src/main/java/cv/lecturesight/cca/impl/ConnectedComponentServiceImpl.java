/* Copyright (C) 2012 Benjamin Wulff
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package cv.lecturesight.cca.impl;

import cv.lecturesight.cca.BoundingBoxFinder;
import cv.lecturesight.cca.CentroidFinder;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.cca.ConnectedComponentService;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.util.conf.Configuration;

import com.nativelibs4java.opencl.CLImage2D;

import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

/** Implementation of Service API
 *
 */
public class ConnectedComponentServiceImpl implements ConnectedComponentService {

  final static String PROPKEY_MAX_BLOBS = "blobs.max";
  final static String PROPKEY_BLOBSIZE_MIN = "blobsize.min";
  final static String PROPKEY_BLOBSIZE_MAX = "blobsize.max";
  @Setter
  private Configuration config;
  @Setter
  private OpenCLService ocl;

  protected void activate(ComponentContext cc) {
    Logger.info("Activated");
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
