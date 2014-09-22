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
package cv.lecturesight.framesource.gst;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameSourceException;
import java.nio.ByteBuffer;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.Structure;
import org.gstreamer.elements.AppSink;

public class GStreamerFrameGrabber implements FrameGrabber {

  private String definition;
  private final Pipeline pipeline;
  private int width, height;
  private ByteBuffer lastFrame;
  private AppSink appsink;

  public GStreamerFrameGrabber(String pipelineDef) throws FrameSourceException {
    definition = pipelineDef;
    try {
      pipeline = createPipeline(pipelineDef);
    } catch (Exception e) {
      throw new FrameSourceException("Failed to create pipeline.", e);
    }
    start();
    getVideoFrameSize();
  }

  private Pipeline createPipeline(String pipelineDef) throws IllegalStateException {

    // instantiate user provided pipeline segment
    Pipeline pipe = Pipeline.launch(pipelineDef);

    // find most downstream element in user pipeline
    Element last = pipe.getElementsSorted().get(0);

    // attach color space and caps filter and appsink
    Element colorspace = createElement("ffmpegcolorspace", "ffmpegcolorspace");
    addToPipeline(pipe, colorspace);
    linkElements(last, colorspace);
    Caps caps = Caps.fromString("video/x-raw-rgb");
    Element capsfilter = createElement("capsfilter", "capsfilter");
    capsfilter.set("caps", caps);
    addToPipeline(pipe, capsfilter);
    linkElements(colorspace, capsfilter);
    appsink = (AppSink) createElement("appsink", "appsink");
    appsink.setCaps(caps);
    appsink.set("async", "true");
    appsink.set("sync", "false");
    appsink.set("drop", "true");
    appsink.set("max-buffers", "5");
    addToPipeline(pipe, appsink);
    linkElements(capsfilter, appsink);

    return pipe;
  }

  private static Element createElement(String fname, String ename) throws IllegalStateException {
    Element elm = ElementFactory.make(fname, ename);
    if (elm == null) {
      throw new IllegalStateException("Failed to create element: " + fname);
    }
    return elm;
  }

  private static void addToPipeline(Pipeline pipeline, Element elm) throws IllegalStateException {
    if (!pipeline.add(elm)) {
      throw new IllegalStateException("Failed to add " + elm.getName() + " to pipeline!");
    }
  }

  private static void linkElements(Element elm, Element toLink) throws IllegalStateException {
    if (!elm.link(toLink)) {
      throw new IllegalStateException("Failed to link " + toLink.getName() + " to " + elm.getName());
    }
  }

  void start() {
    pipeline.play();
  }

  void stop() {
    pipeline.setState(State.NULL);
  }

  private void getVideoFrameSize() throws FrameSourceException {
    try {
      org.gstreamer.Buffer buf = appsink.pullPreroll();
      Structure str = buf.getCaps().getStructure(0);
      width = str.getInteger("width");
      height = str.getInteger("height");
    } catch (Exception e) {
      throw new FrameSourceException("Could not determine frame dimensions. ", e);
    }
  }

  @Override
  public ByteBuffer captureFrame() throws FrameSourceException {
    if (!appsink.isEOS()) {
      org.gstreamer.Buffer buf = appsink.pullBuffer();
      if (buf == null) {
        System.out.println("Buffer is NULL!!");
      }
      lastFrame = buf.getByteBuffer();
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(").append("pipeline=\"").append(definition).append("\"")
            .append(" size=").append(width).append("x").append(height).append(")");
    return sb.toString();
  }
}
