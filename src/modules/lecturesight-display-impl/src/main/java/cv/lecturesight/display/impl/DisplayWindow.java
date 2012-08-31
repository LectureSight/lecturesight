package cv.lecturesight.display.impl;

import cv.lecturesight.display.Display;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

public class DisplayWindow implements WindowListener {
  
  String title = "";
  Display display;
  JFrame frame;
  
  public DisplayWindow(String title, Display display) {
    this.title = title;
    this.display = display;
    initComponents();
  }
  
  private void initComponents() {
    frame = new JFrame(title);
    frame.getContentPane().add(display.getDisplayPanel());
    frame.setResizable(false);
    frame.addWindowListener(this);
  }
  
  // TODO deactivate image fatching when made invisible
  public void setVisible(boolean b) {
    frame.setVisible(b);  
  }

  @Override
  public void windowOpened(WindowEvent e) {
  }

  @Override
  public void windowClosing(WindowEvent e) {
  }

  @Override
  public void windowClosed(WindowEvent e) {
  }

  @Override
  public void windowIconified(WindowEvent e) {
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
  }

  @Override
  public void windowActivated(WindowEvent e) {
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
  }
  
}
