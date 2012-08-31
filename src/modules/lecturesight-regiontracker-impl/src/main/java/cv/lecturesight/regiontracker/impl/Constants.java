package cv.lecturesight.regiontracker.impl;

public class Constants {

  final static String PROPKEY_OBJECTS_MAX = "maxnum";
  final static String PROPKEY_SIZE_MIN = "size.min";
  final static String PROPKEY_SIZE_MAX = "size.max";
  final static String PROPKEY_WIDTH_MIN = "width.min";
  final static String PROPKEY_WIDTH_MAX = "width.max";
  final static String PROPKEY_HEIGHT_MIN = "height.min";
  final static String PROPKEY_HEIGHT_MAX = "height.max";
  final static String PROPKEY_TIMETOLIVE = "ttl";
  final static String PROPKEY_DISPLAY_OVERLAP = "display.overlap";
  final static String PROPKEY_DEBUG = "debug";
  final static String WINDOWNAME_OVERLAP = "regions.temporal.overlap";
  final static String SIGNAME_DONE_COMPUTE_OVERLAP = "regiontracker.COMPUTE_OVERLAP_DONE";
  final static String SIGNAME_DONE_CORRELATION = "regiontracker.CORRELATION_DONE";
  final static int    pairsBufferLength = 1024;
}
