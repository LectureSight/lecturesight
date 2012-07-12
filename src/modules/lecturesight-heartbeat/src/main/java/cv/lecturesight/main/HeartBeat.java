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
import org.osgi.service.component.ComponentContext;

/** Implementation of Service API
 *
 * @scr.component name="lecturesight.main" immediate="true" 
 * @scr.service interface="cv.lecturesight.main.HeartBeat"
 */
public class HeartBeat {

  static final String PROPKEY_LISTENTO = "heartbeat.listens.to";
  private Log log = new Log("Heartbeat");
  /** @scr.reference */
  private Configuration config;
  /** @scr.reference */
  private OpenCLService ocl;
  /** @scr.reference */
  private FrameSourceProvider fsp;
  private FrameSource fsrc;
  private OCLSignalBarrier barrier;
  private OCLSignal SIG_BEGINFRAME;

  protected void activate(ComponentContext cc) throws Exception {

    fsrc = fsp.getFrameSource();                         // get configured FrameSource
    SIG_BEGINFRAME = ocl.getSignal("BEGIN-FRAME");

    // get list of signals we are listening to
    String[] listenSignals = config.getList(PROPKEY_LISTENTO);
    if (listenSignals.length < 1) {
      log.warn("No signals to listen to.");
      return;
    }
    OCLSignal[] signals = new OCLSignal[listenSignals.length];
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < listenSignals.length; i++) {
      String signame = listenSignals[i].trim();
      sb.append(signame).append(" ");
      signals[i] = ocl.getSignal(signame);
    }
    log.info("Listening to: " + sb.toString());

    // create signal barrier
    barrier = ocl.createSignalBarrier(signals);
    ocl.registerTriggerable(barrier.getSignal(),
            new Triggerable() {

              @Override
              public void triggered(OCLSignal signal) {   // when all signals have arrived
                nextFrame();                              // accquire next frame
              }
            });

    log.info("Activated. Starting system.");

    nextFrame();    // start system by getting first frame
  }

  private void nextFrame() {
    try {
      ocl.castSignal(SIG_BEGINFRAME);
      fsrc.captureFrame();
    } catch (FrameSourceException e) {
      log.error("Unable to capture frame", e);
    }
  }
}
