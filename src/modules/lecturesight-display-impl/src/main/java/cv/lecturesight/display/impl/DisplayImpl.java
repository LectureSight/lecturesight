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

import cv.lecturesight.display.Display;
import cv.lecturesight.display.DisplayListener;
import cv.lecturesight.display.DisplayPanel;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;

import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLQueue;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DisplayImpl implements Display {

  OpenCLService ocl;
  OCLSignal trigger;
  OCLSignal sig_DONE;
  ComputationRun workingRun;
  private int width;
  private int height;
  private CLImage2D imageCL;
  private BufferedImage imageHost;
  private boolean active = false;
  private Set<DisplayListener> listeners = new HashSet<DisplayListener>();
  long currentFrame = 0;      // incremented each time image data is updated

  public DisplayImpl(OpenCLService ocl, OCLSignal trigger, CLImage2D imageCL) {
    this.ocl = ocl;
    this.trigger = trigger;
    this.imageCL = imageCL;
    this.width = (int) imageCL.getWidth();
    this.height = (int) imageCL.getHeight();
    this.imageHost = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
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

  @Override
  public Dimension getSize() {
    return new Dimension(width, height);
  }

  @Override
  public long getCurrentFrame() {
    return currentFrame;
  }

  private class WorkingRun implements ComputationRun {

    @Override
    public void launch(CLQueue queue) {
      imageHost = imageCL.read(queue);
    }

    @Override
    public void land() {
      if (currentFrame == Long.MAX_VALUE) {
        currentFrame = 0;
      } else {
        currentFrame++;
      }
      ocl.castSignal(sig_DONE);
      notifyListeners();
    }
  }
}
