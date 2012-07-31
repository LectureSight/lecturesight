/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cv.lecturesight.regiontracker;

import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.awt.image.WritableRaster;

/**
 *
 * @author alexfecke
 */
public class ColorHistogram {

  // Color histogram is stored in 2D-Array, first dimension R/G/B, second values
  float[][]ch;

  /**
   * Construct a ColorHistogram, given a WritableRaster of the imge and the bin
   * size of the histogram
   * @param img WritableRaster of the image
   * @param imgc WritableRaster of the scene, contains colored pixels
   * @param bbox BoundingBox
   * @param N Size of the Histogram
   */
  public ColorHistogram(WritableRaster img, WritableRaster imgc, BoundingBox bbox, int N) {
    ch = compute_ch(img, imgc, bbox, N);
    this.normalize();
  }

  /**
   * Computes a new ColorHistogram, given the actual image and the old histogram
   * @param img WritableRaster of the image
   * @param imgc WritableRaster of the scene, colored pixels
   * @param bbox BoundingBox
   * @param N Size of the Histogram
   * @param ch1 Old ColorHistogram
   */
  public ColorHistogram(WritableRaster img, WritableRaster imgc, BoundingBox bbox, int N,
          ColorHistogram ch1) {

    // This value should be set in order to determine, how much the older
    // ColorHistogram contributes to the new ColorHistogram. 0 => the initial
    // ColorHistogram is never updated, 1 => the old ColorHistogram does not
    // contribute.
    float alpha = 0.5f;
    
    ch = compute_ch(img, imgc, bbox, N);
    this.normalize();

    for(int i = 0; i < ch.length; i++) {
      for(int j = 0; j < ch[i].length; j++) {
        ch[i][j] = (1-alpha)*ch1.ch_value(i, j)+alpha*ch[i][j];
      }
    }
  }

  /**
   * Computes the Color Histogram for a given Image
   * @param img WritableRaster of the Image, contains the silhouette of the person
   * @param imgc WritableRaster of the scene, contains the colored pixels
   * @param bbox Bounding box
   * @param N desired size of histogram
   * @return color histogram
   */
  private float[][] compute_ch(WritableRaster img, WritableRaster imgc, BoundingBox bbox, int N) {
    // we take the coordinates of the BoundingBox
    int width = bbox.getWidth();
    int height = bbox.getHeight();
    Position min = bbox.getMin();

    // intialize histogram
    float[][]bins = new float [3][N];
    for(int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        if(img.getSample(min.getX()+i, min.getY()+j, 0) != 0) {
          for (int rgb = 0; rgb < 3; rgb++) {
            bins[rgb][(imgc.getSample(min.getX()+i,min.getY()+j,rgb)*N)/256]++;
          }
        }
      }
    }
    return bins;
  }

  /**
   * Sums up a two-dimensional float array
   * @param bins float array, 2D
   * @return float sum of the array
   */
  private static float sum(float[][] bins) {
    float sum = 0;
    for(int i = 0; i < bins.length; i++) {
      for(int j = 0; j < bins[i].length; j++) {
        sum += bins[i][j];
      }
    }
    return sum;
  }

  /**
   * Return the value at position (i,j)
   * @param i
   * @param i
   * @return
   */
  private float ch_value(int i, int j) {
    return ch[i][j];
  }

  /**
   * Normalize the Color Histogram
   */
  private void normalize() {
    float sum = sum(ch);
    for(int i = 0; i < ch.length; i++) {
      for(int j = 0; j < ch[i].length; j++) {
        ch[i][j] /= sum;
      }
    }
  }

  /**
   * Computes the Bhattacharya-Distance between the actual Color-Histogram and
   * a given one
   * @param ch1 ColorHistogram
   * @return float Bhattacharya-Distance
   * @throws Exception
   */
  public double bhattacharya_distance(ColorHistogram colorhistogram) throws Exception {
    float [][] ch1 = colorhistogram.ch;
    if(ch1[0].length != ch[0].length) {
      throw new Exception("Color-Histogramme muessen gleiche Laenge haben!");
    }
    // Compute Bhattacharya Coefficient
    double bf = 0;
    for(int i = 0; i < ch1.length; i++) {
      for(int j= 0; j < ch1[i].length; j++) {
        bf += Math.sqrt(ch1[i][j]*ch[i][j]);
      }
    }
    return Math.sqrt(1-bf);
  }
}