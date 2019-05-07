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
import cv.lecturesight.framesource.FrameGrabberFactory;
import cv.lecturesight.framesource.FrameSourceException;

import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Service API
 */
public class VideoFileFrameSourceFactory implements FrameGrabberFactory {

  private List<VideoFilePipeline> children = new LinkedList<VideoFilePipeline>();

  protected void activate(ComponentContext cc) {
    Logger.info("Activating VideoFileFrameSource");
  }

  protected void deactivate(ComponentContext cc) {
    Logger.info("Deactivating VideoFileFrameSource");
    // stop all created pipelines
    for (Iterator<VideoFilePipeline> it = children.iterator(); it.hasNext();) {
      VideoFilePipeline child = it.next();
      child.stop();
    }
  }

  @Override
  public FrameGrabber createFrameGrabber(String input, Map<String,String> conf) throws FrameSourceException {
    File videoFile = new File(input);
    if (!videoFile.exists() || !videoFile.isFile()) {
      throw new FrameSourceException("Not a valid file: " + input);
    }

    // attempt to create the gst pipline
    try {
      VideoFilePipeline grabber = new VideoFilePipeline(videoFile);
      children.add(grabber);
      Logger.info("Created FrameGrabber on video file " + input);
      return grabber;
    } catch (UnableToLinkElementsException e) {
      throw new FrameSourceException("Error while creating FrameGrabber: " + e.getMessage());
    }
  }

  @Override
  public void destroyFrameGrabber(FrameGrabber fg) throws FrameSourceException {
   try {
      ((VideoFilePipeline)fg).stop();
    } catch (Exception e) {
      throw new FrameSourceException("Failed to shut down VideoFilePipeline. ", e);
    }
  }
}
