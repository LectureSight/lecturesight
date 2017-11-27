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
package cv.lecturesight.opencl;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;

/** A collection of utility functions. All functions return a CLEvent so they
 * can be synchronized with other CL commands.
 *
 * @author wulff
 */
public interface OCLUtils {

  /** Set pixel values in a specified rectangle in an RGBA image
   *
   * @param x x position of upper left corner
   * @param y y position of upper left corner
   * @param width width of rectangle
   * @param height height of rectangle
   * @param image image to be changed
   * @param r red value of pixels
   * @oaram g green value of pixels
   * @param b blue value of pixels
   * @param a alpha value of pixels
   * @return CLEvent indicating completion
   */
  CLEvent setValues(int x, int y, int width, int height, CLImage2D image, int r, int g, int b, int a);

  /** Set pixel values in a specified rectangle in an INTENSITY image
   *
   * @param x x position of upper left corner
   * @param y y position of upper left corner
   * @param width width of rectangle
   * @param height height of rectangle
   * @param image image to be changed
   * @param val pixel value
   * @return CLEvent indicating completion
   */
  CLEvent setValues(int x, int y, int width, int height, CLImage2D image, int val);

  /** Set values of a two-dimensional int buffer.
   *
   * @param start start offset
   * @param end end offset
   * @param buffer buffer to be changed
   * @param val value to be set
   * @return CLEvent indicating completion
   */
  CLEvent setValues(int start, int end, CLBuffer<Integer> buffer, int val);

  /** Copy a defined rectangle from one image into another image. The two images
   * must be of same type an channel count.
   *
   * @param x x position of upper left corner
   * @param y y position of upper left corner
   * @param width width of rectangle
   * @param height height of rectangle
   * @param source source image
   * @param dest_x x position in destination image
   * @param dest_y y position in destination image
   * @param dest destination image
   * @return CLEvent indicating completion
   */
  CLEvent copyImage(int x, int y, int width, int height, CLImage2D source, int dest_x, int dest_y, CLImage2D dest);
}
