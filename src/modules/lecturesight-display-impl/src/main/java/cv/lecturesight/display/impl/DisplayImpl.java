package cv.lecturesight.display.impl;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.display.Display;
import cv.lecturesight.display.DisplayListener;
import cv.lecturesight.display.DisplayPanel;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DisplayImpl implements Display {

  OpenCLService ocl;
  OCLSignal trigger, sig_DONE;
  ComputationRun workingRun;
  private CLImage2D imageCL;
  private BufferedImage imageHost;
  private boolean active = false;
  private Set<DisplayListener> listeners = new HashSet<DisplayListener>();

  public DisplayImpl(OpenCLService ocl, OCLSignal trigger, CLImage2D imageCL) {
    this.ocl = ocl;
    this.trigger = trigger;
    this.imageCL = imageCL;
    this.imageHost = new BufferedImage((int) imageCL.getWidth(), (int) imageCL.getHeight(), BufferedImage.TYPE_INT_RGB);
    sig_DONE = ocl.getSignal("Display-" + UUID.randomUUID().toString());
    workingRun = new WorkingRun();
  }

  @Override
  public OCLSignal getSignal() {
    return sig_DONE;
  }

  @Override
  public void activate() {
    if (!active) {
      ocl.registerLaunch(trigger, workingRun);
      active = true;
    }
  }

  @Override
  public void deactivate() {
    if (active) {
      ocl.unregisterLaunch(trigger, workingRun);
      active = false;
    }
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public BufferedImage getImage() {
    return imageHost;
  }

  @Override
  public DisplayPanel getDisplayPanel() {
    return new DisplayPanelImpl(this);
  }

  @Override
  public void addListener(DisplayListener listener) {
    listeners.add(listener);
    activate();
  }

  @Override
  public void removeListener(DisplayListener listener) {
    listeners.remove(listener);
    if (listeners.isEmpty()) {
      deactivate();
    }
  }

  private void notifyListeners() {
    for (DisplayListener l : listeners) {
      l.imageUpdated(imageHost);
    }
  }

  private class WorkingRun implements ComputationRun {

    @Override
    public void launch(CLQueue queue) {
      imageHost = imageCL.read(queue);
    }

    @Override
    public void land() {
      ocl.castSignal(sig_DONE);
      notifyListeners();
    }
  }
}
