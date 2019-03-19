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
package cv.lecturesight.ptz.api;

import cv.lecturesight.util.geometry.Position;

import java.util.Properties;

public class PTZCameraProfile {

  public enum Property {
    VENDOR("camera.vendor.name"),
    MODEL("camera.model.name"),
    PAN_MIN("camera.pan.min"),
    PAN_MAX("camera.pan.max"),
    PAN_MAXSPEED("camera.pan.maxspeed"),
    TILT_MIN("camera.tilt.min"),
    TILT_MAX("camera.tilt.max"),
    TILT_MAXSPEED("camera.tilt.maxspeed"),
    ZOOM_MIN("camera.zoom.min"),
    ZOOM_MAX("camera.zoom.max"),
    ZOOM_MAXSPEED("camera.zoom.maxspeed"),
    HOME_PAN("camera.home.pan"),
    HOME_TILT("camera.home.tilt");

    private String key;
    Property(String key) {this.key=key;}
    public String key() {return key;}
  }

  private String vendor;
  private String model;
  private int zoom_min;
  private int zoom_max;
  private int zoom_maxspeed;
  private int pan_min;
  private int pan_max;
  private int pan_maxspeed;
  private int tilt_min;
  private int tilt_max;
  private int tilt_maxspeed;
  private Position home_pos;

  public PTZCameraProfile(String vendor, String model,
          int pan_min, int pan_max, int pan_maxspeed,
          int tilt_min, int tilt_max, int tilt_maxspeed,
          int zoom_min, int zoom_max, int zoom_maxspeed,
          Position home_pos) {

    this.vendor = vendor;
    this.model = model;
    this.pan_min = pan_min;
    this.pan_max = pan_max;
    this.pan_maxspeed = pan_maxspeed;
    this.tilt_min = tilt_min;
    this.tilt_max = tilt_max;
    this.tilt_maxspeed = tilt_maxspeed;
    this.zoom_min = zoom_min;
    this.zoom_max = zoom_max;
    this.zoom_maxspeed = zoom_maxspeed;
    this.home_pos = home_pos;
  }

  public PTZCameraProfile(Properties props) {
    this.vendor = props.getProperty(Property.VENDOR.key());
    this.model = props.getProperty(Property.MODEL.key());
    this.pan_min = Integer.parseInt(props.getProperty(Property.PAN_MIN.key()));
    this.pan_max = Integer.parseInt(props.getProperty(Property.PAN_MAX.key()));
    this.pan_maxspeed = Integer.parseInt(props.getProperty(Property.PAN_MAXSPEED.key()));
    this.tilt_min = Integer.parseInt(props.getProperty(Property.TILT_MIN.key()));
    this.tilt_max = Integer.parseInt(props.getProperty(Property.TILT_MAX.key()));
    this.tilt_maxspeed = Integer.parseInt(props.getProperty(Property.TILT_MAXSPEED.key()));
    this.zoom_min = Integer.parseInt(props.getProperty(Property.ZOOM_MIN.key()));
    this.zoom_max = Integer.parseInt(props.getProperty(Property.ZOOM_MAX.key()));
    this.zoom_maxspeed = Integer.parseInt(props.getProperty(Property.ZOOM_MAXSPEED.key()));
    this.home_pos = new Position(
            Integer.parseInt(props.getProperty(Property.HOME_PAN.key())),
            Integer.parseInt(props.getProperty(Property.HOME_TILT.key())));
  }

  public String getVendor() {
    return vendor;
  }

  public String getModel() {
    return model;
  }

  public int getZoomMin() {
    return zoom_min;
  }

  public int getZoomMax() {
    return zoom_max;
  }

  public int getZoomMaxSpeed() {
    return zoom_maxspeed;
  }

  public int getPanMin() {
    return pan_min;
  }

  public int getPanMax() {
    return pan_max;
  }

  public int getPanMaxSpeed() {
    return pan_maxspeed;
  }

  public int getTiltMin() {
    return tilt_min;
  }

  public int getTiltMax() {
    return tilt_max;
  }

  public int getTiltMaxSpeed() {
    return tilt_maxspeed;
  }

  public Position getHomePosition() {
    return home_pos.clone();
  }

}
