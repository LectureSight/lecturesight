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
package cv.lecturesight.util.geometry;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Convert between normalized co-ordinates and camera pan/tilt co-ordinates
 */
public class CameraPositionModel {

  // scene limits for normalization
  private int pan_min;
  private int pan_max;
  private int tilt_min;
  private int tilt_max;

  // target position
  private boolean target_set = false;

  // camera position in camera coordinates
  private Position camera_pos = new Position(0, 0);

  // target position in camera coordinates
  private Position target_pos = new Position(0, 0);

  // camera position in normalized coordinates
  private NormalizedPosition target_posn = new NormalizedPosition(0.0f, 0.0f);

  // target position in normalized coordinates
  private NormalizedPosition camera_posn = new NormalizedPosition(0.0f, 0.0f);

  // Interpolation functions
  private PolynomialSplineFunction normCameraX;
  private PolynomialSplineFunction normCameraY;
  private PolynomialSplineFunction cameraNormX;
  private PolynomialSplineFunction cameraNormY;

  // Ranges over which the interpolation functions are valid
  private float minNormX = 1;
  private float maxNormX = -1;
  private float minNormY = 1;
  private float maxNormY = -1;

  private int minCameraX = 0;
  private int maxCameraX = 0;
  private int minCameraY = 0;
  private int maxCameraY = 0;

  /**
   * Initialize camera position model using scene limits
   * @param pan_min
   * @param pan_max
   * @param tilt_min
   * @param tilt_max
   */
  public CameraPositionModel(int pan_min, int pan_max, int tilt_min, int tilt_max) {
    update(pan_min, pan_max, tilt_min, tilt_max);
  }

  public int getPanMin() {
    return pan_min;
  }

  public int getPanMax() {
    return pan_max;
  }

  public int getTiltMin() {
    return tilt_min;
  }

  public int getTiltMax() {
    return tilt_max;
  }

  /**
   * Update camera model from scene limits
   */
  public void update(int pan_min, int pan_max, int tilt_min, int tilt_max) {

    if (pan_min != this.pan_min || pan_max != this.pan_max || tilt_min != this.tilt_min || tilt_max != this.tilt_max) {
      // Invalidate the spline functions if the limits have changed
      normCameraX = null;
      normCameraY = null;
      cameraNormX = null;
      cameraNormY = null;
    }

    this.pan_min = pan_min;
    this.pan_max = pan_max;
    this.tilt_min = tilt_min;
    this.tilt_max = tilt_max;
  }

  /**
   * Update the scene model from a set of matching presets.
   * The point sceneMarker[n] on the overview image matches the camera position cameraPreset[n].
   *
   * @param sceneMarker  Array of scene marker co-ordinates
   * @param cameraPreset Array of camera preset co-ordinates matching the scene markers
   * @return true if the model has been updated based on the preset co-ordinates.
   */
  public boolean update(List<NormalizedPosition> sceneMarker, List<Position> cameraPreset) {

    int points = sceneMarker.size();

    Logger.debug("Calibrating model with {} points", points);

    // SplineInterpolator requires at least 3 points
    if ((points < 3) || (points != cameraPreset.size())) {
      return false;
    }

    int minCameraPresetX = Integer.MAX_VALUE;
    int minCameraPresetY = Integer.MAX_VALUE;

    // X Values
    Map<Double, Integer> xMap = new TreeMap<>();

    // Y values
    Map<Double, Integer> yMap = new TreeMap<>();

    for (int i = 0; i < points; i++) {

      minCameraPresetX = Math.min(minCameraPresetX, cameraPreset.get(i).getX());
      minCameraPresetY = Math.min(minCameraPresetY, cameraPreset.get(i).getY());

      xMap.put(new Double(sceneMarker.get(i).getX()), cameraPreset.get(i).getX());
      yMap.put(new Double(sceneMarker.get(i).getY()), cameraPreset.get(i).getY());

      Logger.debug("Adding calibration point {0.0000},{0.0000} = {},{}",
        sceneMarker.get(i).getX(), sceneMarker.get(i).getY(), cameraPreset.get(i).getX(), cameraPreset.get(i).getY());
    }

    // Check X values
    int lastCameraX = minCameraPresetX - 1;
    double lastNormX = -2;
    int xpoints = 0;

    double[] xNorm = new double[points];
    double[] xCamera = new double[points];

    // Check set of X points
    for (Map.Entry<Double,Integer> entry : xMap.entrySet()) {
      Double xNormV = entry.getKey();
      Integer xCameraV = entry.getValue();

      Logger.trace("X point: {0.0000}, {}", xNormV, xCameraV);

      if ((xCameraV > lastCameraX) && (xNormV - lastNormX > 0.1)) {
        xNorm[xpoints] = xNormV;
        xCamera[xpoints] = xCameraV;
        xpoints++;
        lastCameraX = xCameraV;
        lastNormX = xNormV;
      } else {
        Logger.debug("Ignoring X preset position: not monotonic {} < {} or too closely spaced {0.00} to {0.00}",
          xCameraV, lastCameraX, xNormV, lastNormX);
      }
    }

    Logger.debug("X axis: {} points offered, {} points used", points, xpoints);
    xNorm = Arrays.copyOf(xNorm, xpoints);
    xCamera = Arrays.copyOf(xCamera, xpoints);

    // Check set of Y points
    int lastCameraY = minCameraPresetY - 1;
    double lastNormY = -2;
    int ypoints = 0;

    double[] yNorm = new double[points];
    double[] yCamera = new double[points];

    for (Map.Entry<Double,Integer> entry : yMap.entrySet()) {
      Double yNormV = entry.getKey();
      Integer yCameraV = entry.getValue();

      Logger.trace("Y point: {0.0000}, {}", yNormV, yCameraV);

      if ((yCameraV > lastCameraY) && (yNormV - lastNormY > 0.1)) {
        yNorm[ypoints] = yNormV;
        yCamera[ypoints] = yCameraV;
        ypoints++;
        lastCameraY = yCameraV;
        lastNormY = yNormV;
      } else {
        Logger.debug("Ignoring Y preset position: not monotonic {} < {} or too closely spaced {0.00} to {0.00}",
          yCameraV, lastCameraY, yNormV, lastNormY);
      }
    }

    Logger.debug("Y axis: {} points offered, {} points used", points, ypoints);
    yNorm = Arrays.copyOf(yNorm, ypoints);
    yCamera = Arrays.copyOf(yCamera, ypoints);

    if ((xpoints < 3) || (ypoints < 3)) {
      Logger.info("Insufficient usable marker points for calibration: {} usable x points, {} usable y points", xpoints, ypoints);
      return false;
    }

    minNormX = (float) xNorm[0];
    maxNormX = (float) xNorm[xpoints - 1];
    minNormY = (float) yNorm[0];
    maxNormY = (float) yNorm[ypoints - 1];

    minCameraX = (int) Math.round(xCamera[0]);
    maxCameraX = (int) Math.round(xCamera[xpoints - 1]);
    minCameraY = (int) Math.round(yCamera[0]);
    maxCameraY = (int) Math.round(yCamera[ypoints - 1]);

    Logger.debug("normalized X range {0.0000} to {0.0000}, normalized Y range {0.0000} to {0.0000}",
      minNormX, maxNormX, minNormY, maxNormY);

    Logger.debug("camera X range {} to {}, camera Y range {} to {}",
      minCameraX, maxCameraX, minCameraY, maxCameraY);

    // Create normalized to camera interpolators
    try {
      normCameraX = new SplineInterpolator().interpolate(xNorm, xCamera);
      normCameraY = new SplineInterpolator().interpolate(yNorm, yCamera);
    } catch (Exception e) {
      Logger.warn(e, "Cannot create calibration spline function from co-ordinate set");
      return false;
    }

    double[] xCameraCalc = new double[200];
    double[] yCameraCalc = new double[200];
    double[] xNormRange = new double[200];
    double[] yNormRange = new double[200];

    // Create inverse functions (camera to normalized)
    for (int x = -100; x < 100; x++) {
      xNormRange[x + 100] = x / 100f;
      yNormRange[x + 100] = x / 100f;
      Position xyCalc = toCameraCoordinates(new NormalizedPosition(x / 100f, x/100f));
      xCameraCalc[x + 100] = xyCalc.getX();
      yCameraCalc[x + 100] = xyCalc.getY();
    }

    try {
      cameraNormX = new SplineInterpolator().interpolate(xCameraCalc, xNormRange);
      cameraNormY = new SplineInterpolator().interpolate(yCameraCalc, yNormRange);
    } catch (Exception e) {
      Logger.warn(e, "Cannot create inverse spline function from co-ordinate set");
      return false;
    }

    // Calculate the edge points to set tilt/pan min/max

    if (normCameraX.isValidPoint(-1)) {
      pan_min = (int) Math.round(normCameraX.value(-1));
    } else {
      pan_min = (int) Math.round(findEdge(normCameraX, -1, minNormX, maxNormX));
    }

    if (normCameraX.isValidPoint(1)) {
      pan_max = (int) Math.round(normCameraX.value(1));
    } else {
      pan_max = (int) Math.round(findEdge(normCameraX, 1, minNormX, maxNormX));
    }

    if (normCameraY.isValidPoint(-1)) {
      tilt_min = (int) Math.round(normCameraY.value(-1));
    } else {
      tilt_min = (int) Math.round(findEdge(normCameraY, -1, minNormY, maxNormY));
    }

    if (normCameraY.isValidPoint(1)) {
      tilt_max = (int) Math.round(normCameraY.value(1));
    } else {
      tilt_max = (int) Math.round(findEdge(normCameraY, 1, minNormY, maxNormY));
    }

    Logger.debug("Updated pan min/max {} to {}, tilt min/max {} to {}",
      pan_min, pan_max, tilt_min, tilt_max);

    if (Logger.getLevel() == Level.TRACE) {
      logModel();
    }

    return true;
  }

  /**
   * Log the model
   */
  public void logModel() {

    Logger.trace("X Axis Overview:Camera Mapping, interpolated from {} to {}", minNormX, maxNormX);
    for (int x = -100; x <= 100; x += 5) {
      NormalizedPosition nPos = new NormalizedPosition(x / 100f, 0);
      Position cameraPos = toCameraCoordinates(nPos);
      NormalizedPosition nPosInv = toNormalizedCoordinates(cameraPos);
      Logger.trace("xNorm:xCam {0.0000} {} inverse {0.0000}", nPos.getX(), cameraPos.getX(), nPosInv.getX());
    }

    Logger.trace("Y Axis Overview:Mapping, interpolated from {} to {}", minNormY, maxNormY);
    for (int y = -100; y <= 100; y += 5) {
      NormalizedPosition nPos = new NormalizedPosition(0, y / 100f);
      Position cameraPos = toCameraCoordinates(nPos);
      NormalizedPosition nPosInv = toNormalizedCoordinates(cameraPos);
      Logger.trace("yNorm:yCam {0.0000} {} inverse {0.0000}", nPos.getY(), cameraPos.getY(), nPosInv.getY());
    }

  }

  /**
   * Return a position extrapolated outside the spline points.
   * Used bidirectionally (normalized to camera and camera to normalized)
   *
   * @param spline The spline function for the axis
   * @param x The edge point (-1 or 1)
   * @param minX The smallest knot point x value
   * @param maxX The largest knot point x value
   * @return The extrapolated y position for x
   */
  private double findEdge(PolynomialSplineFunction spline, float x, float minX, float maxX) {

    if (x < minX) {
      // Extrapolate from the smallest calibration point and a point close to it
      double minY = spline.value(minX);
      double nextX = minX + 0.1 * (maxX - minX);
      double nextY = spline.value(nextX);
      return extrapolate(x, minX, minY, nextX, nextY);
    } else {
      // Extrapolate from the largest calibration point and a point close to it
      double maxY = spline.value(maxX);
      double nextX = maxX - 0.1 * (maxX - minX);
      double nextY = spline.value(nextX);
      return extrapolate(x, maxX, maxY, nextX, nextY);
    }

  }

  public double extrapolate(double x, double x1, double y1, double x2, double y2) {
    return y1 + (x - x1) / (x2 - x1) * (y2 - y1);
  }

  /**
   * Translates camera coordinates to normalized coordinates (-1 to 1)
   *
   * @param pos camera coordinates
   * @return normalized coordinates
   */
  public NormalizedPosition toNormalizedCoordinates(Position pos) {

    NormalizedPosition out = new NormalizedPosition(0.0f, 0.0f);
    float x = pos.getX();
    float y = pos.getY();

    if ((cameraNormX != null) && (cameraNormY != null)) {

      if (cameraNormX.isValidPoint(x)) {
        // Spline interpolation
        out.setX((float) cameraNormX.value(x));
      } else {
        out.setX((float) findEdge(cameraNormX, x, minCameraX, maxCameraX));
      }

      if (cameraNormY.isValidPoint(pos.getY())) {
        // Spline interpolation
        out.setY((float) cameraNormY.value(pos.getY()));
      } else {
        out.setY((float) findEdge(cameraNormY, y, minCameraY, maxCameraY));
      }

    } else {
      // Linear mapping using scene limits
      out.setX((x - pan_min) / (pan_max - pan_min) * 2 - 1);
      out.setY((y - tilt_min) / (tilt_max - tilt_min) * 2 - 1);
    }

    return out;
  }

  /**
   * Translates normalized coordinates (-1 to 1) to camera coordinates.
   *
   * @param pos normalized coordinates
   * @return camera coordinates
   */
  public Position toCameraCoordinates(NormalizedPosition pos) {

    Position out = new Position(0, 0);
    float x = pos.getX();
    float y = pos.getY();

    if ((normCameraX != null) && (normCameraY != null)) {

      if (normCameraX.isValidPoint(x)) {
        // Spline interpolation
        out.setX((int) Math.round(normCameraX.value(x)));
      } else {
        out.setX((int) Math.round(findEdge(normCameraX, x, minNormX, maxNormX)));
      }

      if (normCameraY.isValidPoint(y)) {
        // Spline interpolation
        out.setY((int) Math.round(normCameraY.value(y)));
      } else {
        out.setY((int) Math.round(findEdge(normCameraY, y, minNormY, maxNormY)));
      }

    } else {
      // Linear mapping using scene limits
      out.setX((int) Math.round((x + 1) * (pan_max - pan_min) * 0.5 + pan_min));
      out.setY((int) Math.round((y + 1) * (tilt_max - tilt_min) * 0.5 + tilt_min));
    }

    return out;
  }

  public synchronized void setCameraPositionNorm(NormalizedPosition posn) {
    camera_posn = posn;
    camera_pos = toCameraCoordinates(posn);
  }

  public synchronized void setCameraPosition(Position pos) {
    camera_posn = toNormalizedCoordinates(pos);
    camera_pos = pos;
  }

  public synchronized void setTargetPositionNorm(NormalizedPosition posn) {
    target_set = true;
    target_posn = posn;
    target_pos = toCameraCoordinates(posn);
  }

  public synchronized void setTargetPosition(Position pos) {
    target_set = true;
    target_posn = toNormalizedCoordinates(pos);
    target_pos = pos;
  }

  public synchronized Position getCameraPosition() {
    return camera_pos.clone();
  }

  public synchronized NormalizedPosition getCameraPositionNorm() {
    return camera_posn.clone();
  }

  public synchronized Position getTargetPosition() {
    return target_pos.clone();
  }

  public synchronized NormalizedPosition getTargetPositionNorm() {
    return target_posn.clone();
  }

  public boolean isTargetSet() {
    return target_set;
  }

}
