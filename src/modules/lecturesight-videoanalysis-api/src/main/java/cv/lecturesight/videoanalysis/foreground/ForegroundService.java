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
package cv.lecturesight.videoanalysis.foreground;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.opencl.CLImageDoubleBuffer;
import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.image.BufferedImage;

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
  
  BufferedImage getForegroundMapHost();
  
  /** Returns the foreground map working buffer.
   * 
   * @return foreground map working buffer
   */
  CLImageDoubleBuffer getForegroundWorkingBuffer();
  
  /** Returns the connected component labeler of this foreground service.
   * 
   * @return ConnectedComponentLabeler
   */
  ConnectedComponentLabeler getLabeler();

  int getActivity(int id);
}
