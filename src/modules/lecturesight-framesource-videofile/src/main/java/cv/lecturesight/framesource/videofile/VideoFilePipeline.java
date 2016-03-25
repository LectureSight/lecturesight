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
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.GstObject;
import org.freedesktop.gstreamer.Pad;
import org.freedesktop.gstreamer.PadDirection;
import org.freedesktop.gstreamer.PadLinkReturn;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.State;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.AppSink;

import org.pmw.tinylog.Logger;

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

  private int width, height;    // the video frame size
  private ByteBuffer lastFrame;
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

//    pipeline.debugToDotFile(Pipeline.DEBUG_GRAPH_SHOW_ALL, "ls-framesource", true);
    if (!playing) {
      stop();
      throw new FrameSourceException(
              String.format("Pipeline not running after %d sec", (int)(waitPlaying / 10)));
    }

    try {
      Structure structure = appsink.getSinkPads().get(0)
              .getNegotiatedCaps().getStructure(0);
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
	  if (pad.link(peerPad) != PadLinkReturn.OK) {
            Logger.error("Can't link decodebin to videoconvert");
          } else {
            elementsLinked = true;
          }
        }
      }
    });

    if (!src.link(decodebin)) {
      throw new UnableToLinkElementsException(src, decodebin);
    }

    // Pad p = new Pad(null, PadDirection.SRC);
    // decodebin.addPad(p);

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
        pipeline.seek(0, TimeUnit.SECONDS);
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
      Buffer buffer = appsink.pullSample().getBuffer();
      if (buffer == null) {
        throw new FrameSourceException("Can't grab video frame.");
      }
      lastFrame = buffer.map(false);
    } else {
      if (lastFrame == null) {
        throw new FrameSourceException("Stream is EOS and no previously captured frame availabel.");
      }
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
