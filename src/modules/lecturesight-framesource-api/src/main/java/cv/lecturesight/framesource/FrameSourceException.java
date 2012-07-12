package cv.lecturesight.framesource;

public class FrameSourceException extends Exception {

  public FrameSourceException(String msg) {
    super(msg);
  }
  
  public FrameSourceException(String msg, Exception e) {
    super(msg, e);
  }

}
