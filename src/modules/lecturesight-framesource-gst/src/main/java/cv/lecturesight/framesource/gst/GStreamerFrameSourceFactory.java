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
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.util.Log;
import java.util.Map;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.gstreamer.Gst;
import org.osgi.service.component.ComponentContext;

/**
 * GStreamer FrameSourceFactory
 *
 */
@Component(name = "lecturesight.framesource.gst", immediate = true)
@Service
@Properties({
  @Property(name = "cv.lecturesight.framesource.name", value = "GStreamer"),
  @Property(name = "cv.lecturesight.framesource.type", value = "gst")
})
public class GStreamerFrameSourceFactory implements FrameGrabberFactory {

  private final Log log = new Log("GStreamer Framesource");

  protected void activate(ComponentContext cc) {
    Gst.init();
    log.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated");
  }

  @Override
  public FrameGrabber createFrameGrabber(String input, Map<String, String> conf) throws FrameSourceException {
    
    // evaluate drop parameter if configured
    boolean drop = true;
    if (conf.containsKey("drop")) {
      try {
        drop = Boolean.valueOf(conf.get("drop"));
      } catch (Exception e) {
        log.warn("Error while parsing value for 'drop' parameter, using default drop=true");
      }
    }
    
    // create FrameGrabber
    GStreamerFrameGrabber fg = new GStreamerFrameGrabber(input, drop);
    log.info("Create new GStreamerFrameGrabber " + fg.toString());
    return fg;
  }

  @Override
  public void destroyFrameGrabber(FrameGrabber fg) throws FrameSourceException {
    ((GStreamerFrameGrabber)fg).stop();
  }
}
