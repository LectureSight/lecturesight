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
package cv.lecturesight.opencl.impl;

import cv.lecturesight.opencl.OCLProgramStore;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class SimpleOCLProgramStore implements OCLProgramStore {

  private Map<String, CLProgram> programs = new HashMap<String,CLProgram>();

  public SimpleOCLProgramStore(Map<String, CLProgram> programs) {
    this.programs.putAll(programs);
  }

  @Override
  public CLProgram getProgram(String name) throws NoSuchElementException {
    CLProgram prog;
    if (programs.containsKey(name)) {
      prog = programs.get(name);
    } else {
      throw new NoSuchElementException("Unknown program: " + name);
    }
    return prog;
  }

  @Override
  public CLKernel getKernel(String program, String kernel) throws NoSuchElementException {
    CLProgram prog = getProgram(program);
    try {
      return prog.createKernel(kernel);
    } catch (CLBuildException ex) {
      throw new NoSuchElementException("Unable to create kernel " + kernel + ": " + ex.getMessage());
    }
  }
}
