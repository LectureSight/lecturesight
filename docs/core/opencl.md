# OpenCL

## Configuration

TODO: Code and default.properties are inconsistent.

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.opencl.context.opengl | false | |
| cv.lecturesight.opencl.queue.profiling | false | Enable profiling |

## Startup options

Also start_lecturesight.sh

```
# OpenCL device type to be used. Default is GPU.
# Available options: CPU, GPU, ACCELERATOR, DEFAULT, ALL
OPENCL_DEVICE="GPU"
OPENCL_OPTS="-Docl.device.type=$OPENCL_DEVICE -Docl.profiling=no"
```
