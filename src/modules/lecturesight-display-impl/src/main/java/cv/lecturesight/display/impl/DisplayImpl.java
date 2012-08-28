package cv.lecturesight.display.impl;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.display.CustomRenderer;
import cv.lecturesight.display.Display;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class DisplayImpl implements Display {

  OpenCLService ocl;
  OCLSignal trigger, SIG_done;
  ComputationRun workingRun;
  private CLImage2D image;
  private CustomRenderer renderer = null;

  public DisplayImpl(String title, CLImage2D image) {
    frame.setTitle(title);
    this.image = image;
    initComponents();
    initRun();
  }

  public DisplayImpl(String title, CLImage2D image, CustomRenderer renderer) {
    frame.setTitle(title);
    this.image = image;
    this.renderer = renderer;
    initComponents();
    initRun();
  }

  private void initRun() {
    workingRun = new ComputationRun() {

      BufferedImage hostImage;

      @Override
      public void launch(CLQueue queue) {
        hostImage = image.read(queue);
      }

      @Override
      public void land() {
        if (renderer != null) {
          renderer.render(hostImage.createGraphics());
        }
        label.setIcon(new ImageIcon(hostImage));
        ocl.castSignal(SIG_done);
      }
    };
  }

  @Override
  public OCLSignal getSignal() {
    return SIG_done;
  }

  @Override
  public void show() {
    activate();
    frame.setVisible(true);
  }

  @Override
  public void hide() {
    deactivate();
    frame.setVisible(false);
  }

  // TODO: synchronize
  private void activate() {
    if (!active) {
      ocl.registerLaunch(trigger, workingRun);
      active = true;
    }
  }

  // TODO: synchronize
  private void deactivate() {
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
  public void windowClosing(WindowEvent we) {
    deactivate();
  }

  @Override
  public void windowIconified(WindowEvent we) {
    deactivate();
  }

  @Override
  public void windowDeiconified(WindowEvent we) {
    activate();
  }

  // <editor-fold defaultstate="collapsed" desc="Unused WindowListener methods">
  @Override
  public void windowOpened(WindowEvent we) {
  }

  @Override
  public void windowClosed(WindowEvent we) {
  }

  @Override
  public void windowActivated(WindowEvent we) {
  }

  @Override
  public void windowDeactivated(WindowEvent we) {
  }
  // </editor-fold>

  @Override
  public BufferedImage getImage() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public JLabel getDisplayLabel() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
