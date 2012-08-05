package cv.lecturesight.decorator.head;

import cv.lecturesight.decorator.api.ObjectDecorator;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import cv.lecturesight.videoanalysis.foreground.ForegroundService;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/** ObjectDecorator Service: Head finder
 *
 */
@Component(name = "lecturesight.decorator.head", immediate = true)
@Service
@Properties({
  @Property(name = "lecturesight.decorator.name", value = "Head Finder"),
  @Property(name = "lecturesight.decorator.callon", value = "EACHFRAME"),
  @Property(name = "lecturesight.decorator.produces", value = {"head.center",
    "head.boundingbox", "head.radius"})
})
public class HeadDecorator implements ObjectDecorator {

  final static String PROPKEY_K = "k";
  final static String PROPKEY_MAXITER = "iterations.max";
  final static String OBJ_PROPKEY_HEAD_CENTROID = "head.center";
  final static String OBJ_PROPKEY_HEAD_BBOX = "head.boundingbox";
  final static String OBJ_PROPKEY_HEAD_RADIUS = "head.radius";
  final static String OBJ_PROPKEY_HEAD_CLUSTERS = "head.clusters";
  private Log log = new Log("Head Finder");
  @Reference
  Configuration config;
  @Reference
  ForegroundService fgs;
  private int PARAM_K = 7;
  private int MAX_ITER = 100;

  protected void activate(ComponentContext cc) throws Exception {
    PARAM_K = config.getInt(PROPKEY_K);
    MAX_ITER = config.getInt(PROPKEY_MAXITER);
    log.info("BrainzzZ!");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    log.info("Deactivated");
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

        //log.debug("iterations: " + iterations);

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
        object.setProperty(OBJ_PROPKEY_HEAD_CENTROID, new Position((int) gravity.getX(), (int) gravity.getY()));
        object.setProperty(OBJ_PROPKEY_HEAD_BBOX, new BoundingBox(
                new Position((int) boundaries[0].getX(), (int) boundaries[0].getY()),
                new Position((int) boundaries[1].getX(), (int) boundaries[1].getY())));
        object.setProperty(OBJ_PROPKEY_HEAD_RADIUS, new Double(clusters[optimal].radius()));
        object.setProperty(OBJ_PROPKEY_HEAD_CLUSTERS, clusters);
      }
    } catch (Exception e) {
      log.error("Error in head finder!", e);
    }
  }
}
