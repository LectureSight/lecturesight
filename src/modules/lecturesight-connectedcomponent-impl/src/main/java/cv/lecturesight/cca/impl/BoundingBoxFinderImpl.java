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
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;

import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.UUID;

public class BoundingBoxFinderImpl implements BoundingBoxFinder {

  final static String SIGNAME_DONE = "cv.lecturesight.cca.bboxfinder.DONE";

  EnumMap<BoundingBoxFinder.Signal, OCLSignal> signals =
          new EnumMap<BoundingBoxFinder.Signal, OCLSignal>(Signal.class);
  OpenCLService ocl;
  ConnectedComponentLabelerImpl ccli;
  CLBuffer<Integer> boxes;
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
  public CLBuffer<Integer> getBoxBuffer() {
    return boxes;
  }

  @Override
  public BoundingBox getBox(int id) {
    int index = (id-1) * 4;
    Position max = new Position(boxes_out[index++], boxes_out[index++]);
    Position min = new Position(boxes_out[index++], boxes_out[index]);
    return new BoundingBox(min, max);
  }

  @Override
  public BoundingBox[] getAllBoxes() {
    BoundingBox[] out = new BoundingBox[ccli.numBlobs];
    for (int i = 1; i <= ccli.numBlobs; i++) {
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
