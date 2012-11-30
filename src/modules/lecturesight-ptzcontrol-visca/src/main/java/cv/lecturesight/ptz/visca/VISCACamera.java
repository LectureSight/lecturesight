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
package cv.lecturesight.ptz.visca;

import cv.lecturesight.ptz.api.AbstractPTZCamera;
import cv.lecturesight.util.geometry.Position;
import java.util.Properties;

public class VISCACamera extends AbstractPTZCamera {
  
  LibVISCACamera camera;
  Properties props;
  String name;
  
  public VISCACamera(String name, LibVISCACamera cam, Properties profile) {
    this.name = name;
    this.camera = cam;
    this.props = profile;
    this.setCameraProfile(profile);
  }
  
  @Override
  public String getName() {
    return name;
  }

  @Override
  public void reset() {
    camera.setPanTiltReset();
  }

  @Override
  public void stopMove() {
    camera.setPanTiltStop();
  }

  @Override
  public void moveHome() {
    camera.setPanTiltHome();
  }

  @Override
  public void moveUp(int tiltSpeed) {
    camera.setPanTiltUp(tiltSpeed);
  }

  @Override
  public void moveDown(int tiltSpeed) {
    camera.setPanTiltDown(tiltSpeed);
  }

  @Override
  public void moveLeft(int panSpeed) {
    camera.setPanTiltLeft(panSpeed);
  }

  @Override
  public void moveRight(int panSpeed) {
    camera.setPanTiltRight(panSpeed);
  }

  @Override
  public void moveUpLeft(int panSpeed, int tiltSpeed) {
    camera.setPanTiltUpLeft(panSpeed, tiltSpeed);
  }

  @Override
  public void moveUpRight(int panSpeed, int tiltSpeed) {
    camera.setPanTiltUpRight(panSpeed, tiltSpeed);
  }

  @Override
  public void moveDownLeft(int panSpeed, int tiltSpeed) {
    camera.setPanTiltDownLeft(panSpeed, tiltSpeed);
  }

  @Override
  public void moveDownRight(int panSpeed, int tiltSpeed) {
    camera.setPanTiltDownRight(panSpeed, tiltSpeed);
  }

  @Override
  public void moveAbsolute(int panSpeed, int tiltSpeed, Position target) {
    camera.setPanTiltAbsolutePosition(panSpeed, tiltSpeed, target.getX(), target.getY());
  }

  @Override
  public void moveRelative(int panSpeed, int tiltSpeed, Position target) {
    camera.setPanTiltRelativePosition(panSpeed, tiltSpeed, target.getX(), target.getY());
  }

  @Override
  public Position getPosition() {
    return new Position(camera.getPanPosition(), camera.getTiltPosition());
  }

  @Override
  public void stopZoom() {
    camera.setZoomStop();
  }

  @Override
  public void zoomIn(int speed) {
    camera.setZoomTeleSpeed(speed);
  }

  @Override
  public void zoomOut(int speed) {
    camera.setZoomWideSpeed(speed);
  }

  @Override
  public void zoom(int zoom) {
    camera.setZoomValue(zoom);
  }

  @Override
  public int getZoom() {
    return camera.getZoomValue();
  }
  
  public void setLimitUpRight(int up, int right) {
    
  }
  
  public void setLimitDownLeft(int down, int left) {
    
  }
}
