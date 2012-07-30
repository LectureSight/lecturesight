package cv.lecturesight.visualization;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.object.ObjectService;
import cv.lecturesight.object.TrackerObject;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.ui.CustomRenderer;
import cv.lecturesight.ui.DisplayService;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Iterator;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Visualization Service
 *
 */
@Component(name = "lecturesight.visualization", immediate = true)
@Service
public class VisualizationImpl implements Visualization, CustomRenderer {
  
  final static String PROPKEY_DISPLAY_VISUAL = "display.terminator";
  final static String WINDOWNAME_VISUAL = "visual";
  final static String SIGNAME_DONE_VISUAL = "visual.DONE";

  private Log log = new Log("Heartbeat");
  @Reference
  private Configuration config;
  @Reference
  private OpenCLService ocl;
  @Reference
  private FrameSourceProvider fsp;
  private FrameSource fsource;
  @Reference
  private ObjectService tracker;
  @Reference
  private DisplayService dsps;
//  CLImage2D visual;
  OCLSignal SIG_done;
  int[] workDim;
  private Font font = new Font("Monospaced", Font.PLAIN, 10);

  protected void activate(ComponentContext cc) throws Exception {
    SIG_done = ocl.getSignal(SIGNAME_DONE_VISUAL);
    fsource = fsp.getFrameSource();
//    workDim = new int[] { fsource.getWidth(), fsource.getHeight() };
    
    if (config.getBoolean(PROPKEY_DISPLAY_VISUAL)) {
//      visual = ocl.context().createImage2D(Usage.InputOutput,
//              Format.BGRA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
//      ocl.registerLaunch(tracker.getSignal(ObjectService.Signal.DONE_CORRELATION), new VisualizationRun());
//      dsps.registerDispaly(WINDOWNAME_VISUAL, "visual", visual, this, SIG_done);
      dsps.registerDispaly(WINDOWNAME_VISUAL, "visual", fsource.getImage(), 
              this, tracker.getSignal(ObjectService.Signal.DONE_CORRELATION));
    }
    log.info("Activated.");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    log.info("Deactivated");
  }

  @Override
  public void render(Graphics g) {
    TrackerObject[] objects = tracker.getAllTrackedObjects();
    g.setFont(font);
    g.setColor(Color.white);
    for (int i = 0; i < objects.length; i++) {
      TrackerObject obj = objects[i];
      
      BoundingBox box = obj.getBoundingBox();
      g.drawRect(box.getMin().getX(), box.getMin().getY(), box.getWidth(), box.getHeight());
      
      Position pos = obj.getCentroid();
      g.drawOval(pos.getX(), pos.getY(), 2, 2);
      
      String info = Integer.toString(obj.getId()) + ": " + Integer.toString(pos.getX()) + "/" + Integer.toString(pos.getY());
      g.drawString(info, box.getMin().getX(), box.getMin().getY() - 1);
      
      int x = obj.getBoundingBox().getMax().getX() + 1;
      int y = obj.getBoundingBox().getMin().getY();
      for (Iterator<String> it = obj.getProperties().keySet().iterator(); it.hasNext();) {
        String key = it.next();
        Object val = obj.getProperty(key);
        String prop = key + ": " + val.toString();
        g.drawString(prop, x, y);
        x += 10;
      }
    }
    g.drawString("   t: " + fsource.getFrameNumber(), 2, 26);
    g.drawString("objs: " + objects.length, 2, 36);
    
    ocl.castSignal(SIG_done);
  }

  @Override
  public OCLSignal getSignal() {
    return SIG_done;
  }

  private class VisualizationRun implements ComputationRun {

    CLKernel copyRedTintK = ocl.programs().getKernel("visual", "copy_red_tint");
    CLImage2D input = fsp.getFrameSource().getImage();

    @Override
    public void launch(CLQueue queue) {
//      copyRedTintK.setArgs(input, fgs.getForegroundMap(), labels_current, visual, workDim[0]);
      copyRedTintK.enqueueNDRange(queue, workDim);
    }

    @Override
    public void land() {
      ocl.castSignal(SIG_done);
    }
  }
}
