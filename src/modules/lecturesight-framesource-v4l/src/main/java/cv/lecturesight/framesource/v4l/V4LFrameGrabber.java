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
package cv.lecturesight.framesource.v4l;

import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import cv.lecturesight.framesource.FrameGrabber.PixelFormat;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.util.Log;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class V4LFrameGrabber implements cv.lecturesight.framesource.FrameGrabber, CaptureCallback {

  @Override
  public void exceptionReceived(V4L4JException vlje) {
    throw new UnsupportedOperationException(
      new FrameSourceException("Could not capture frame from " + device.getDevicefile() + ": " + vlje.getMessage())); 
  }

  private Log log;
  VideoDevice device;
  int width, height, standard, channel, quality;
  private FrameGrabber grabber;
  private ByteBuffer frameBuffer;

  V4LFrameGrabber(VideoDevice device, int frameWidth, int frameHeight, int videoStandard,
    int videoChannel, int videoQuality) throws FrameSourceException {
    this.device = device;
    this.width = frameWidth;
    this.height = frameHeight;
    this.standard = videoStandard;
    this.channel = videoChannel;
    this.quality = videoQuality;

    try {
      log = new Log(device.getDeviceInfo().getName());
      grabber = device.getRGBFrameGrabber(width, height, channel, standard);
      byte[] barr = new byte[grabber.getWidth() * grabber.getHeight() * 3];
      frameBuffer = ByteBuffer.wrap(barr);
      grabber.setCaptureCallback(this);
      grabber.startCapture();
    } catch (V4L4JException e) {
      throw new FrameSourceException("Failed to initialize FrameGrabber on device " + device.getDevicefile());
    }
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public PixelFormat getPixelFormat() {
    return PixelFormat.RGB_8BIT;
  }

  @Override
  public void nextFrame(VideoFrame frame) {
    frameBuffer = ByteBuffer.wrap(frame.getBytes());
    frame.recycle();
  }
 
  @Override
  public Buffer captureFrame() throws FrameSourceException {
      if (frameBuffer != null) {
          return frameBuffer;
      } else {
          throw new FrameSourceException("Could not capture frame from " + device.getDevicefile());
      }
  }

  void shutdown() {
    log.info("Shutting down");
    grabber.stopCapture();
    device.releaseFrameGrabber();
    device.release();
  }
  
  @Override
  public void finalize() throws Throwable {
    super.finalize();
    shutdown();
  }
}
