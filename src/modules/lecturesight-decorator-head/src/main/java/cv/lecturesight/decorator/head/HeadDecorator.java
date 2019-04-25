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
package cv.lecturesight.decorator.head;

import cv.lecturesight.decorator.api.ObjectDecorator;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

/** ObjectDecorator Service: Head finder
 *
 */
public class HeadDecorator implements ObjectDecorator {

  final static String PROPKEY_K = "k";
  final static String PROPKEY_MAXITER = "iterations.max";
  final static String OBJ_PROPKEY_HEAD_CENTROID = "head.center";
  final static String OBJ_PROPKEY_HEAD_BBOX = "head.boundingbox";
  final static String OBJ_PROPKEY_HEAD_RADIUS = "head.radius";
  final static String OBJ_PROPKEY_HEAD_CLUSTERS = "head.clusters";
  final static String OBJ_PROPKEY_BW_PIXELS = "obj.bw_pixels";
  
  @Setter
  Configuration config;
  @Setter
  ForegroundService fgs;
  private int PARAM_K = 7;
  private int MAX_ITER = 100;

  protected void activate(ComponentContext cc) throws Exception {
    PARAM_K = config.getInt(PROPKEY_K);
    MAX_ITER = config.getInt(PROPKEY_MAXITER);
    Logger.info("BrainzzZ!");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    Logger.info("Deactivated");
  }

  @Override
  public void examine(TrackerObject object) {
    // Try to read the image
    try {
      BoundingBox bbox = (BoundingBox) object.getProperty(ObjectTracker.OBJ_PROPKEY_BBOX);
      BufferedImage image = fgs.getForegroundMapHost();
      WritableRaster r = image.getRaster();

      // We will store all points in this stack
      PointStack points = new PointStack();

      int width = bbox.getWidth();
      int height = bbox.getHeight();

      // get all points
      Position min = bbox.getMin();
      for (int i = 0; i < width; i++) {
        for (int j = 0; j < height; j++) {
          if (r.getSample(min.getX() + i, min.getY() + j, 0) > 0) {
            points.push(new Position(i, j));
          }
        }
      }
      
      object.setProperty(OBJ_PROPKEY_BW_PIXELS, (float) points.length()/(width*height+1));

      if (points.length() > PARAM_K) {
        Position gravity = new ClusterStack(points).get_center();

        // Cluster array
        ClusterStack[] clusters = (ClusterStack[])object.getProperty(OBJ_PROPKEY_HEAD_CLUSTERS);

        if(clusters == null) {
          clusters = new ClusterStack[PARAM_K];
          Random generator = new Random();

          for (int i = 0; i < PARAM_K; i++) {
            int index = generator.nextInt(points.length());
            clusters[i] = new ClusterStack(points.index(index));
          }
        }

        int iterations = 0;
        boolean noChange = false;

        while (noChange == false && iterations < MAX_ITER) {
          // Reset clusters
          for (int i = 0; i < clusters.length; i++) {
            clusters[i].reset();
          }
          noChange = true;
          // check distance for all points
          for (int i = 0; i < points.length(); i++) {
            int shortest = 0;
            // distance between first cluster and actual point
            double distance = Helper.euclidean_distance(points.index(i),
                    clusters[0].get_center());
            // Compute distance to all clusters
            for (int j = 1; j < clusters.length; j++) {
              double distance_new = Helper.euclidean_distance(points.index(i),
                      clusters[j].get_center());
              if (distance_new < distance) {
                distance = distance_new;
                shortest = j;
              }
            }
            // push point to the nearest cluster
            clusters[shortest].push(points.index(i));
          }
          // recalculate the cluster centers
          for (int i = 0; i < clusters.length; i++) {
            if (clusters[i].recalculate_center()) {
              noChange = false;
            }
          }
          iterations++;
        }

        //Logger.debug("iterations: " + iterations);

        double d = Double.MAX_VALUE;
        int optimal = 0;

        for (int i = 0; i < clusters.length; i++) {
          double x_distance = Math.pow((clusters[i].get_center().getX()
                  - gravity.getX()), 2);
          double y_distance = Math.pow((clusters[i].get_center().getY()), 2);
          if (x_distance + y_distance < d) {
            d = x_distance + y_distance;
            optimal = i;
          }
        }
        Position[] boundaries = clusters[optimal].min_max();

        // save results to TackerObject
        int bx = bbox.getMin().getX(), by = bbox.getMin().getY();
        object.setProperty(OBJ_PROPKEY_HEAD_CENTROID, new Position(
          (int) clusters[optimal].get_center().getX(), 
          (int) clusters[optimal].get_center().getY()));
        object.setProperty(OBJ_PROPKEY_HEAD_BBOX, new BoundingBox(
                new Position((int) boundaries[0].getX(), (int) boundaries[0].getY()),
                new Position((int) boundaries[1].getX(), (int) boundaries[1].getY())));
        object.setProperty(OBJ_PROPKEY_HEAD_RADIUS, new Double(clusters[optimal].radius()));
        object.setProperty(OBJ_PROPKEY_HEAD_CLUSTERS, clusters);
      }
    } catch (Exception e) {
      Logger.error("Error in head finder!", e.getCause());
    }
  }
}
