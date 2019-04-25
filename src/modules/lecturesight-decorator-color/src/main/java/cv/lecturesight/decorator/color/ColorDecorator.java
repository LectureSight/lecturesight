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
package cv.lecturesight.decorator.color;

import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.decorator.api.ObjectDecorator;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

/** ObjectDecorator Service: Head finder
 *
 */
public class ColorDecorator implements ObjectDecorator {

  final static String OBJ_PROPKEY_COLOR_HISTOGRAM = "color.histogram";
  @Setter
  Configuration config;
  @Setter
  ForegroundService fgs;
  @Setter
  private FrameSourceProvider fsp;
  private int width_min, width_max, height_min, height_max, channel_number;

  protected void activate(ComponentContext cc) throws Exception {
    width_min = config.getInt("width.min");
    width_max = config.getInt("width.max");
    height_min = config.getInt("height.min");
    height_max = config.getInt("height.max");
    channel_number = config.getInt("channel.number");
    Logger.info("Activated");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    Logger.info("Deactivated");
  }

  @Override
  public void examine(TrackerObject object) {
    // Try to read the image
    try {
      BoundingBox bbox = (BoundingBox) object.getProperty(
              ObjectTracker.OBJ_PROPKEY_BBOX);

      BufferedImage sil = fgs.getForegroundMapHost();
      
      FrameSource fsrc = fsp.getFrameSource();
      BufferedImage scene = fsrc.getImageHost();
      WritableRaster img = sil.getRaster();
      WritableRaster imgc = scene.getRaster();
      
      ColorHistogram ch = (ColorHistogram) object.getProperty(
              OBJ_PROPKEY_COLOR_HISTOGRAM);
      if(ch == null) {
        //Logger.info("ColorHistogram erstellt fuer TrackerObject "+object.getId());
        object.setProperty(OBJ_PROPKEY_COLOR_HISTOGRAM, 
              new ColorHistogram(img, imgc, bbox, channel_number));
      }
      else {
        //Logger.info("ColorHistogram geupdated fuer TrackerObject "+object.getId());
        ColorHistogram ch2 = new ColorHistogram(
                img, imgc, bbox, channel_number, ch);
        object.setProperty(OBJ_PROPKEY_COLOR_HISTOGRAM, ch2);
        double dist = ch.bhattacharya_distance(ch2);
        //Logger.info("ColorHistogram-Update-Difference fuer TrackerObject "+object.getId()+": "+dist);
      }
    } catch (Exception e) {
        Logger.error("Error in color decorator!", e);
    }
  }
}
