package cv.lecturesight.videoanalysis.change.impl;

import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;

public class DisplayPanel extends JLabel implements HierarchyListener {
  
  BufferedImage image;
  
  public DisplayPanel() {
    super();
    
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(50, 20);
  }
  
  @Override
  public void addNotify() {
    super.addNotify();
    addHierarchyListener(this);
  }

  @Override
  public void removeNotify() {
    removeHierarchyListener(this);
    super.removeNotify();
  }

  @Override
  public void hierarchyChanged(HierarchyEvent e) {
    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
      System.out.println("Is me showing? " + isShowing());
    }
  }
}
