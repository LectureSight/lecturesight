package cv.lecturesight.cca.impl;

import com.nativelibs4java.opencl.CLIntBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.cca.BoundingBoxFinder;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.UUID;

public class BoundingBoxFinderImpl implements BoundingBoxFinder {

  final static String SIGNAME_DONE = "cv.lecturesight.cca.bboxfinder.DONE";

  EnumMap<BoundingBoxFinder.Signal, OCLSignal> signals =
          new EnumMap<BoundingBoxFinder.Signal, OCLSignal>(Signal.class);
  OpenCLService ocl;
  ConnectedComponentLabelerImpl ccli;
  CLIntBuffer boxes;
  int[] bboxBufferDim;
  int[] boxes_out;

  public BoundingBoxFinderImpl(ConnectedComponentLabelerImpl ccli, OpenCLService ocl) {
    this.ccli = ccli;
    this.ocl = ocl;
    signals.put(Signal.DONE, ocl.getSignal(SIGNAME_DONE + "-" + UUID.randomUUID().toString()));
    bboxBufferDim = new int[] {ccli.maxBlobs * 4};
    boxes = ocl.context().createIntBuffer(Usage.InputOutput, bboxBufferDim[0]);
    boxes_out = new int[bboxBufferDim[0]];
    ocl.registerLaunch(ccli.getSignal(ConnectedComponentLabeler.Signal.DONE), new FindBoundingBoxesRun());
  }

  @Override
  public OCLSignal getSignal(Signal signal) {
    return signals.get(signal);
  }
  
  @Override
  public CLIntBuffer getBoxBuffer() {
    return boxes;
  }

  @Override
  public BoundingBox getBox(int id) {
    int index = id * 4;
    Position max = new Position(boxes_out[index++], boxes_out[index++]);
    Position min = new Position(boxes_out[index++], boxes_out[index]);
    return new BoundingBox(min, max);
  }

  @Override
  public BoundingBox[] getAllBoxes() {
    BoundingBox[] out = new BoundingBox[ccli.numBlobs];
    for (int i = 0; i < ccli.numBlobs; i++) {
      out[i] = getBox(i);
    }
    return out;
  }

  class FindBoundingBoxesRun implements ComputationRun {

    OCLSignal SIG_done = signals.get(Signal.DONE);
    CLKernel reset_bbox_bufferK = ocl.programs().getKernel("bbox", "reset_bbox_buffer");
    CLKernel compute_bboxesK = ocl.programs().getKernel("bbox", "compute_bboxes");
    IntBuffer boxesH;

    {
      reset_bbox_bufferK.setArgs(boxes);
      compute_bboxesK.setArgs(ccli.labels_work, boxes, ccli.imageDim[0], ccli.imageDim[1]);
    }

    @Override
    public void launch(CLQueue queue) {
      reset_bbox_bufferK.enqueueNDRange(queue, bboxBufferDim);
      compute_bboxesK.enqueueNDRange(queue, ccli.imageDim);
      boxesH = boxes.read(queue);
    }

    @Override
    public void land() {
      boxesH.get(boxes_out, 0, ccli.numBlobs * 4);    // better save numBlobs in BBFI too?
      //boxes_out = boxesH.getInts(ccli.numBlobs * 4);
      ocl.castSignal(SIG_done);
    }
  }
}
