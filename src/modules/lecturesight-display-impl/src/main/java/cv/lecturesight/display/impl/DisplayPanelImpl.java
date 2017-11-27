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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DisplayPanelImpl extends DisplayPanel implements DisplayListener, HierarchyListener {

  private DisplayImpl display;
  private BufferedImage gpuImage;
  private BufferedImage renderBuffer;
  private Dimension size;

  public DisplayPanelImpl(DisplayImpl display) {
    this.display = display;
    this.size = display.getSize();
    gpuImage = display.getImage();
    renderBuffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
  }

  @Override
  public Dimension getImageDimension() {
    return new Dimension((int) size.getWidth(), (int) size.getHeight());
  }

  @Override
  public void paint(Graphics componentG) {
    display.addListener(this);
    // image = display.getImage();    // is this necessary?
    Graphics renderBufferG = renderBuffer.getGraphics();

    // draw gpu image to render buffer
    renderBufferG.drawImage(gpuImage, 0, 0, this);

    // draw custom graphics (if any) to render buffer
    if (hasCustomRenderer()) {
      getCustomRenderer().render(renderBufferG);
    }

    // draw render buffer to component
    int x = 0;
    int y = 0;
    if (size.getWidth() < getWidth() || size.getHeight() < getHeight()) {
      x = (getWidth() - (int) size.getWidth()) / 2;
      y = (getHeight() - (int) size.getHeight()) / 2;
    }
    componentG.setColor(Color.black);
    componentG.drawRect(0, 0, getWidth(), getHeight());
    componentG.drawImage(renderBuffer, x, y, this);

    // save render buffer if recording
    if (isRecording()) {
      File filepath = new File(getRecordingDir().getAbsolutePath() + File.separator + "frame-" + Long.toString(display.getCurrentFrame()) + ".png");

      try {
        ImageIO.write(renderBuffer, "PNG", filepath);
      } catch (IOException ex) {
        // TODO add error logging here
      }
    }
    // TODO dispose renderBufferG here??
  }

  public Dimension getPrefferedSize() {
    return new Dimension((int) size.getWidth(), (int) size.getHeight());
  }

  @Override
  public void imageUpdated(BufferedImage image) {
    this.gpuImage = image;
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
      display.addListener(this);
    } else {
      display.removeListener(this);
    }
  }
}
