package cv.lecturesight.opencl;

import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLBuffer;

public interface OCLUtils {

  CLEvent setValues(int x, int y, int width, int height, CLImage2D image, int r, int g, int b, int a);
  CLEvent setValues(int x, int y, int width, int height, CLImage2D image, int val);
  CLEvent setValues(int start, int end, CLBuffer<Integer> buffer, int val);

  CLEvent copyImage(int x, int y, int width, int height, CLImage2D source, int dest_x, int dest_y, CLImage2D dest);
}
