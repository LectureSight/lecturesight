# Starting Lecturesight

By default LectureSight will try to use the video device `/dev/video0` as overview camera with a resolution of 320x240 pixels (QVGA). It will not try to initialize any PTZ camera.

You will see the main application window appear. The  ``Services`` menu should be populated with a number of entries.

If you only see the entry `System configuration` , it is most likely that the system was not able to initialize the OpenCL platform successfully. You can find out if the OpenCL platform was initialized correctly by looking for the OpenCL device report in the console, for example:

```
OpenCL device report:

NVIDIA Corporation NVS 315 (driver version: 375.66)

Compute units :  1 at 1046 MHz max

Memories :  global   : 964.4375 MB
constant : 64.0 KB
local    : 48.0 KB

Workgroups :  1024 threads max in 3 dimensions
2D Image size :  16384x16384 max
Work item sizes:  1024 1024 64
```

If you don't find an OpenCL device report in the console, this means that the OpenCL service was not able to find and initalize the OpenCL platform. In this case check the installation of the graphics card driver.
