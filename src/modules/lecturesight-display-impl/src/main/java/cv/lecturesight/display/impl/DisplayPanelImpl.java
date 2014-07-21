/* Copyright (C) 2012 Benjamin Wulff
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package cv.lecturesight.display.impl;

import cv.lecturesight.display.DisplayListener;
import cv.lecturesight.display.DisplayPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;

public class DisplayPanelImpl extends DisplayPanel implements DisplayListener, HierarchyListener {

  private DisplayImpl display;
  private BufferedImage image;
  private Dimension size;

  public DisplayPanelImpl(DisplayImpl display) {
    this.display = display;
    this.size = display.getSize();
    image = display.getImage();
  }

  @Override
  public Dimension getImageDimension() {
    return new Dimension((int) size.getWidth(), (int) size.getHeight());
  }
  
  @Override
  public void paint(Graphics g) {
    //System.out.println("PAINT");
    display.addListener(this);
    image = display.getImage();

    int x = 0, y = 0;

    if (size.getWidth() < getWidth() || size.getHeight() < getHeight()) {
      x = (getWidth() - (int) size.getWidth()) / 2;
      y = (getHeight() - (int) size.getHeight()) / 2;
    }

    g.drawImage(image, x, y, this);

    if (hasCustomRenderer()) {
      getCustomRenderer().render(g.create(x, y, (int) size.getWidth(), (int) size.getHeight()));
    }
  }

  public Dimension getPrefferedSize() {
    return new Dimension((int) size.getWidth(), (int) size.getHeight());
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

  // TODO is this really the maximum of control we can get over the rendering state of the display??
  @Override
  public void hierarchyChanged(HierarchyEvent e) {
    if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) != 0
            && (e.getChanged().isVisible() || this.getParent().isShowing())) {

      //System.out.println("ACTIVATE DISPLAYLISTENER");
      display.addListener(this);
    } else {
      //System.out.println("DEACTIVATE DISPLAYLISTENER");
      display.removeListener(this);
    }
  }
}
