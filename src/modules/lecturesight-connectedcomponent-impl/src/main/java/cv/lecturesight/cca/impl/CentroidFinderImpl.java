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

import cv.lecturesight.cca.CentroidFinder;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.geometry.Position;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;

import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.UUID;

public class CentroidFinderImpl implements CentroidFinder {

  final static String SIGNAME_DONE = "cv.lecturesight.cca.centroidfinder.DONE";
  EnumMap<CentroidFinder.Signal, OCLSignal> signals =
  new EnumMap<CentroidFinder.Signal, OCLSignal>(Signal.class);
  OpenCLService ocl;
  ConnectedComponentLabelerImpl ccli;
  CLBuffer<Integer> centroids;
  int maxCount;
  int numBlobs;
  int[] centroids_out;
  int[] workDim;

  public CentroidFinderImpl(ConnectedComponentLabelerImpl ccli, OpenCLService ocl) {
    this.ccli = ccli;
    this.ocl = ocl;
    signals.put(Signal.DONE, ocl.getSignal(SIGNAME_DONE + "-" + UUID.randomUUID().toString()));
    workDim = ccli.imageDim;
    this.maxCount = ccli.getMaxBlobs();
    centroids = ocl.context().createIntBuffer(Usage.InputOutput, maxCount * 2);
    centroids_out = new int[maxCount * 2];
    ocl.registerLaunch(ccli.getSignal(ConnectedComponentLabeler.Signal.DONE), new FindCentroidsRun());
  }

  @Override
  public OCLSignal getSignal(Signal signal) {
    return signals.get(signal);
  }

  @Override
  public CLBuffer<Integer> getCentroidBuffer() {
    return centroids;
  }

  @Override
  public Position getControid(int id) {
    int index = (id-1) * 2;
    Position pos = new Position(centroids_out[index++], centroids_out[index]);
    return pos;
  }

  @Override
  public Position[] getAllCentroids() {
    Position[] out = new Position[numBlobs];
    for (int i = 0; i < numBlobs; i++) {
      out[i] = getControid(i);
    }
    return out;
  }

  private class FindCentroidsRun implements ComputationRun {

    OCLSignal SIG_done = getSignal(Signal.DONE);
    IntBuffer centroidsH;
    CLKernel addCoordsK = ocl.programs().getKernel("centroid", "add_coordinates");
    CLKernel computeMeansK = ocl.programs().getKernel("centroid", "compute_means");

    {
      addCoordsK.setArgs(ccli.getLabelBuffer(), centroids, workDim[0], workDim[1]);
      computeMeansK.setArgs(centroids, ccli.sizes);
    }

    @Override
    public void launch(CLQueue queue) {
      numBlobs = ccli.getNumBlobs();
      if (numBlobs > 0) {
        ocl.utils().setValues(0, maxCount - 1, centroids, 0);
        addCoordsK.enqueueNDRange(queue, workDim);
        int[] currentDim = {numBlobs};
        computeMeansK.enqueueNDRange(queue, currentDim);
        centroidsH = centroids.read(queue);
      }
    }

    @Override
    public void land() {
      if (numBlobs > 0) {
        centroidsH.get(centroids_out, 0, numBlobs * 2);
        //centroids_out = centroidsH.getInts(numBlobs * 2);
      }
      ocl.castSignal(SIG_done);
    }
  }
}
