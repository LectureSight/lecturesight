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
package cv.lecturesight.framesource.rtph264;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.util.Log;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.gstreamer.Gst;
import org.osgi.service.component.ComponentContext;

/**
 * RTP h.264 Streaming FrameSourceFactory
 *
 */
@Component(name = "lecturesight.framesource.rtph264", immediate = true)
@Service
@Properties({
  @Property(name = "cv.lecturesight.framesource.name", value = "RTP h.264 Streaming"),
  @Property(name = "cv.lecturesight.framesource.type", value = "rtph264")
})
public class RTPH264FrameSourceFactory implements FrameGrabberFactory {

  final static int DEFAULT_PORT = 8554;

  private final Log log = new Log("RTPH264");

  private List<RTPH264ClientFrameGrabber> children = new LinkedList<RTPH264ClientFrameGrabber>();

  protected void activate(ComponentContext cc) {
    System.out.println("RTPH264FrameSourceFactory.activate");
    Gst.init();
    log.info("RTP h.264 Streaming FrameSource activated");
  }

  protected void deactivate(ComponentContext cc) {
    for (Iterator<RTPH264ClientFrameGrabber> it = children.iterator(); it.hasNext();) {
      RTPH264ClientFrameGrabber child = it.next();
      child.stop();
    }
    log.info("RTP h.264 Streaming FrameSource deactivated");
  }

  @Override
  public FrameGrabber createFrameGrabber(String input, Map<String, String> conf) throws FrameSourceException {
    String host;
    int port;

    // parse server url
    try {
      String[] s = input.trim().split(":");
      if (s.length == 2) {
        host = s[0];
        port = Integer.parseInt(s[1]);
      } else {
        host = input;
        port = DEFAULT_PORT;
      }
    } catch (Exception e) {
      String msg = "Failed to parse server URL: " + input;
      log.error(msg);
      throw new FrameSourceException(msg, e);
    }

    // attempt to create the FrameGrabber
    try {
      RTPH264ClientFrameGrabber grabber = new RTPH264ClientFrameGrabber(host, port);
      children.add(grabber);
      log.info("Created FrameGrabber " + grabber.toString());
      return grabber;
    } catch (IllegalStateException e) {
      String msg = "Failed to create RTPH264ClientFrameGrabber for " + input;
      log.error(msg);
      throw new FrameSourceException(msg, e);
    }
  }

  @Override
  public void destroyFrameGrabber(FrameGrabber fg) throws FrameSourceException {
    try {
      ((RTPH264ClientFrameGrabber) fg).stop();
    } catch (Exception e) {
      String msg = "Error while stopping FrameSource.";
      log.error(msg);
      throw new FrameSourceException(msg, e);
    }
  }
}
