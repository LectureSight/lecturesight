package cv.lecturesight.opencl;

import com.nativelibs4java.opencl.CLImage;

public class CLImageDoubleBuffer {

    CLImage current, last;

    public CLImageDoubleBuffer(CLImage img1, CLImage img2) {
      current = img1;
      last = img2;
    }

    public void swap() {
      CLImage tmp = current;
      current = last;
      last = tmp;
    }

    public CLImage current() {
      return current;
    }

    public CLImage last() {
      return last;
    }
}
