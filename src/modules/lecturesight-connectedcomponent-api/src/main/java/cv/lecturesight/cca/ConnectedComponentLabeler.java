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
package cv.lecturesight.cca;

import cv.lecturesight.opencl.api.OCLSignal;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLImage2D;

public interface ConnectedComponentLabeler {

  enum Signal {
    START, ITERATE, FINISH, DONE
  }

  OCLSignal getSignal(Signal signal);
  void doLabels();
  void doLabels(OCLSignal trigger);
  CLImage2D getLabelImage();
  CLBuffer<Integer> getLabelBuffer();
  CLBuffer<Integer> getIdBuffer();
  CLBuffer<Integer> getSizeBuffer();
  void setMinBlobSize(int numPixels);
  void setMaxBlobSize(int numPixels);
  int getNumBlobs();
  int[] getLabels();
  int[] getSizes();
  int getSize(int id);
  int getMaxBlobs();
  void dispose();
  void setInput(CLImage2D image);
}
