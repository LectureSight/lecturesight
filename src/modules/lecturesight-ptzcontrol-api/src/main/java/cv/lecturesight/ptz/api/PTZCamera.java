package cv.lecturesight.ptz.api;

import cv.lecturesight.util.geometry.Position;

public interface PTZCamera {
  
  String getName();
  
  PTZCameraProfile getProfile();

  void reset();
  
  void stopMove();
  
  void moveHome();
  
  void moveUp(int tiltSpeed);
  
  void moveDown(int tiltSpeed);
  
  void moveLeft(int panSpeed);
  
  void moveRight(int panSpeed);
  
  void moveUpLeft(int panSpeed, int tiltSpeed);
  
  void moveUpRight(int panSpeed, int tiltSpeed);
  
  void moveDownLeft(int panSpeed, int tiltSpeed);
  
  void moveDownRight(int panSpeed, int tiltSpeed);
  
  void moveAbsolute(int panSpeed, int tiltSpeed, Position target);
  
  void moveRelative(int panSpeed, int tiltSpeed, Position target);
  
  Position getPosition();
  
  void stopZoom();
  
  void zoomIn(int speed);
  
  void zoomOut(int speed);
  
  void zoom(int zoom);
  
  int getZoom();
}
