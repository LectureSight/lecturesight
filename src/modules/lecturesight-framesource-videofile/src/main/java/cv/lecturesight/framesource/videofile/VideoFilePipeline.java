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

import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.GstObject;
import org.freedesktop.gstreamer.Pad;
import org.freedesktop.gstreamer.PadDirection;
import org.freedesktop.gstreamer.PadLinkException;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.State;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.AppSink;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.nio.ByteBuffer;

/**
 *
 * @author wsmirnow
 */
public class VideoFilePipeline implements FrameGrabber {

  private Element src;
  private Element decodebin;
  private Element videoconvert;
  private Element capsfilter;
  private AppSink appsink;
  private Pipeline pipeline;

  // the video frame size
  private int width;
  private int height;

  private ByteBuffer lastFrame;
  private Buffer lastBuf;
  private Sample lastSam;
  private boolean elementsLinked;
  private boolean error;
  private boolean playing;

  public VideoFilePipeline(File videoFile) throws UnableToLinkElementsException, FrameSourceException {
    createElements();
    setElementProperties(videoFile);
    linkElements();
    setupBus();
    pipeline.play();

    int waitPlaying = 0;
    while (!playing && waitPlaying < 150) { // wait 15sec
      waitPlaying++;
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) { }

      if (error) {
        throw new FrameSourceException("Gstreamer Pipeline doesn't come up.");
      }

      if (elementsLinked && State.PLAYING == pipeline.getState(500)) {
        playing = true;
      }
    }

    // Write pipeline .dot file. Requires gstreamer configured with "--gst-enable-gst-debug"
    //  and the environment variable GST_DEBUG_DUMP_DOT_DIR set to a basepath (e.g. /tmp)
    if (Logger.getLevel(VideoFilePipeline.class) == Level.DEBUG) {
      pipeline.debugToDotFile(Bin.DebugGraphDetails.SHOW_ALL, "ls-framesource");
    }

    if (!playing) {
      stop();
      throw new FrameSourceException(
                                     String.format("Pipeline not running after %d sec", (int)(waitPlaying / 10)));
    }

    try {
      Structure structure = appsink.getSinkPads().get(0)
      .getCurrentCaps().getStructure(0);
      width = structure.getInteger("width");
      height = structure.getInteger("height");
    } catch (Exception e) {
      stop();
      throw new FrameSourceException("Could not determine frame dimensions: " + e.getMessage());
    }
  }

  private void createElements() {
    src = ElementFactory.make("filesrc", null);
    decodebin = ElementFactory.make("decodebin", null);
    videoconvert = ElementFactory.make("videoconvert", null);
    capsfilter = ElementFactory.make("capsfilter", null);
    appsink = (AppSink) ElementFactory.make("appsink", null);

    pipeline = new Pipeline();

    pipeline.add(src);
    pipeline.add(decodebin);
    pipeline.add(videoconvert);
    pipeline.add(capsfilter);
    pipeline.add(appsink);
  }

  private void setElementProperties(File video) {
    src.set("location", video.getAbsolutePath());
    Caps caps = Caps.fromString("video/x-raw,format=RGB");
    capsfilter.set("caps", caps);
    appsink.setCaps(caps);
    appsink.set("async", "false");
    appsink.set("sync", "true");
    appsink.set("drop", "true");
    appsink.set("max-buffers", "5");
  }

  private void linkElements() throws UnableToLinkElementsException {
    elementsLinked = false;
    decodebin.connect(new Element.PAD_ADDED() {

      @Override
      public void padAdded(Element element, Pad pad) {
        Pad peerPad = videoconvert.getStaticPad("sink");
        if (pad.getDirection() == PadDirection.SRC) {
          try {
            pad.link(peerPad);
            elementsLinked = true;
          } catch (PadLinkException e) {
            Logger.error("Can't link decodebin to videoconvert");
          }
        }
      }
    });

    if (!src.link(decodebin)) {
      throw new UnableToLinkElementsException(src, decodebin);
    }
    if (!videoconvert.link(capsfilter)) {
      throw new UnableToLinkElementsException(videoconvert, capsfilter);
    }
    if (!capsfilter.link(appsink)) {
      throw new UnableToLinkElementsException(capsfilter, appsink);
    }
  }

  private void setupBus() throws FrameSourceException {
    Bus bus = pipeline.getBus();
    if (bus == null) {
      throw new FrameSourceException("Can't get Gstreamer Bus from Pipeline");
    }

    bus.connect(new Bus.EOS() {

      @Override
      public void endOfStream(GstObject go) {
        Logger.info("Looping video file framesource");
        pipeline.stop();
        pipeline.play();
      }
    });

    error = false;
    bus.connect(new Bus.ERROR() {

      @Override
      public void errorMessage(GstObject source, int code, String message) {
        Logger.error("Gstreamer error from {} (error code: {}): {}\n", source, code, message);
        error = true;
        pipeline.stop();
      }
    });
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
      Sample sam = appsink.pullSample();
      Buffer buf = sam.getBuffer();
      if (buf != null) {
        lastFrame = buf.map(false);
        if (lastBuf != null) {
          // Free memory allocated for the previous buffer so we don't leak memory
          lastBuf.unmap();
          lastSam.dispose();
        }
        lastBuf = buf;
        lastSam = sam;
      } else {
        Logger.warn("Buffer is NULL!!");
      }
    } else {
      Logger.trace("appsink is EOS");
    }

    if (lastFrame == null) {
      throw new FrameSourceException("Stream is EOS and no previously captured frame available.");
    }

    return lastFrame;
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
