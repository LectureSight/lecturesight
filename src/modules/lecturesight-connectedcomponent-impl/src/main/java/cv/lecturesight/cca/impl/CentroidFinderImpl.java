package cv.lecturesight.cca.impl;

import com.nativelibs4java.opencl.CLIntBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.cca.CentroidFinder;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.geometry.Position;
import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.UUID;

public class CentroidFinderImpl implements CentroidFinder {

  final static String SIGNAME_DONE = "cv.lecturesight.cca.centroidfinder.DONE";
  EnumMap<CentroidFinder.Signal, OCLSignal> signals =
          new EnumMap<CentroidFinder.Signal, OCLSignal>(Signal.class);
  OpenCLService ocl;
  ConnectedComponentLabelerImpl ccli;
  CLIntBuffer centroids;
  int maxCount, numBlobs;
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
  public CLIntBuffer getCentroidBuffer() {
    return centroids;
  }
  
  @Override
  public Position getControid(int id) {
    int index = id * 2;
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
      }
      ocl.castSignal(SIG_done);
    }
  }
}
