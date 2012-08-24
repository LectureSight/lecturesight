package cv.lecturesight.visualization;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.display.CustomRenderer;
import cv.lecturesight.display.DisplayService;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.regiontracker.Region;
import cv.lecturesight.regiontracker.RegionTracker;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
  static final String OBJ_PROPKEY_COLOR = "obj.color";
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
  private RegionTracker rTracker;
  @Reference
  private ObjectTracker oTracker;
  @Reference
  private DisplayService dsps;
//  CLImage2D visual;
  OCLSignal sig_DONE;
  int[] workDim;
  private Font font = new Font("Monospaced", Font.PLAIN, 10);
  private Font smallFont = new Font("Monospaced", Font.PLAIN, 8);
  
  protected void activate(ComponentContext cc) throws Exception {
    sig_DONE = ocl.getSignal(SIGNAME_DONE_VISUAL);
    fsource = fsp.getFrameSource();
//    workDim = new int[] { fsource.getWidth(), fsource.getHeight() };

    if (config.getBoolean(PROPKEY_DISPLAY_VISUAL)) {
//      visual = ocl.context().createImage2D(Usage.InputOutput,
//              Format.BGRA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
//      ocl.registerLaunch(rTracker.getSignal(ObjectService.Signal.DONE_CORRELATION), new VisualizationRun());
//      dsps.registerDispaly(WINDOWNAME_VISUAL, "visual", visual, this, sig_DONE);
      dsps.registerDispaly(WINDOWNAME_VISUAL, "visual", fsource.getImage(),
              this, oTracker.getSignal());
    }
    log.info("Activated.");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    log.info("Deactivated");
  }

  @Override
  public OCLSignal getSignal() {
    return sig_DONE;
  }

  @Override
  public void render(Graphics g) {

    // draw RegionTracker data
    g.setColor(Color.lightGray);
    List<Region> regions = rTracker.getRegions();
    for (Iterator<Region> it = regions.iterator(); it.hasNext();) {
      Region region = it.next();

      BoundingBox box = region.getBoundingBox();
      g.drawRect(box.getMin().getX(), box.getMin().getY(), box.getWidth(), box.getHeight());

      Position pos = region.getCentroid();
      g.drawOval(pos.getX(), pos.getY(), 2, 2);

      //String info = Integer.toString(region.getLabel()) + ": " + Integer.toString(pos.getX()) + "/" + Integer.toString(pos.getY());
      g.setFont(smallFont);
      String info = Integer.toString(region.getLabel());
      g.drawString(info, box.getMin().getX() + 1, box.getMin().getY() + 10);
    }

    // draw ObjectTracker data
    List<TrackerObject> objects = oTracker.getCurrentlyTracked();
    Map<Integer, TrackerObject> all_o = oTracker.getAllObjects();
    for (TrackerObject object : objects) {
      g.setColor((Color) object.getProperty(OBJ_PROPKEY_COLOR));

      BoundingBox box = (BoundingBox) object.getProperty("obj.bbox");
      g.drawRect(box.getMin().getX(), box.getMin().getY(), box.getWidth(), box.getHeight());

      String info = Integer.toString(object.getId());
      g.setFont(font);
      g.drawString(info, box.getMin().getX(), box.getMin().getY() - 1);
      
      if (object.hasProperty("head.center")) {
        g.setColor(Color.cyan);
        BoundingBox hbox = (BoundingBox) object.getProperty("head.boundingbox");
        g.drawRect(box.getMin().getX() + hbox.getMin().getX(), 
                box.getMin().getY() + hbox.getMin().getY(), 
                hbox.getWidth(), hbox.getHeight());
      }

//      int x = box.getMin().getX();
//      int y = box.getMax().getY() + 8;
//      g.setFont(smallFont);
//      for (Iterator<String> pit = object.getProperties().keySet().iterator(); pit.hasNext();) {
//        String key = pit.next();
//        Object val = object.getProperty(key);
//        String prop = key + ": " + val.toString();
//        g.drawString(prop, x, y);
//        y += 10;
//      }
    }
    
    // draw frame information
    g.setColor(Color.white);
    g.setFont(font);
    g.drawString("      t : " + fsource.getFrameNumber(), 2, 26);
    g.drawString("regions : " + regions.size(), 2, 36);
    g.drawString("objects : " + objects.size(), 2, 46);
    g.drawString("tracked objects : "+ all_o.size(), 2, 56);

    ocl.castSignal(sig_DONE);
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
      ocl.castSignal(sig_DONE);
    }
  }
}
