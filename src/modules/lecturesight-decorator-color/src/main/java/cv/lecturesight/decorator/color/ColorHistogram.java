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
 
import cv.lecturesight.util.geometry.BoundingBox;
import cv.lecturesight.util.geometry.Position;
import java.awt.image.WritableRaster;
 
/**
 *
 * @author alexfecke
 */
public class ColorHistogram {
 
  // Color histogram is stored in 2D-Array, first dimension R/G/B, second values
  private float[][][]ch;
  public boolean mist = false;
 
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
        for(int k = 0; k < ch[i][j].length; k++) {
          ch[i][j][k] = (1-alpha)*ch1.ch_value(i, j, k)+alpha*ch[i][j][k];
        }
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
  private float[][][] compute_ch(WritableRaster img, WritableRaster imgc, BoundingBox bbox, int N) {
    // we take the coordinates of the BoundingBox
    int width = bbox.getWidth();
    int height = bbox.getHeight();
    Position min = bbox.getMin();
 
    // intialize histogram
    float[][][]bins = new float [N][N][N];
    for(int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        if(img.getSample(min.getX()+i, min.getY()+j, 0) != 0) {
          bins[(imgc.getSample(min.getX()+i,min.getY()+j,0)*N)/256]
              [(imgc.getSample(min.getX()+i,min.getY()+j,1)*N)/256]
              [(imgc.getSample(min.getX()+i,min.getY()+j,2)*N)/256]++;
        }
      }
    }
    return bins;
  }
 
  /**
   * Sums up a two-dimensional float array
   * @param bins float array, 3D
   * @return float sum of the array
   */
  private static float sum(float[][][] bins) {
    float sum = 0;
    for(int i = 0; i < bins.length; i++) {
      for(int j = 0; j < bins[i].length; j++) {
        for(int k = 0; k < bins[i][j].length; k++) {
          sum += bins[i][j][k];
        }
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
  private float ch_value(int i, int j, int k) {
    return ch[i][j][k];
  }
  
  public float[][][] get_ch() {
    return ch;
  }
 
  /**
   * Normalize the Color Histogram
   */
  private void normalize() {
    float sum = sum(ch);
    if (sum != 0) {
      for(int i = 0; i < ch.length; i++) {
        for(int j = 0; j < ch[i].length; j++) {
          for(int k = 0; k < ch[i][j].length; k++) {
            ch[i][j][k] /= sum;
          }
        }
      }
    }
    else {
      mist = true;
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
    float [][][] ch1 = colorhistogram.get_ch();
    if(ch1[0].length != ch[0].length) {
      throw new Exception("Color-Histogramme muessen gleiche Laenge haben!");
    }
    // Compute Bhattacharya Coefficient
    double bf = 0;
    for(int i = 0; i < ch1.length; i++) {
      for(int j = 0; j < ch1[i].length; j++) {
        for(int k = 0; k < ch1[i][j].length; k++) {
          bf += Math.sqrt(ch1[i][j][k]*ch[i][j][k]);
        }
      }
    }
    return (1-bf);
  }
  
  @Override
  public String toString() {
    return ch[0].length+" Channels";
  }
  
}