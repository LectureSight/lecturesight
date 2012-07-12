package cv.lecturesight.framesource;

public interface FrameSourceManager {

  FrameSource createFrameSource(String input) throws FrameSourceException;

}
