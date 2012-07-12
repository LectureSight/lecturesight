package cv.lecturesight.ui.impl;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.ui.CustomRenderer;
import cv.lecturesight.ui.DisplayWindow;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DisplayWindowImpl implements DisplayWindow, WindowListener {

  OpenCLService ocl;
  OCLSignal trigger, SIG_done;
  ComputationRun workingRun;
  private CLImage2D image;
  private CustomRenderer renderer = null;
  private JFrame frame = new JFrame();
  private JLabel label = new JLabel();
  private boolean active = false;

  public DisplayWindowImpl(String title, CLImage2D image) {
    frame.setTitle(title);
    this.image = image;
    initComponents();
    initRun();
  }

  public DisplayWindowImpl(String title, CLImage2D image, CustomRenderer renderer) {
    frame.setTitle(title);
    this.image = image;
    this.renderer = renderer;
    initComponents();
    initRun();
  }

  private void initComponents() {
    frame.setSize((int) image.getWidth(), (int) image.getHeight());
    frame.getContentPane().add(label);
    frame.setResizable(false);
    frame.addWindowListener(this);
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
  public void setTitle(String title) {
    frame.setTitle(title);
  }

  @Override
  public String getTitle() {
    return frame.getTitle();
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
}
