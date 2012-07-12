package cv.lecturesight.videoanalysis.foreground;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.opencl.CLImageDoubleBuffer;
import cv.lecturesight.opencl.api.OCLSignal;

/** A Foreground Service is responsible for creating a binary map assigning each
 * pixel in the input image either to the background or a foreground region. 
 * Pixels belonging to the background must have a value of 0, foreground pixels
 * must have a value >0.
 * 
 */
public interface ForegroundService {

  enum Signal {
    DONE_ADDSUB,      // signal indicating that the foreground map has been updated
    DONE_CLEANING     // signal indicating that the foreground map has been cleaned
  }

  /** Returns the <code>OCLSignal<code> for the provided signal name
   * 
   * @param Signal name
   * @return OCLSignal for given name
   */
  OCLSignal getSignal(Signal signal);
  
  /** Returns the update map image.
   * 
   * @return CLImage2D update map
   */
  CLImage2D getUpdateMap();
  
  /** Returns the foreground binary map.
   * 
   * @return CLImage2D foreground map
   */
  CLImage2D getForegroundMap();
  
  /** Returns the foreground map working buffer.
   * 
   * 
   */
  CLImageDoubleBuffer getForegroundWorkingBuffer();
  
  /** Returns the connected component labeler of this foreground service.
   * 
   * @return ConnectedComponentLabeler
   */
  ConnectedComponentLabeler getLabeler();

}
