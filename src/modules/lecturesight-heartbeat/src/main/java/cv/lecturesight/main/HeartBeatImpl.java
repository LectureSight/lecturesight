package cv.lecturesight.main;

import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.OCLSignalBarrier;
import cv.lecturesight.opencl.api.Triggerable;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 */
@Component(name = "lecturesight.main", immediate = true)
@Service
public class HeartBeatImpl implements HeartBeat {

  static final String PROPKEY_LISTENTO = "heartbeat.listens.to";
  static final String PROPKEY_AUTOSTART = "heartbeat.autostart";
  private Log log = new Log("Heartbeat");
  @Reference
  private Configuration config;
  @Reference
  private OpenCLService ocl;
  @Reference
  private FrameSourceProvider fsp;
  private FrameSource fsrc;
  private OCLSignal[] listenTo;
  private OCLSignalBarrier barrier;
  private OCLSignal sig_BEGINFRAME;
  private boolean ready = false;
  private int iterationsToRun = 0;

  protected void activate(ComponentContext cc) throws Exception {
    sig_BEGINFRAME = ocl.getSignal("BEGIN-FRAME");
    log.info("Activated.");
    if (config.getBoolean(PROPKEY_AUTOSTART)) {
      init();
      log.info("Autostart");
      iterationsToRun = -1;
      nextFrame();    // start system by getting first frame
    }
  }
  
  protected void deactivate(ComponentContext cc) throws Exception {
    if (ready) {
      deinit();
    }
    log.info("Deactivated");
  }

  private void nextFrame() {
    try {
      ocl.castSignal(sig_BEGINFRAME);
      fsrc.captureFrame();
    } catch (FrameSourceException e) {
      log.error("Unable to capture frame", e);
    }
  }

  @Override
  public void init() {
    fsrc = fsp.getFrameSource();                         // get configured FrameSource

    // get list of signals we are listening to
    String[] listenSignals = config.getList(PROPKEY_LISTENTO);
    if (listenSignals.length < 1) {
      log.warn("No signals to listen to.");
      return;
    }
    listenTo = new OCLSignal[listenSignals.length];
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < listenSignals.length; i++) {
      String signame = listenSignals[i].trim();
      sb.append(signame).append(" ");
      listenTo[i] = ocl.getSignal(signame);
    }
    log.info("Listening to: " + sb.toString());

    // create signal barrier
    barrier = ocl.createSignalBarrier(listenTo);
    ocl.registerTriggerable(barrier.getSignal(),
            new Triggerable() {

              @Override
              public void triggered(OCLSignal signal) {   // when all signals have arrived
                if (iterationsToRun != 0) {
                  nextFrame();                              // accquire next frame
                  if (iterationsToRun > 0) {
                    iterationsToRun--;
                  }
                }
              }
            });

    ready = true;
    log.info("Initialized");
  }

  @Override
  public void deinit() {
    iterationsToRun = 0;
    for (int i = 0; i < listenTo.length; i++) {
      ocl.unregisterTriggerable(listenTo[i], barrier);
    }
    fsrc = null;
    ready = false;
    log.info("Stopped");
  }

  @Override
  public void step(int i) {
    if (ready) {
      iterationsToRun = i;
      nextFrame();
    } else {
      throw new IllegalStateException("Cannot step, HeartBeat not initialized");
    }
  }

  @Override
  public void go() {
    if (ready) {
      iterationsToRun = -1;
      nextFrame();
      log.info("Started");
    } else {
      throw new IllegalStateException("Cannot start, HeartBeat not initialized");
    }
  }

  @Override
  public void stop() {
    iterationsToRun = 0;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  public boolean isRunning() {
    return !(iterationsToRun == 0);
  }
}
