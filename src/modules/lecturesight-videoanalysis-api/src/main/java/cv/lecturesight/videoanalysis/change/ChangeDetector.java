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
package cv.lecturesight.videoanalysis.change;

import com.nativelibs4java.opencl.CLImage2D;
import cv.lecturesight.opencl.api.OCLSignal;

/** A ChangeDetector service is responsible for computing a binary map in which
 * pixels that changed since the last frame are marked. Pixels that changed must
 * have a value >0, all others zero.
 * 
 */
public interface ChangeDetector {

  enum Signal {
    DONE_DETECTION,
    DONE_CLEANING
  };

  OCLSignal getSignal(Signal signal);

  CLImage2D getChangeMapRaw12();
  CLImage2D getChangeMapRaw23();
  CLImage2D getChangeMapRaw13();
  CLImage2D getChangeMapDilated();
}
