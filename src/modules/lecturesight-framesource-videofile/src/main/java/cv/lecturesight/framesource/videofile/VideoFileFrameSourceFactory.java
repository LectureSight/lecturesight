package cv.lecturesight.framesource.videofile;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.framesource.FrameSourceException;
import cv.lecturesight.framesource.FrameGrabberFactory;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.gstreamer.Gst;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

/** Implementation of Service API
 *
 * @scr.component name="lecturesight.framesource.videofile" immediate="true"
 * @scr.service
 * @scr.property name="cv.lecturesight.framesource.name" value="Video File"
 * @scr.property name="cv.lecturesight.framesource.type" value="file"
 *
 */
public class VideoFileFrameSourceFactory implements FrameGrabberFactory {

  /** @scr.reference */
  private LogService log;

  private List<VideoFilePipeline> children = new LinkedList<VideoFilePipeline>();

  protected void activate(ComponentContext cc) {
    log.log(log.LOG_INFO, "Activating VideoFileFrameSource");
    // init gstreamer
    Gst.init();
  }

  protected void deactivate(ComponentContext cc) {
    log.log(log.LOG_INFO, "Deactivating VideoFileFrameSource");
    // stop all created pipelines
    for (Iterator<VideoFilePipeline> it = children.iterator(); it.hasNext();) {
      VideoFilePipeline child = it.next();
      child.stop();
    }
    // deinit gstreamer
    Gst.deinit();
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
      log.log(log.LOG_INFO, "Create FrameGrabber on video file " + input);
      return grabber;
    } catch (UnableToLinkElementsException e) {
      throw new FrameSourceException("Error while creating FrameGrabber: " + e.getMessage());
    }
  }
}
