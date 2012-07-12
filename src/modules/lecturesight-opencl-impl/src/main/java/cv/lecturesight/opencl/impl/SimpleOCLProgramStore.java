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
