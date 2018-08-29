OpenCL
======

The *OpenCLService* in the ``lecturesight-opencl-impl`` bundle is
responsible for initializing the GPU.

OpenCL is configured through Java system properties that are set in the
``start_lecturesight.sh`` script. To change these properties, edit the
script before starting LectureSight.

Configuration
-------------

+-------------------+-----------+---------------------------------------+
| Property          | Default   | Description                           |
+===================+===========+=======================================+
| ocl.device.type   | GPU       | Set the *CLDevice.Type*: CPU or GPU   |
+-------------------+-----------+---------------------------------------+
| ocl.use.gl        | false     | Use current OpenGL context if true    |
+-------------------+-----------+---------------------------------------+
| ocl.profiling     | false     | Enable profiling if true              |
+-------------------+-----------+---------------------------------------+

Profiling
---------

If OpenCL profiing is enabled, LectureSight will save profiling
information to the files ``frametimes-TIMESTAMP.csv`` and
``profiling-TIMESTAMP.csv``.
