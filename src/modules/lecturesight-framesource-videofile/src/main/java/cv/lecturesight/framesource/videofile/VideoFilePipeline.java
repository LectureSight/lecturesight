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
package cv.lecturesight.framesource.videofile;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameSourceException;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.PadDirection;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.Structure;
import org.gstreamer.elements.AppSink;

/**
 *
 * @author wsmirnow
 */
public class VideoFilePipeline implements FrameGrabber {

  private Element src;
  private Element decodebin;
  private Element colorspace;
  private Element capsfilter;
  private AppSink appsink;
  private Pipeline pipeline;
  private int width, height;    // the video frame size
  private ByteBuffer lastFrame;
  private boolean playing;

  public VideoFilePipeline(File videoFile) throws UnableToLinkElementsException, FrameSourceException {
    createElements();
    setElementProperties(videoFile);
    linkElements();
    playing = true;
    pipeline.play();
    getVideoFrameSize();
  }

  private void createElements() {
    src = ElementFactory.make("filesrc", null);
    decodebin = ElementFactory.make("decodebin", null);
    colorspace = ElementFactory.make("ffmpegcolorspace", null);
    capsfilter = ElementFactory.make("capsfilter", null);
    appsink = (AppSink) ElementFactory.make("appsink", null);

    pipeline = new Pipeline();

    pipeline.add(src);
    pipeline.add(decodebin);
    pipeline.add(colorspace);
    pipeline.add(capsfilter);
    pipeline.add(appsink);
  }

  private void setElementProperties(File video) {
    src.set("location", video.getAbsolutePath());
    Caps caps = Caps.fromString("video/x-raw-rgb");
    capsfilter.set("caps", caps);
    appsink.setCaps(caps);
    appsink.set("async", "true");
    appsink.set("sync", "false");
    appsink.set("drop", "false");
    appsink.set("max-buffers", "5");
  }

  private void linkElements() throws UnableToLinkElementsException {
    decodebin.connect(new Element.PAD_ADDED() {

      @Override
      public void padAdded(Element element, Pad pad) {
        element.link(colorspace);
      }
    });

    if (!src.link(decodebin)) {
      throw new UnableToLinkElementsException(src, decodebin);
    }

    Pad p = new Pad(null, PadDirection.SRC);
    decodebin.addPad(p);

    if (!colorspace.link(capsfilter)) {
      throw new UnableToLinkElementsException(colorspace, capsfilter);
    }
    if (!capsfilter.link(appsink)) {
      throw new UnableToLinkElementsException(capsfilter, appsink);
    }
  }

  /**
   * Called by factory when service is stopped.
   */
  void stop() {
      playing = false;
      pipeline.setState(State.NULL);
  }

  /**
   * Returns next frame as ByteBuffer.
   * @return frame as ByteBuffer
   */
  @Override
  public ByteBuffer captureFrame() throws FrameSourceException {
    if (!appsink.isEOS()) {
      Buffer buf = appsink.pullBuffer();
      if (buf == null) {
        System.out.println("Buffer is NULL!!");
      }
      lastFrame = buf.getByteBuffer();
    } else {
      if (lastFrame == null) {
        throw new FrameSourceException("Stream is EOS and no previously captured frame availabel.");
      }
      if (playing) {  // rewind if video has ended
        pipeline.seek(0, TimeUnit.SECONDS);
      }
    }
    return lastFrame;
  }

  private void getVideoFrameSize() throws FrameSourceException {
    try {
      Buffer buf = appsink.pullPreroll();
      Structure str = buf.getCaps().getStructure(0);
      width = str.getInteger("width");
      height = str.getInteger("height");
    } catch (Exception e) {
      throw new FrameSourceException("Could not determine frame dimensions: " + e.getMessage());
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
}
