package cv.lecturesight.display.impl;

import cv.lecturesight.display.Display;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DisplayWindow implements WindowListener {
  
  private JFrame frame = new JFrame();
  private JLabel displayLabel = new JLabel();
  
  public DisplayWindow(String title, Display display) {
    
  }
  
  private void initComponents() {
    frame.setSize((int) image.getWidth(), (int) image.getHeight());
    frame.getContentPane().add(displayLabel);
    frame.setResizable(false);
    frame.addWindowListener(this);
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
