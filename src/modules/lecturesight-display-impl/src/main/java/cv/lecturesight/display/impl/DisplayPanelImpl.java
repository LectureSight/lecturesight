package cv.lecturesight.display.impl;

import cv.lecturesight.display.DisplayListener;
import cv.lecturesight.display.DisplayPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;

public class DisplayPanelImpl extends DisplayPanel implements DisplayListener, HierarchyListener {

  private DisplayImpl display;
  private BufferedImage image;

  public DisplayPanelImpl(DisplayImpl display) {
    this.display = display;
    image = display.getImage();
  }

  @Override
  public void paint(Graphics g) {
    image = display.getImage();

    int x = 0, y = 0;

    if (image.getWidth() < getWidth() || image.getHeight() < getHeight()) {
      x = (getWidth() - image.getWidth()) / 2;
      y = (getHeight() - image.getHeight()) / 2;
    }

    g.drawImage(image, x, y, this);

    if (hasCustomRenderer()) {
      getCustomRenderer().render(g.create(x, y, image.getWidth(), image.getHeight()));
    }
  }

  public Dimension getPrefferedSize() {
    return new Dimension(image.getWidth(), image.getHeight());
  }

  @Override
  public void imageUpdated(BufferedImage image) {
    this.image = image;
    repaint();
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
      if (isShowing()) {
        display.addListener(this);
      } else {
        display.removeListener(this);
      }
    }
  }
}
