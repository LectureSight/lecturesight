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
package cv.lecturesight.videoanalysis.foreground.impl;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.videoanalysis.backgroundmodel.BackgroundModel;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.cca.ConnectedComponentService;
import cv.lecturesight.display.DisplayService;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.videoanalysis.change.ChangeDetector;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import cv.lecturesight.opencl.CLImageDoubleBuffer;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.OpenCLService.Format;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.OCLSignalBarrier;
import java.awt.image.BufferedImage;
import org.osgi.service.component.ComponentContext;
import cv.lecturesight.util.conf.Configuration;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.EnumMap;
import lombok.Setter;
import org.pmw.tinylog.Logger;

/** Foreground Service Implementation
 *
 * This foreground service constructs the foreground map by combining results
 * from the background subtraction and the inter-frame change detection. For
 * each pixel that changed since the last frame it is decided if it became fore-
 * ground or background, the corresponding pixel is added to or removed from the
 * foreground map accordingly. Foreground regions are labeled, small regions
 * are removed. The number of pixels that changed in each blob is counted, if
 * this number is below a threshold the blob is 'aged'. This way inactive blobs
 * disappear after a certain period of time.
 *
 */
public class ForegroundServiceImpl implements ForegroundService {

  // collection of this services signals
  private EnumMap<ForegroundService.Signal, OCLSignal> signals =
          new EnumMap<ForegroundService.Signal, OCLSignal>(ForegroundService.Signal.class);

  @Setter
  private Configuration config;             // this services configuration

  @Setter
  private OpenCLService ocl;                // OpenCL service

  @Setter
  private DisplayService dsps;              // display service

  @Setter
  private FrameSourceProvider fsp;
  private FrameSource fsrc;

  @Setter
  private BackgroundModel bgmodel;          // background model service

  @Setter
  private ChangeDetector changedetect;      // change detection service

  @Setter
  private ConnectedComponentService ccs;    // connected component analysis service

  private CLImage2D change;                 // output of change detection
  private CLImage2D bgdiff;                 // output of background subtraction
  private CLImage2D updateMap;              // working buffer indicating pixels to add/remove
  private CLImage2D fgUpdated;              // foreground map output buffer
  private CLImageDoubleBuffer fgBuffer;     // foreground map working buffer
  private CLBuffer<Integer> activity;             // buffer for activity count of blobs
  private int[] activities;
  private BufferedImage fgMapHost;
  private CLBuffer<Float> activity_ratio;

  private int[] workDim;                    // dimensions of buffers
  private ConnectedComponentLabeler ccl;    // connected component analyzer
  private OCLSignal ccl_START, ccl_DONE;    // connected component analyzer signals


  /** Activation method of this service. Sets up data structures, components
   *  and signals.
   *
   * @param ComponentContext
   * @throws Exception
   */
  protected void activate(ComponentContext cc) throws Exception {
    // create signals
    signals.put(Signal.DONE_ADDSUB, ocl.getSignal(Constants.SIGNAME_DONE_UPDATE));
    signals.put(Signal.DONE_CLEANING, ocl.getSignal(Constants.SIGNAME_DONE_CLEANING));

    // get input data pointers
    fsrc = fsp.getFrameSource();
    change = changedetect.getChangeMapDilated();
    bgdiff = bgmodel.getDifferenceMap();

    // input image dimensions
    workDim = new int[]{(int) change.getWidth(), (int) change.getHeight()};

    // allocate working buffers
    updateMap = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    fgUpdated = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    fgBuffer = new CLImageDoubleBuffer(
            ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]),
            ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]));
    activity = ocl.context().createIntBuffer(Usage.InputOutput, config.getInt(Constants.PROPKEY_CCL_MAXBLOBS)+1);
    activity_ratio = ocl.context().createFloatBuffer(Usage.InputOutput, config.getInt(Constants.PROPKEY_CCL_MAXBLOBS)+1);
    activities = new int[config.getInt(Constants.PROPKEY_CCL_MAXBLOBS)+1];

    reset();    // initialize working buffers

    // create connected component analyzer on foreground map
    ccl = ccs.createLabeler(fgUpdated, config.getInt(Constants.PROPKEY_CCL_MAXBLOBS),
            config.getInt(Constants.PROPKEY_CCL_MINSIZE), config.getInt(Constants.PROPKEY_CCL_MAXSIZE));
    ccl_START = ccl.getSignal(ConnectedComponentLabeler.Signal.START);
    ccl_DONE  = ccl.getSignal(ConnectedComponentLabeler.Signal.DONE);

    registerDisplays();

    // register UpdateRun to be launched when bgmodel and changedetect are done
    OCLSignalBarrier startBarrier = ocl.createSignalBarrier(new OCLSignal[]{
              changedetect.getSignal(ChangeDetector.Signal.DONE_DETECTION),
              bgmodel.getSignal(BackgroundModel.Signal.DONE_DIFF)
            });
    ocl.registerLaunch(startBarrier.getSignal(), new UpdateRun());
    ocl.registerLaunch(ccl_DONE, new MaskCleanRun());               // register cleaning run to be launched when CCA is done

    Logger.info("Activated");
  }

  //<editor-fold defaultstate="collapsed" desc="Display Registration">
  /** Register displays if configured
   *
   */
  private void registerDisplays() {
    dsps.registerDisplay(Constants.WINDOWNAME_UPDATEMAP, updateMap, signals.get(Signal.DONE_ADDSUB));
    dsps.registerDisplay(Constants.WINDOWNAME_FOREGROUNDMAP, fgUpdated, signals.get(Signal.DONE_CLEANING));
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
  @Override
  public OCLSignal getSignal(Signal signal) {
    return signals.get(signal);
  }

  @Override
  public CLImage2D getUpdateMap() {
    return updateMap;
  }

  @Override
  public CLImage2D getForegroundMap() {
    return fgUpdated;
  }

  @Override
  public CLImageDoubleBuffer getForegroundWorkingBuffer() {
    return fgBuffer;
  }

  @Override
  public ConnectedComponentLabeler getLabeler() {
    return ccl;
  }

  @Override
  public BufferedImage getForegroundMapHost() {
    return fgMapHost;
  }

  public int getActivity(int id) {
    return activities[id+1];
  }
  //</editor-fold>

  /** Resets all working buffers of this service.
   *
   */
  public void reset() {
    ocl.utils().setValues(0, 0, workDim[0], workDim[1], updateMap, 0);
    ocl.utils().setValues(0, 0, workDim[0], workDim[1], fgUpdated, 0);
    ocl.utils().setValues(0, 0, workDim[0], workDim[1], (CLImage2D) fgBuffer.current(), 0);
    ocl.utils().setValues(0, 0, workDim[0], workDim[1], (CLImage2D) fgBuffer.last(), 0);
    Logger.info("Working buffers initialized");
  }

  /** Run that computes the update map and performs the update of the foreground
   *  map. This Run is launched by the signal barrier listening to the change
   *  detection and background subtraction. Casts signal DONE_ADDSUB on completion.
   *
   */
  private class UpdateRun implements ComputationRun {

    OCLSignal SIG_done = signals.get(Signal.DONE_ADDSUB);
    CLKernel computeAddSubMaskK = ocl.programs().getKernel("fg", "compute_add_sub_mask");   // kernel that computes the update map
    CLKernel updateForegroundK = ocl.programs().getKernel("fg", "update_foreground");       // kernel that performs for foreground update
    CLKernel erode_FGBG_lr = ocl.programs().getKernel("fg", "erode_fg_bg_lr");
    CLKernel erode_FGBG_rl = ocl.programs().getKernel("fg", "erode_fg_bg_rl");
    CLKernel erode_FGBG_tb = ocl.programs().getKernel("fg", "erode_fg_bg_tb");
    CLKernel erode_FGBG_bt = ocl.programs().getKernel("fg", "erode_fg_bg_bt");
    int[] erodeDimVertical = new int[] {workDim[0]};
    int[] erodeDimHorizontal = new int[] {workDim[1]};

    @Override
    public void launch(CLQueue queue) {
      fgBuffer.swap();                                          // swap working buffer pointers

      computeAddSubMaskK.setArgs(change, bgdiff, updateMap);    // compute update map
      computeAddSubMaskK.enqueueNDRange(queue, workDim);

      updateForegroundK.setArgs(updateMap, fgBuffer.last());    // perform foreground mask update
      updateForegroundK.enqueueNDRange(queue, workDim);

      // horizontal phantom erosion left -> right
      erode_FGBG_lr.setArgs(fsrc.getImage(), fgBuffer.last(), fgBuffer.current(),
              config.getInt(Constants.PROPKEY_PHANTOMDECAY_THRESH), fsrc.getWidth());
      erode_FGBG_lr.enqueueNDRange(queue, erodeDimHorizontal);

      // horizontal phantom erosion right -> left
      erode_FGBG_rl.setArgs(fsrc.getImage(), fgBuffer.current(), fgBuffer.last(),
              config.getInt(Constants.PROPKEY_PHANTOMDECAY_THRESH), fsrc.getWidth());
      erode_FGBG_rl.enqueueNDRange(queue, erodeDimHorizontal);

      // vertical phantom erosion top -> bottom
      erode_FGBG_tb.setArgs(fsrc.getImage(), fgBuffer.last(), fgBuffer.current(),
              config.getInt(Constants.PROPKEY_PHANTOMDECAY_THRESH), fsrc.getHeight());
      erode_FGBG_tb.enqueueNDRange(queue, erodeDimVertical);

      // vertical phantom erosion bottom -> top
      erode_FGBG_bt.setArgs(fsrc.getImage(), fgBuffer.current(), fgBuffer.last(),
              config.getInt(Constants.PROPKEY_PHANTOMDECAY_THRESH), fsrc.getHeight());
      erode_FGBG_bt.enqueueNDRange(queue, erodeDimVertical);
    }

    @Override
    public void land() {
      ccl.setInput((CLImage2D) fgBuffer.last());                // update input pointer for CCA
      ocl.castSignal(SIG_done);                                 // cast completion signal
      ocl.castSignal(ccl_START);                                // trigger CCA
    }
  }

  /** Run that removes small blobs and ages inactive blobs in the foreground mask.
   *  This Run is launched when the connected component analysis is done. Casts
   *  signal DONE_CLEANING on completion.
   *
   */
  private class MaskCleanRun implements ComputationRun {

    OCLSignal SIG_done = signals.get(Signal.DONE_CLEANING);
    OCLSignal SIG_startBGUpdate = bgmodel.getSignal(BackgroundModel.Signal.DO_UPDATE);
    int[] bufferDim = new int[]{(int) activity.getElementCount()-1};                  // dimensions of the activity buffer
    CLKernel resetBuffer = ocl.programs().getKernel("fg", "reset_buffer");          // kernel that resets the activity buffer
    CLKernel fgRemoveSmallK = ocl.programs().getKernel("fg", "remove_smallblobs");  // kernel that removes small blobs
    CLKernel fgGatherActivity = ocl.programs().getKernel("fg", "gather_activity");  // kernel that computes the activity of blobs
    CLKernel fgActRatioK = ocl.programs().getKernel("fg", "compute_activity_ratios");
    CLKernel fgDecayK = ocl.programs().getKernel("fg", "refresh_decay");            // kernel that refrshes/ages blobs
    CLImage2D bgUpdateMask = bgmodel.getUpdateMap();
    IntBuffer activityH;
    FloatBuffer ratiosH;

    {
      resetBuffer.setArgs(activity, 0);     // parameters for reset kernel can be set once
    }

    @Override
    public void launch(CLQueue queue) {
      fgRemoveSmallK.setArgs(ccl.getLabelBuffer(), fgBuffer.last(), workDim[0], workDim[1]);              // remove small blobs
      fgRemoveSmallK.enqueueNDRange(queue, workDim);
      resetBuffer.enqueueNDRange(queue, bufferDim);                                                       // reset activity buffer
      fgGatherActivity.setArgs(ccl.getLabelBuffer(), activity, fgBuffer.last(), updateMap, workDim[0]);   // compute blob activity
      fgGatherActivity.enqueueNDRange(queue, workDim);
      fgActRatioK.setArgs(activity, ccl.getSizeBuffer(), activity_ratio);                                 // compute activity activity_ratio
      fgActRatioK.enqueueNDRange(queue, bufferDim);
      fgDecayK.setArgs(fgBuffer.last(), fgBuffer.current(), fgUpdated, ccl.getLabelBuffer(),              // refresh/age blobs
              activity_ratio, config.getFloat(Constants.PROPKEY_DECAY_THRESHRATIO),
              config.getInt(Constants.PROPKEY_DECAY_ALPHA), workDim[0]);
      fgDecayK.enqueueNDRange(queue, workDim);
      ocl.utils().copyImage(0, 0, workDim[0], workDim[1], fgUpdated, 0, 0, bgUpdateMask);
      fgMapHost = fgUpdated.read(queue);
      activityH = activity.read(queue);
    }

    @Override
    public void land() {
      activityH.get(activities);
      ocl.castSignal(SIG_startBGUpdate);
      ocl.castSignal(SIG_done);     // cast completion signal
    }
  }
}
