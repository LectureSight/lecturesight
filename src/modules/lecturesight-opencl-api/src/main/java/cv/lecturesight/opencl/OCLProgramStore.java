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

import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;

import java.util.NoSuchElementException;

public interface OCLProgramStore {

  /** Returns the CLProgram specified by name or throws a NoSuchElementException 
   *  if a program with the specified name is not stored.
   * 
   * @param name
   * @return
   * @throws NoSuchElementException
   */
  CLProgram getProgram(String name) throws NoSuchElementException;

  /** Returns the CLKernel specified by program and function name or throws
   *  a NoSuchElementException if the specified kernel is not stored.
   *
   * @param program
   * @param kernel
   * @return
   * @throws NoSuchElementException
   */
  CLKernel  getKernel(String program, String kernel) throws NoSuchElementException;

}
