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
