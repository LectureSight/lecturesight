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
package cv.lecturesight.videoanalysis.backgroundmodel.impl;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.display.DisplayService;
import cv.lecturesight.videoanalysis.backgroundmodel.BackgroundModel;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.opencl.CLImageDoubleBuffer;
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
public class BackgroundModelImpl implements BackgroundModel {

  @Setter
  private Configuration config;
  @Setter
  private OpenCLService ocl;
  @Setter
  private DisplayService dsps;
  @Setter
  private FrameSourceProvider fsp;
  private FrameSource fsrc;
  private int[] workDim;
  private CLImage2D input;
  private CLImage2D background;
  private CLImageDoubleBuffer bgBuffer;
  private CLImage2D diffMap;
  private CLImage2D updateMap;
  private EnumMap<BackgroundModel.Signal, OCLSignal> signals =
          new EnumMap<BackgroundModel.Signal, OCLSignal>(BackgroundModel.Signal.class);

  protected void activate(ComponentContext cc) {
    // create signals
    signals.put(Signal.DONE_DIFF, ocl.getSignal(Constants.SIGNAME_DONE_DIFF));
    signals.put(Signal.DO_UPDATE, ocl.getSignal(Constants.SIGNAME_DO_UPDATE));
    signals.put(Signal.DONE_UPDATE, ocl.getSignal(Constants.SIGNAME_DONE_UPDATE));

    // set up input
    fsrc = fsp.getFrameSource();
    input = fsrc.getImage();
    workDim = new int[]{fsrc.getWidth(), fsrc.getHeight()};

    // allocate gpu buffers
    background = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    diffMap = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    updateMap = ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    bgBuffer = new CLImageDoubleBuffer(
            ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]),
            ocl.context().createImage2D(Usage.InputOutput, Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1])
            );

    ocl.utils().copyImage(0, 0, workDim[0], workDim[1], input, 0, 0, (CLImage2D)bgBuffer.last());
    ocl.utils().copyImage(0, 0, workDim[0], workDim[1], input, 0, 0, (CLImage2D)bgBuffer.current());
    ocl.utils().copyImage(0, 0, workDim[0], workDim[1], input, 0, 0, (CLImage2D)background);
    ocl.utils().setValues(0, 0, workDim[0], workDim[1], updateMap, 0);

    registerDisplays();

    // register computation runs
    ocl.registerLaunch(fsrc.getSignal(), new AnalysisRun());
    ocl.registerLaunch(signals.get(Signal.DO_UPDATE), new UpdateRun());
    Logger.info("Activated");
  }

  private void registerDisplays() {
    dsps.registerDisplay(Constants.WINDOWNAME_DIFF, diffMap, signals.get(Signal.DONE_DIFF));
    dsps.registerDisplay(Constants.WINDOWNAME_MODEL, background, signals.get(Signal.DONE_DIFF));
    dsps.registerDisplay(Constants.WINDOWNAME_UPDATEMAP, updateMap, signals.get(Signal.DONE_DIFF));
  }

  @Override
  public void updateBackground() {
    ocl.castSignal(signals.get(Signal.DO_UPDATE));
  }

  @Override
  public OCLSignal getSignal(Signal signal) {
    return signals.get(signal);
  }

  @Override
  public CLImage2D getDifferenceMap() {
    return diffMap;
  }

  @Override
  public CLImage2D getBackgroundImage() {
    return background;
  }

  @Override
  public CLImage2D getUpdateMap() {
    return updateMap;
  }

  public void reset() {
    ocl.utils().copyImage(0, 0, workDim[0], workDim[1], input, 0, 0, (CLImage2D)bgBuffer.last());
    Logger.info("Reinitialized");
  }

  private class AnalysisRun implements ComputationRun {

    CLKernel diffK = ocl.programs().getKernel("bgdiff", "bgdiff_3thresh");

    @Override
    public void launch(CLQueue queue) {
      diffK.setArgs(background, input, diffMap,
              config.getInt(Constants.PROPKEY_THRESH_LOW),
              config.getInt(Constants.PROPKEY_THRESH_MID),
              config.getInt(Constants.PROPKEY_THRESH_HIGH));
      diffK.enqueueNDRange(queue, workDim);
    }

    @Override
    public void land() {
      ocl.castSignal(signals.get(Signal.DONE_DIFF));
    }
  };

  private class UpdateRun implements ComputationRun {

    OCLSignal SIG_done = signals.get(Signal.DONE_UPDATE);
    CLKernel updateK = ocl.programs().getKernel("bgdiff", "update_model");

    @Override
    public void launch(CLQueue queue) {
      bgBuffer.swap();
      updateK.setArgs(input, bgBuffer.last(), updateMap, background,
              bgBuffer.current(), config.getFloat(Constants.PROPKEY_UPDATE_ALPHA));
      updateK.enqueueNDRange(queue, workDim);
    }

    @Override
    public void land() {
      ocl.castSignal(SIG_done);
    }
  }

}
