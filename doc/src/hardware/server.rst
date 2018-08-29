Server Requirements
===================

Operating System
~~~~~~~~~~~~~~~~

LectureSight is designed to run on a Linux system equipped with a GPU
and running a GUI desktop. Ubuntu 16.04 or later is recommended.

GPU
~~~

The video processing portions of the system have been implemented for
the GPU using the OpenCL standard for cross-platform parallel computing.
Most modern graphics cards are compatible with OpenCL. Graphics cards
with an NVIDIA GPU that are label as *CUDA compatible* are OpenCL
compatible. ATI graphics chips that are labeled *Stream SDK compatible*
are also compatible with OpenCL.

For use in a real-time scenario, it is suggested to use a GPU with a
least six OpenCL compute units and at least 512 MB of graphics memory.
Such a configuration can be found, for example, in graphics cards
equipped with the NVIDIA GT 220 chip set.

For NVIDIA GPUs, the number of CUDA units divided by eight yields the
number of OpenCL compute units. For example, the GT 220 has 48 CUDA
units, thus the GPU provides 6 compute units in OpenCL.

The GPU must support the following image formats (see
`LS-165 <http://opencast.jira.com/browse/LS-165>`__)

-  BGRA / UnsignedInt8
-  INTENSITY / UnsignedInt8

The following GPUs are known to work with LectureSight:

-  NVIDIA NVS 310, NVS 315, GT 220, GeForce GTX 750 Ti

These GPUs do not support the image formats required by LectureSight and
thus can not be used:

-  AMD ATI Radeon HD 6770M (older iMac)
-  NVIDIA GeForce GT 750M (newer iMac)
-  Intel HD Graphics 5000 (Mac Air)

CPU
~~~

LectureSight was designed with the goal of being capable of running on a
single 2 GHz core. The system should not take up all CPU resources in a
modern system so that video recording software can run alongside on the
same computer. Thus LectureSight should run on any modern system. The
suggestion is to use a system with a CPU of at least the performance of
an Intel Core 2 Duo with 2,2 Ghz.

RAM
~~~

Since video ananlysis is nearly entirely done on the graphics card,
there are no special memory requirements for LectureSight when run
stand-alone. The usual default memory configuration for the Java VM
suffice.

Storage
~~~~~~~

LectureSight requires less than 50 MB of disk space when installed.
Additional space may be used by `log files <../core/logging.md>`__ or
`metrics <../core/metrics.md>`__ if configured. No other data is
incrementally saved.
