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
package cv.lecturesight.framesource.impl;

import cv.lecturesight.opencl.api.OCLSignal;

import com.nativelibs4java.opencl.CLImage2D;

import java.awt.image.BufferedImage;
import java.nio.Buffer;

public interface FrameUploader {

  String SIG_NEWFRAME = "framesource.NEWFRAME";

  OCLSignal getSignal();

  CLImage2D getRawOutputImage();

  CLImage2D getOutputImage();

  CLImage2D getLastOutputImage();

  BufferedImage getOutputImageHost();

  void upload(Buffer frame);

  void setMask(BufferedImage mask);

  void destroy();
}
