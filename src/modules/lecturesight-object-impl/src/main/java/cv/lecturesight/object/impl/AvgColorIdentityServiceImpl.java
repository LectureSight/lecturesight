package cv.lecturesight.object.impl;

import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.object.ColorHistogram;
import cv.lecturesight.object.ObjectIdentityService;
import cv.lecturesight.object.TrackerObject;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;

@Component(name="lecturesight.objects.identity",immediate=true)
@Service
public class AvgColorIdentityServiceImpl implements ObjectIdentityService {

  @Reference
  ForegroundService fgs;
  @Reference
  private FrameSourceProvider fsp;
  
  @Override
  public TrackerObject match(TrackerObject matchee, List<TrackerObject> others, float threshold) {
    // actual Color-Histogram
    ColorHistogram ch = (ColorHistogram) matchee.getProperty("ColorHistogram");
    // initial minimum distance
    double min_distance = Double.MAX_VALUE;
    // we set the actual element to the element to which we have to compare the
    // rest. This is to assure, that match does not fail if called with an empty
    // list.
    TrackerObject act = matchee;

    // Iterate over all elements
    Iterator iter = others.iterator();
    while(iter.hasNext()) {
      // This is the element we have compare the matchee to
      TrackerObject act1 = (TrackerObject) iter.next();
      try {
        // compute the bhattacharya-distance
        double distance = ch.bhattacharya_distance((ColorHistogram) act1.getProperty("ColorHistogram"));
        if(distance < min_distance) {
          min_distance = distance;
          act = act1;
        }
      } catch (Exception ex) {
        Logger.getLogger(AvgColorIdentityServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    // Return the element, which is nearest to the element matchee
    return act;
  }

  @Override
  public void update(List<TrackerObject> singletons) {
    // Get Foreground-Map
    BufferedImage sil   = fgs.getForegroundMapHost();
    // Get Scene-Image
    FrameSource fsrc    = fsp.getFrameSource();
    BufferedImage scene = fsrc.getImageHost();
    WritableRaster img  = sil.getRaster();
    WritableRaster imgc = scene.getRaster();

    // Iterate over all TrackerObjects
    Iterator iter = singletons.iterator();
    while(iter.hasNext()) {
      TrackerObject act = (TrackerObject) iter.next();
      // There exists no ColorHistogram yet -> create one
      if(!act.hasProperty("ColorHistogram")) {
        act.setProperty("ColorHistogram", 
                new ColorHistogram(img, imgc, act.getBoundingBox(), 256));
      }
      // merge existing ColorHistogram with the actual one
      else {
        act.setProperty("ColorHistogram", 
                new ColorHistogram(img, imgc, act.getBoundingBox(),
                256, (ColorHistogram) act.getProperty("ColorHistogram")));
      }
    }
  }
  
}