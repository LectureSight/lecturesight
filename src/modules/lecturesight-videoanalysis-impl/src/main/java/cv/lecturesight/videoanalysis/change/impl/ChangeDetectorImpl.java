package cv.lecturesight.videoanalysis.change.impl;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.videoanalysis.change.ChangeDetector;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.OpenCLService.Format;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.ui.DisplayService;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.util.EnumMap;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 * @scr.component name="lecturesight.changedetector" immediate="true"
 * @scr.service
 */
public class ChangeDetectorImpl implements ChangeDetector {

  Log log = new Log("Change Detector Service");
  /** @scr.reference */
  private Configuration config;
  /** @scr.reference */
  private OpenCLService ocl;
  /** @scr.reference */
  private DisplayService dsps;
  /** @scr.reference */
  private FrameSourceProvider fsp;
  private FrameSource fsrc;
  CLImage2D input, last;
  CLImage2D changeMapRaw;
  CLImage2D changeMapDilated;
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
    last = ocl.context().createImage2D(Usage.InputOutput, Format.BGRA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    changeMapRaw = ocl.context().createImage2D(Usage.InputOutput, Format.INTENSITY_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    changeMapDilated = ocl.context().createImage2D(Usage.InputOutput, Format.INTENSITY_UINT8.getCLImageFormat(), workDim[0], workDim[1]);

    ocl.utils().copyImage(0, 0, workDim[0], workDim[1], input, 0, 0, last);

    registerDisplays();

    // register computation runs
    ocl.registerLaunch(fsrc.getSignal(), new ChangeDetectRun());
    log.info("Activated");
  }

  private void registerDisplays() {
    if (config.getBoolean(Constants.PROPKEY_DISPLAY_RAW)) {
      dsps.registerDisplay(Constants.WINDOWNAME_CHANGE_RAW, "IF-Diff",
              changeMapRaw, signals.get(Signal.DONE_DETECTION));
    }
    if (config.getBoolean(Constants.PROPKEY_DISPLAY_DILATED)) {
      dsps.registerDisplay(Constants.WINDOWNAME_CHANGE_DILATED, "IF-Diff dilated",
              changeMapDilated, signals.get(Signal.DONE_DETECTION));
    }
  }

  @Override
  public OCLSignal getSignal(Signal signal) {
    return signals.get(signal);
  }

  @Override
  public CLImage2D getChangeMapRaw() {
    return changeMapRaw;
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
      dilateK.setArgs(changeMapRaw, changeMapDilated);
    }

    @Override
    public void launch(CLQueue queue) {
      absDiffThreshK.setArgs(input, last, changeMapRaw, config.getInt(Constants.PROPKEY_THRESH));
      absDiffThreshK.enqueueNDRange(queue, workDim);
      ocl.utils().copyImage(0, 0, workDim[0], workDim[1], input, 0, 0, last);
      dilateK.enqueueNDRange(queue, workDim);
    }

    @Override
    public void land() {
      ocl.castSignal(SIG_done);
    }
  }
}
