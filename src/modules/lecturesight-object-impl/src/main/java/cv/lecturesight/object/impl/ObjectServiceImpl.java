package cv.lecturesight.object.impl;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLIntBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.cca.BoundingBoxFinder;
import cv.lecturesight.cca.CentroidFinder;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.cca.ConnectedComponentService;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.object.ObjectService;
import cv.lecturesight.opencl.CLImageDoubleBuffer;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.OpenCLService.Format;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.OCLSignalBarrier;
import cv.lecturesight.ui.CustomRenderer;
import cv.lecturesight.ui.DisplayService;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.nio.IntBuffer;
import java.util.EnumMap;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 * @scr.component name="lecturesight.objects" immediate="true"
 * @scr.service
 */
public class ObjectServiceImpl implements ObjectService, CustomRenderer {

  // collection of this services signals
  private EnumMap<ObjectService.Signal, OCLSignal> signals =
          new EnumMap<ObjectService.Signal, OCLSignal>(ObjectService.Signal.class);
  
  private Log log = new Log("Object Service");

  /** @scr.reference */
  private Configuration config;
  /** @scr.reference */
  private OpenCLService ocl;
  /** @scr.reference */
  private DisplayService dsps;
  /** @scr.reference */
  private ConnectedComponentService ccs;
  /** @scr.reference */
  private FrameSourceProvider fsp;
  /** @scr.reference */
  private ForegroundService fgs;
  
  int[] workDim;
  FrameSource fs;
  ConnectedComponentLabeler foregroundLabeler, overlapLabeler;
  BoundingBoxFinder boxFinder;
  CentroidFinder centroidFinder;
  OCLSignalBarrier analysisBarrier;
  CLIntBuffer labels_current, labels_last;
  CLIntBuffer label_pairs;
  int[] pairs = new int[Constants.pairsBufferLength];
  CLImage2D overlap, visual;
  CLImageDoubleBuffer fgBuffer;
  int max_objects;
  TrackerObject[] objects;

  protected void activate(ComponentContext cc) throws Exception {
    signals.put(Signal.DONE_COMPUTE_OVERLAP, ocl.getSignal(Constants.SIGNAME_DONE_COMPUTE_OVERLAP));
    signals.put(Signal.DONE_VISUAL, ocl.getSignal(Constants.SIGNAME_DONE_VISUAL));
    signals.put(Signal.DONE_CORRELATION, ocl.getSignal(Constants.SIGNAME_DONE_CORRELATION));
    
    fs = fsp.getFrameSource();
    
    // get global working range
    workDim = new int[] { (int)fgs.getForegroundMap().getWidth(), 
                          (int)fgs.getForegroundMap().getHeight() };
    max_objects = config.getInt(Constants.PROPKEY_OBJECTS_MAX);

    foregroundLabeler = fgs.getLabeler();
    boxFinder = ccs.createBoundingBoxFinder(foregroundLabeler);
    centroidFinder = ccs.createCentroidFinder(foregroundLabeler);
    analysisBarrier = ocl.createSignalBarrier(new OCLSignal[] {
      fgs.getSignal(ForegroundService.Signal.DONE_CLEANING), 
      boxFinder.getSignal(BoundingBoxFinder.Signal.DONE),
      centroidFinder.getSignal(CentroidFinder.Signal.DONE)
    });
    
    // init label buffer copy 
    labels_current = foregroundLabeler.getLabelBuffer();
    labels_last = ocl.context().createIntBuffer(Usage.InputOutput, labels_current.getElementCount());
    ocl.utils().setValues(0, (int) labels_last.getElementCount() - 1, labels_last, 0);

    // init overlap image
    fgBuffer = fgs.getForegroundWorkingBuffer();
    overlap = ocl.context().createImage2D(Usage.InputOutput, 
            Format.INTENSITY_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
    
    // init buffer for label pairs
    label_pairs = ocl.context().createIntBuffer(Usage.InputOutput, Constants.pairsBufferLength);
    
    // init overlap computation run
    ocl.registerLaunch(fgs.getSignal(ForegroundService.Signal.DONE_CLEANING), new OverlapImageRun());
    
    // init CC-Labeler on overlap image
    overlapLabeler = ccs.createLabeler(overlap, fgs.getLabeler().getMaxBlobs(), 1, Integer.MAX_VALUE);
    
    // init correlation run
    ocl.registerLaunch(overlapLabeler.getSignal(ConnectedComponentLabeler.Signal.DONE), new CorrelationRun());
    
    // set up terminator-like view if configured
    if (config.getBoolean(Constants.PROPKEY_DISPLAY_OVERLAP)) {
      visual = ocl.context().createImage2D(Usage.InputOutput, 
              Format.BGRA_UINT8.getCLImageFormat(), workDim[0], workDim[1]);
      ocl.registerLaunch(analysisBarrier.getSignal(), new VisualizationRun());
      dsps.registerDispaly(Constants.WINDOWNAME_VISUAL, "visual", visual, 
              this, signals.get(Signal.DONE_VISUAL));
    }
    
    registerDisplays();
    
    log.info("Object Service activated.");
  }
  
  //<editor-fold defaultstate="collapsed" desc="Display Registration">
  /** Register displays if configured 
   * 
   */
  private void registerDisplays() {
    // register overlap display if configured
    if (config.getBoolean(Constants.PROPKEY_DISPLAY_OVERLAP)) {
      dsps.registerDisplay(Constants.WINDOWNAME_OVERLAP, "temporal overlap",
              overlap, signals.get(Signal.DONE_COMPUTE_OVERLAP));
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
  //</editor-fold>
  
  @Override
  public void render(Graphics g) {
    Font font = new Font("Monospaced", Font.PLAIN, 10);
    g.setFont(font);
    int minWidth = config.getInt(Constants.PROPKEY_WIDTH_MIN);
    int minHeight = config.getInt(Constants.PROPKEY_HEIGHT_MIN);
    int maxWidth = config.getInt(Constants.PROPKEY_WIDTH_MAX);
    int maxHeight = config.getInt(Constants.PROPKEY_HEIGHT_MAX);
    int num = foregroundLabeler.getNumBlobs();
    for (int i=0; i < num; i++) {
      BoundingBox box = boxFinder.getBox(i);
      Position pos = centroidFinder.getControid(i);
      int width = box.getWidth();
      int height = box.getHeight();
      if (width >= minWidth && 
          width <= maxWidth && 
          height >= minHeight && 
          height <= maxHeight) {
        g.setColor(Color.white);
        g.drawOval(pos.getX(), pos.getY(), 2, 2);
        String info = Integer.toString(pos.getX()) + "/" + Integer.toString(pos.getY());
        g.drawString(info, box.getMin().getX(), box.getMin().getY() - 1);
        g.setColor(Color.yellow);
      } else {
        g.setColor(Color.gray);
      }
      g.drawRect(box.getMin().getX(), box.getMin().getY(), box.getWidth(), box.getHeight());
    }
    g.setColor(Color.yellow);
    g.drawString("   t: " + fs.getFrameNumber(), 2, 26);
    g.drawString("objs: " + num, 2, 36);
  }
  
  private class OverlapImageRun implements ComputationRun {

    OCLSignal SIG_done = signals.get(Signal.DONE_COMPUTE_OVERLAP);
    CLKernel imageAndK = ocl.programs().getKernel("objects", "image_and");
    
    @Override
    public void launch(CLQueue queue) {
      imageAndK.setArgs(fgBuffer.current(), fgBuffer.last(), overlap);
      imageAndK.enqueueNDRange(queue, workDim);
    }

    @Override
    public void land() {
      overlapLabeler.doLabels();
      ocl.castSignal(SIG_done);
    }
  }
  
  private class CorrelationRun implements ComputationRun {
    
    OCLSignal SIG_done = signals.get(Signal.DONE_CORRELATION);
    CLKernel gatherLabelPairsK = ocl.programs().getKernel("objects", "gather_label_pairs");
    CLIntBuffer addresses = overlapLabeler.getIdBuffer();
    IntBuffer pairsH;
    int[] pairsWorkDim = new int[1];

    @Override
    public void launch(CLQueue queue) {
      ocl.utils().setValues(0, (int)label_pairs.getElementCount(), label_pairs, 0);
      // gather pairs
      if (overlapLabeler.getNumBlobs() > 0) {
        gatherLabelPairsK.setArgs(addresses, labels_current, labels_last, label_pairs, overlapLabeler.getNumBlobs(), Constants.pairsBufferLength);    // TODO length / 2 !!
        pairsWorkDim[0] = overlapLabeler.getNumBlobs();
        gatherLabelPairsK.enqueueNDRange(queue, pairsWorkDim);
        pairsH = label_pairs.read(queue);
      }
      
      // copy label buffer
      labels_current.copyTo(queue, 0, labels_current.getElementCount(), labels_last, 0);
    }

    @Override
    public void land() {
//      int num_corr = overlapLabeler.getNumBlobs();
//      if (num_corr > 0) {
//        pairsH.get(pairs);                        // TODO optimize: get only num_corr pairs instead of the whole buffer;
//        for (int i = 0; i < num_corr; i++) {
//          int idx = 2*i;
//          int current = pairs[idx];
//          int last = pairs[++idx];
//          System.out.append("  " + last + " > " + current);
//          
//        }
//      }
//      System.out.println("\n-------------------------------------------------------");
      ocl.castSignal(SIG_done);
    }
  }
  
  private class VisualizationRun implements ComputationRun {
    
    OCLSignal SIG_done = signals.get(Signal.DONE_VISUAL);
    CLKernel copyRedTintK = ocl.programs().getKernel("objects", "copy_red_tint");
    CLImage2D input = fsp.getFrameSource().getImage();

    @Override
    public void launch(CLQueue queue) {
      copyRedTintK.setArgs(input, fgs.getForegroundMap(), labels_current, visual, workDim[0]);
      copyRedTintK.enqueueNDRange(queue, workDim);
    }

    @Override
    public void land() {
      ocl.castSignal(SIG_done);
    }
  }
}
