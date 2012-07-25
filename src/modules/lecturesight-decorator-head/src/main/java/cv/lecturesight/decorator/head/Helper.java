package cv.lecturesight.decorator.head;

import cv.lecturesight.util.geometry.Position;

public class Helper {

  public static double euclidean_distance(Position x, Position y) {
    float diff1 = x.getX() - y.getX();
    float diff2 = x.getY() - y.getY();
    return Math.sqrt(diff1 * diff1 + diff2 * diff2);
  }
}