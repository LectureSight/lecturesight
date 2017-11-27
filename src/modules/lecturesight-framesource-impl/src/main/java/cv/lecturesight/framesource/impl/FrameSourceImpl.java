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
package cv.lecturesight.framesource.impl;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.image.BufferedImage;
import java.nio.Buffer;

class FrameSourceImpl implements FrameSource {

  String type;
  FrameGrabber frameGrabber;
  FrameUploader uploader;
  CLImage2D lastImage;

  // frame information
  long frameNumber = 0L;
  final int FPS_SAMPLES = 30;
  long lastFrame;
  double[] frameTime = new double[FPS_SAMPLES];
  int sample_i = 0;

  int min_frame_duration = 0;

  public FrameSourceImpl(String type, FrameGrabber frameGrabber, FrameUploader loader, int maxfps) {
    this.type = type;
    this.frameGrabber = frameGrabber;
    this.uploader = loader;

    min_frame_duration = (maxfps > 0) ? (1000 / maxfps) : 0;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public OCLSignal getSignal() {
    return uploader.getSignal();
  }

  @Override
  public CLImage2D getImage() {
    return uploader.getOutputImage();
  }

  public CLImage2D getLastImage() {
    return uploader.getLastOutputImage();
  }

  @Override
  public CLImage2D getRawImage() {
    return uploader.getRawOutputImage();
  }

  @Override
  public void captureFrame() throws FrameSourceException {
    try {

      long now = System.currentTimeMillis();
      if (now < (lastFrame + min_frame_duration)) {
        Thread.sleep(lastFrame + min_frame_duration - now);
      }

      Buffer buf = frameGrabber.captureFrame();
      if (buf != null) {
        lastImage = uploader.getOutputImage();
        uploader.upload(buf);

        frameNumber++;
        frameTime[sample_i++] = 1000.0 / (double) (System.currentTimeMillis() - lastFrame);
        lastFrame = System.currentTimeMillis();
        if (sample_i == FPS_SAMPLES) {
          sample_i = 0;
        }
      } else {
        throw new IllegalStateException("Underlying frame grabber did not provide data.");
      }
    } catch (Exception e) {
      throw new FrameSourceException("Unable to capture frame.", e);
    }
  }

  @Override
  public int getWidth() {
    return frameGrabber.getWidth();
  }

  @Override
  public int getHeight() {
    return frameGrabber.getHeight();
  }

  public long getFrameNumber() {
    return frameNumber;
  }

  @Override
  public BufferedImage getImageHost() {
    return uploader.getOutputImageHost();
  }

  @Override
  public double getFPS() {
    double sum = 0.0;
    for (int i = 0; i < FPS_SAMPLES; i++) {
      sum += (double) frameTime[i];
    }
    return sum / FPS_SAMPLES;
  }
}
