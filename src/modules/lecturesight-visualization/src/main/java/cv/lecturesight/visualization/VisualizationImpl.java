package cv.lecturesight.visualization;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.display.DisplayService;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.OpenCLService.Format;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.regiontracker.RegionTracker;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import javax.swing.JPanel;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

/**
 * Visualization Service
 *
 */
public class VisualizationImpl implements Visualization, UserInterface {

  static final String OBJ_PROPKEY_COLOR = "obj.color";
  final static String WINDOWNAME_VISUAL = "visual";
  final static String SIGNAME_DONE_VISUAL = "visual.DONE";
  @Setter
  private Configuration config;
  @Setter
  private OpenCLService ocl;
  @Setter
  private FrameSourceProvider fsp;
  private FrameSource fsource;
  @Setter
  ForegroundService fgs;
  @Setter
  private RegionTracker rTracker;
  @Setter
  private ObjectTracker oTracker;
  @Setter
  private DisplayService dsps;
  CLImage2D visual;
  OCLSignal sig_DONE;
  int[] workDim;
  JPanel panel;

  protected void activate(ComponentContext cc) throws Exception {
    sig_DONE = ocl.getSignal(SIGNAME_DONE_VISUAL);
    fsource = fsp.getFrameSource();
    workDim = new int[]{fsource.getWidth(), fsource.getHeight()};

    visual = ocl.context().createImage2D(Usage.InputOutput,
            Format.RGBA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    ocl.registerLaunch(rTracker.getSignal(RegionTracker.Signal.DONE_CORRELATION), new VisualizationRun());
    dsps.registerDisplay(WINDOWNAME_VISUAL, visual, sig_DONE);
    panel = new ObjectTrackerUIPanel(dsps.getDisplayBySID("visual"), rTracker, oTracker);
    Logger.info("Activated.");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    Logger.info("Deactivated");
  }

  @Override
  public OCLSignal getSignal() {
    return sig_DONE;
  }

  @Override
  public String getTitle() {
    return "Object Tracker";
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public boolean isResizeable() {
    return false;
  }
  
  private class VisualizationRun implements ComputationRun {

    CLKernel copyRedTintK = ocl.programs().getKernel("visual", "copy_red_tint");
    CLImage2D input = fsp.getFrameSource().getRawImage();

    @Override
    public void launch(CLQueue queue) {
      copyRedTintK.setArgs(input, fgs.getForegroundMap(), fgs.getLabeler().getLabelBuffer(), visual, workDim[0]);
      copyRedTintK.enqueueNDRange(queue, workDim);
    }

    @Override
    public void land() {
      ocl.castSignal(sig_DONE);
    }
  }
}
