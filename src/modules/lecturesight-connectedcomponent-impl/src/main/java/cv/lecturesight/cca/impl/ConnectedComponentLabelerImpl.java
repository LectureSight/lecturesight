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

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.UUID;

public class ConnectedComponentLabelerImpl implements ConnectedComponentLabeler {

  final static String SIGNAME_START = "cv.lecturesight.cca.analysis.START";
  final static String SIGNAME_ITERATE = "cv.lecturesight.cca.analysis.ITERATE";
  final static String SIGNAME_FINISH = "cv.lecturesight.cca.analysis.FINISH";
  final static String SIGNAME_DONE = "cv.lecturesight.cca.analysis.DONE";

  private EnumMap<ConnectedComponentLabeler.Signal, OCLSignal> signals =
          new EnumMap<ConnectedComponentLabeler.Signal, OCLSignal>(ConnectedComponentLabeler.Signal.class);
  private OpenCLService ocl;
  private ComputationRun initRun, updateRun, resultRun;
  private CLImage2D input;
  CLImage2D labelImage;
  CLBuffer<Integer> labels_work, sizes_work, ids, sizes, changed;
  int[] imageDim, bufferDim;
  int[] ids_out, sizes_out;
  long bufferSize;
  int minSize;
  int maxSize;
  int maxBlobs;
  int numBlobs = 0;
  OCLSignal trigger = null;   // custom signal to trigger CCL start

  public ConnectedComponentLabelerImpl(CLImage2D input, OpenCLService ocl, int maxBlobs, int minSize, int maxSize) {
    this.ocl = ocl;
    this.input = input;
    makeSignals();
    imageDim = new int[]{(int) input.getWidth(), (int) input.getHeight()};
    bufferDim = new int[]{imageDim[0] + 2, imageDim[1] + 2};
    bufferSize = bufferDim[0] * bufferDim[1];
    this.minSize = minSize;
    this.maxSize = maxSize;
    this.maxBlobs = maxBlobs;
    ids_out = new int[maxBlobs];
    sizes_out = new int[maxBlobs];
    labels_work = ocl.context().createIntBuffer(Usage.InputOutput, bufferSize);
    sizes_work = ocl.context().createIntBuffer(Usage.InputOutput, bufferSize);
    changed = ocl.context().createIntBuffer(Usage.InputOutput, 1);
    ids = ocl.context().createIntBuffer(Usage.InputOutput, maxBlobs+1);   // +1 -> [0] = numBlobs
    sizes = ocl.context().createIntBuffer(Usage.InputOutput, maxBlobs);
    labelImage = ocl.context().createImage2D(Usage.InputOutput, OpenCLService.Format.RGBA_UINT8.getCLImageFormat(), imageDim[0], imageDim[1]);

    // instantiate ComputationRuns
    initRun = new InitRun();
    updateRun = new UpdateRun();
    resultRun = new ResultRun();
    
    // register ComputationRuns
    ocl.registerLaunch(signals.get(Signal.START), initRun);
    ocl.registerLaunch(signals.get(Signal.ITERATE), updateRun);
    ocl.registerLaunch(signals.get(Signal.FINISH), resultRun);
  }

  private void makeSignals() {
    String UUIDsuffix = "-" + UUID.randomUUID().toString();
    signals.put(Signal.START, ocl.getSignal(SIGNAME_START + UUIDsuffix));
    signals.put(Signal.ITERATE, ocl.getSignal(SIGNAME_ITERATE + UUIDsuffix));
    signals.put(Signal.FINISH, ocl.getSignal(SIGNAME_FINISH + UUIDsuffix));
    signals.put(Signal.DONE, ocl.getSignal(SIGNAME_DONE + UUIDsuffix));
  }

  @Override
  public void doLabels() {
    ocl.castSignal(signals.get(Signal.START));
  }
  
  public void doLabels(OCLSignal trigger) {
    this.trigger = trigger;
    ocl.registerLaunch(trigger, initRun);
  }
  
  @Override
  public void dispose() {
    // unregister runs
    if (trigger != null) {
      ocl.unregisterLaunch(trigger, initRun);
    }
    ocl.unregisterLaunch(signals.get(Signal.START), initRun);
    ocl.unregisterLaunch(signals.get(Signal.ITERATE), updateRun);
    ocl.unregisterLaunch(signals.get(Signal.FINISH), resultRun);
    
    // release buffer object
    labels_work.release();
    sizes_work.release();
    changed.release();
    ids.release();
    sizes.release();
  }
  
  //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
  @Override
  public OCLSignal getSignal(Signal signal) {
    return signals.get(signal);
  }
  
  @Override
  public CLImage2D getLabelImage() {
    return labelImage;
  }

  @Override
  public CLBuffer<Integer> getLabelBuffer() {
    return labels_work;
  }

  @Override
  public void setMinBlobSize(int numPixels) {
    if (0 <= numPixels) {
      minSize = numPixels;
    }
  }

  @Override
  public void setMaxBlobSize(int numPixels) {
    maxSize = numPixels;
  }

  @Override
  public int getNumBlobs() {
    return numBlobs;
  }

  @Override
  public int[] getLabels() {
    return ids_out;           // TODO make thread-safe
  }

  @Override
  public int[] getSizes() {
    return sizes_out;         // TODO make thread-safe
  }
  
  public int getSize(int id) {
    return sizes_out[id-1];
  }
  
  @Override
  public int getMaxBlobs() {
    return maxBlobs;
  }
  
  @Override
  public void setInput(CLImage2D image) {
    this.input = image;
  }
  
  @Override
  public CLBuffer<Integer> getIdBuffer() {
    return ids;
  }
  
  @Override
  public CLBuffer<Integer> getSizeBuffer() {
    return sizes;
  }
  //</editor-fold>

  /** Initialization Run :
   *    Init label array, assign each element with a label that has a corresponding
   *    non-zero pixel in the input image, 0 is assigned otherwise.
   */
  private class InitRun implements ComputationRun {

    OCLSignal SIG_iterate = signals.get(Signal.ITERATE);
    CLKernel assignIndicesK = ocl.programs().getKernel("labelequiv", "assign_indices");

    @Override
    public void launch(CLQueue queue) {
      assignIndicesK.setArgs(input, labels_work, sizes_work, imageDim[0], imageDim[1]);
      assignIndicesK.enqueueNDRange(queue, bufferDim);
    }

    @Override
    public void land() {
      ocl.castSignal(SIG_iterate);
    }
  }

  /** Iteration Run:
   *    apply analysis and update step; fire ITERATE signal if at least one
   *    label has been updated, fire FINISH signal otherwise
   */
  private class UpdateRun implements ComputationRun {

    OCLSignal SIG_iterate = signals.get(Signal.ITERATE);
    OCLSignal SIG_finish = signals.get(Signal.FINISH);
    CLKernel resetK = ocl.programs().getKernel("labelequiv", "reset_change");
    CLKernel minNeighbourK = ocl.programs().getKernel("labelequiv", "min_neighbour");
    CLKernel updateIndicesK = ocl.programs().getKernel("labelequiv", "update_indices");
    IntBuffer changedH;

    @Override
    public void launch(CLQueue queue) {
      resetK.setArgs(changed);
      resetK.enqueueTask(queue);
      minNeighbourK.setArgs(labels_work, changed, imageDim[0], imageDim[1]);
      minNeighbourK.enqueueNDRange(queue, imageDim);
      updateIndicesK.setArgs(labels_work, sizes_work, imageDim[0], imageDim[1]);
      updateIndicesK.enqueueNDRange(queue, imageDim);
      changedH = changed.read(queue);
    }

    @Override
    public void land() {
      if (changedH.get(0) == 0) {
        ocl.castSignal(SIG_finish);
      } else {
        ocl.castSignal(SIG_iterate);
      }
    }
  }

  /** Result Run:
   *    Assign sequential IDs to blobs, download sizes and IDs.
   */
  private class ResultRun implements ComputationRun {

    OCLSignal SIG_done = signals.get(Signal.DONE);
    CLKernel resetK = ocl.programs().getKernel("labelequiv", "reset_change");
    CLKernel getResultsK = ocl.programs().getKernel("labelequiv", "make_blob_ids");
    CLKernel updateResultLabelK = ocl.programs().getKernel("labelequiv", "update_blob_ids");
    IntBuffer idsH, sizesH;

    @Override
    public void launch(CLQueue queue) {
      ocl.utils().setValues(0, (int) ids.getElementCount(), ids, 0);
      getResultsK.setArgs(labels_work, ids, sizes_work, sizes, imageDim[0], imageDim[1], minSize, maxSize, maxBlobs);
      getResultsK.enqueueNDRange(queue, imageDim);
      updateResultLabelK.setArgs(labels_work, labelImage, imageDim[0], imageDim[1]);
      updateResultLabelK.enqueueNDRange(queue, imageDim);
      idsH = ids.read(queue);
      sizesH = sizes.read(queue);
    }

    @Override
    public void land() {
      numBlobs = idsH.get(0);
      numBlobs = numBlobs <= maxBlobs ? numBlobs : maxBlobs;    // FIXME maxBlobs check in kernel seems to be ignored
      idsH.get(ids_out, 0, numBlobs);
      sizesH.get(sizes_out, 0, numBlobs);
      ocl.castSignal(SIG_done);
    }
  }
}
