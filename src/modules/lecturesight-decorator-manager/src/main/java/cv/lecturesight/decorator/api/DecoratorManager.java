package cv.lecturesight.decorator.api;

import cv.lecturesight.objecttracker.TrackerObject;

public interface DecoratorManager {
  
  public enum CallType {
    EACHFRAME, ONAPPEAR
  }
  
  void applyDecorators(CallType type, TrackerObject obj);
}
