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

import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLIntBuffer;

public interface OCLUtils {

  CLEvent setValues(int x, int y, int width, int height, CLImage2D image, int r, int g, int b, int a);
  CLEvent setValues(int x, int y, int width, int height, CLImage2D image, int val);
  CLEvent setValues(int start, int end, CLIntBuffer buffer, int val);

  CLEvent copyImage(int x, int y, int width, int height, CLImage2D source, int dest_x, int dest_y, CLImage2D dest);
}
