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
package cv.lecturesight.videoanalysis.change.impl;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.display.DisplayService;
import cv.lecturesight.videoanalysis.change.ChangeDetector;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.OpenCLService.Format;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.conf.Configuration;
import java.util.EnumMap;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

/** Implementation of Service API
 *
 */
public class ChangeDetectorImpl implements ChangeDetector {

  @Setter
  Configuration config;
  @Setter
  private OpenCLService ocl;
  @Setter
  private DisplayService dsps;
  @Setter
  private FrameSourceProvider fsp;
  private FrameSource fsrc;
  CLImage2D input, last, twoback;
  CLImage2D changeMapRaw12;
  CLImage2D changeMapRaw23;
  CLImage2D changeMapRaw13;
  CLImage2D changeMapDilated;
  CLImage2D combinedDiffs;

  private int[] workDim;
  private EnumMap<ChangeDetector.Signal, OCLSignal> signals =
          new EnumMap<ChangeDetector.Signal, OCLSignal>(ChangeDetector.Signal.class);

  protected void activate(ComponentContext cc) {
    // create signals
    signals.put(Signal.DONE_DETECTION, ocl.getSignal(Constants.SIGNAME_DONE_DETECTION));

    // set up input
    fsrc = fsp.getFrameSource();
    input = fsrc.getImage();
    workDim = new int[]{fsrc.getWidth(), fsrc.getHeight()};

    // allocate gpu buffers
    twoback = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    last = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    changeMapRaw12 = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    changeMapRaw23 = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    changeMapRaw13 = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    changeMapDilated = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    combinedDiffs = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);

    ocl.utils().copyImage(0, 0, workDim[0], workDim[1], input, 0, 0, last);
    ocl.utils().copyImage(0, 0, workDim[0], workDim[1], input, 0, 0, twoback);

    registerDisplays();

    // register computation runs
    ocl.registerLaunch(fsrc.getSignal(), new ChangeDetectRun());
    Logger.info("Activated");
  }

  private void registerDisplays() {
    dsps.registerDisplay(Constants.WINDOWNAME_CHANGE_RAW, changeMapRaw12, signals.get(Signal.DONE_DETECTION));
    dsps.registerDisplay(Constants.WINDOWNAME_CHANGE_RAW, changeMapRaw23, signals.get(Signal.DONE_DETECTION));
    dsps.registerDisplay(Constants.WINDOWNAME_CHANGE_RAW, changeMapRaw13, signals.get(Signal.DONE_DETECTION));
    dsps.registerDisplay(Constants.WINDOWNAME_CHANGE_DILATED, changeMapDilated, signals.get(Signal.DONE_DETECTION));
  }

  @Override
  public OCLSignal getSignal(Signal signal) {
    return signals.get(signal);
  }

  @Override
  public CLImage2D getChangeMapRaw12() {
    return changeMapRaw12;
  }

  @Override
  public CLImage2D getChangeMapRaw23() {
    return changeMapRaw23;
  }
  @Override
  public CLImage2D getChangeMapRaw13() {
    return changeMapRaw13;
  }

  @Override
  public CLImage2D getChangeMapDilated() {
    return changeMapDilated;
  }

  private class ChangeDetectRun implements ComputationRun {

    OCLSignal SIG_done = signals.get(Signal.DONE_DETECTION);
    CLKernel absDiffThreshK = ocl.programs().getKernel("change", "abs_diff_thresh");
    CLKernel dilateK = ocl.programs().getKernel("change", "image_dilate8");

    {
      dilateK.setArgs(combinedDiffs, changeMapDilated);
    }

    @Override
    public void launch(CLQueue queue) {
      absDiffThreshK.setArgs(input, last, changeMapRaw12, config.getInt(Constants.PROPKEY_THRESH));
      absDiffThreshK.enqueueNDRange(queue, workDim);
      absDiffThreshK.setArgs(input, twoback, changeMapRaw13, config.getInt(Constants.PROPKEY_THRESH));
      absDiffThreshK.enqueueNDRange(queue, workDim);
//      absDiffThreshK.setArgs(last, twoback, changeMapRaw23, config.getInt(Constants.PROPKEY_THRESH));
//      absDiffThreshK.enqueueNDRange(queue, workDim);
      absDiffThreshK.setArgs(changeMapRaw12, changeMapRaw13, combinedDiffs, config.getInt(Constants.PROPKEY_THRESH));
      absDiffThreshK.enqueueNDRange(queue, workDim);
      ocl.utils().copyImage(0, 0, workDim[0], workDim[1], last, 0, 0, twoback);
      ocl.utils().copyImage(0, 0, workDim[0], workDim[1], input, 0, 0, last);
      dilateK.enqueueNDRange(queue, workDim);
    }

    @Override
    public void land() {
      ocl.castSignal(SIG_done);
    }
  }
}
