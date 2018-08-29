Frame Sources
=============

A frame source provides images to LectureSight for processing, typically
from an overview camera.

The *FrameSourceProvider* service in the
``lecturesight-framesource-impl`` bundle provides the infrastructure
responsible for managing FrameSource implementations. It discovers video
input plugins and is responsible for setting up configured
*FrameSources* with the proper input plugin.

Configuration
-------------

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| cv.lecturesight.framesource.i | v4l:///d | MRL of the video input from the   |
| nput.mrl                      | ev/video | overview camera                   |
|                               | 0[width= |                                   |
|                               | 320;heig |                                   |
|                               | ht=240]  |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.framesource.i | false    | If true, rotates the framesource  |
| nverted                       |          | 180 degrees. Used for cameras     |
|                               |          | mounted inverted (upside-down)    |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.framesource.m | 60       | Maximum fps rate at which frames  |
| axfps                         |          | should be read from the device.   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.framesource.s |          | Filename to which overview image  |
| napshot.file                  |          | snapshots should be saved         |
|                               |          | periodically                      |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.framesource.s | 60       | Interval in seconds to save       |
| napshot.interval              |          | overview image snapshots          |
+-------------------------------+----------+-----------------------------------+

The Media Resource Locator (MRL) has the following form:

.. code:: ini

    type :// path [options]

+-----------+----------------------------------------------------------------+
| Part      | Meaning                                                        |
+===========+================================================================+
| type      | the type of the input, determines which input plugin is used   |
+-----------+----------------------------------------------------------------+
| path      | path to the input, usually a Linux device or file              |
+-----------+----------------------------------------------------------------+
| options   | additional arguments for the input plugin                      |
+-----------+----------------------------------------------------------------+

V4L Frame Source
----------------

The ``lecturesight-framesource-v4l`` bundle provides a FrameSource
implementation for accquiring frames from Video4Linux and Video4Linux 2
devices such as webcams and frame grabbers.

Arguments for creation of a new FrameSource from this implementation can
be provided in the FrameSource MRL. If an argument is not present in the
MRL, the default value is taken from the configuration properties.

Usage
~~~~~

The *type* for this FrameSource implementation is *v4l* or *v4l2*. The
*path* is the path to a Linux video device such as ``/dev/video``

Available arguments are ``width, height, standard, channel, quality``.

Examples
~~~~~~~~

Video4Linux device ``/dev/video0`` as input with QVGA resolution.

.. code:: ini

        cv.lecturesight.framesource.input.mrl=v4l:///dev/video0[width=320;height=240]

Video4Linux2 device ``/dev/video0`` as input with QVGA resolution.

.. code:: ini

        cv.lecturesight.framesource.input.mrl=v4l2:///dev/video0[width=320;height=240]

Configuration
~~~~~~~~~~~~~

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| cv.lecturesight.framesource.v | 0        | Default video input. Usually not  |
| 4l.channel                    |          | used with USB webcams but rather  |
|                               |          | with capture cards. This can be   |
|                               |          | useful with capture cards, since  |
|                               |          | they are by default set to tuner  |
|                               |          | input and need to be set to       |
|                               |          | composite (usually 1).            |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.framesource.v | YUYV     | YUYV or MJPEG for webcams         |
| 4l.format                     |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.framesource.v | 0        | Default encoding quality. Only    |
| 4l.quality                    |          | used for devices that provide     |
|                               |          | encoded video streams (such as    |
|                               |          | MPEG2 or MJPEG). Value range      |
|                               |          | depends on device driver.         |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.framesource.v | 320      | Default width for input frames.   |
| 4l.resolution.width           |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.framesource.v | 240      | Default height for input frames.  |
| 4l.resolution.height          |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.framesource.v | 0        | Default video standard. Usually   |
| 4l.standard                   |          | not used with USB webcams but     |
|                               |          | rather with capture cards. Which  |
|                               |          | value indicates a certain         |
|                               |          | standard (eg. PAL-X/NTSC) depends |
|                               |          | on the driver of the video        |
|                               |          | device.                           |
+-------------------------------+----------+-----------------------------------+

V4L2 Device Controls
~~~~~~~~~~~~~~~~~~~~

It is also possible to set V4L2 device controls by including them in the
option list. To find out which controls the v4l2 device supports, look
at the device information logged by the
*V4LFrameGrabberFactory.createFrameGrabber* method. For example this
device has a control named ``Brightness`` which can be set in the range
0 to 255:

::

    2017-12-12 06:31:43.587 INFO    CM Configuration Updater (Update: pid=org.apache.felix.fileinstall.235d4e28-9777-462b-93dd-d7a91af9d18e) V4LFrameGrabberFactory.createFrameGrabber() : Name: Brightness = 128 Type: CTRL_TYPE_SLIDER Values: [ 0 .. 255 ] increment: 1

To set this value in the MRL, add it to the option list:

::

    cv.lecturesight.framesource.input.mrl=v4l2:///dev/logitech[width=640;height=360;Brightness=100]

GStreamer Frame Source
----------------------

The ``lecturesight-framesource-gst`` bundle provides a Frame Source
implementation that uses a user-defined GStreamer pipeline to capture
frames. The pipeline definition is given in the format that is used in
the ``gst-launch`` command. The implementation adds a color space
element, a capsfilter and an appsink to the user-defined pipeline that
are responsible for converting the frames to RGB format and hand them
over to the system.

Usage
~~~~~

MRLs for this Frame Source contain the GStreamer pipeline definition in
the format used in the gst-launch command.

.. code:: ini

    gst://(gst-launch definition)[(options)]

**Note:** You must define a pipeline with at least two elements,
otherwise the creation of the Frame Source will fail (issue
`LS-71 <https://opencast.jira.com/browse/LS-71>`__).

Options
~~~~~~~

**drop** -- optional, default: *true*

Sets the *drop* property of the ``appsink`` that hands the frames over
to the system.

For real-time frame sources such as cameras it is recommended to set
this value to *true* so that the newest frames is uploaded to the GPU
for video analysis.

When testing with a non-live frame source such as video files, *drop*
may be set to *false* so that the system gets every frame for analysis.

More information on the ``appsink`` element and its *drop* property are
available in the `GStreamer
documentation <http://gstreamer.freedesktop.org/data/doc/gstreamer/head/gst-plugins-base-libs/html/gst-plugins-base-libs-appsink.html>`__.

Examples
~~~~~~~~

Consume an RTSP stream from an Axis IP camera:

.. code:: ini

    cv.lecturesight.framesource.input.mrl=gst://rtspsrc location=rtsp://venue1-camera.someplace.edu/axis-media/media.amp ! rtph264depay ! avdec_h264 ! videoconvert

Use a V4L2 source:

.. code:: ini

    cv.lecturesight.framesource.input.mrl=gst://v4l2src device=/dev/video0 ! ffmpegcolorspace [drop=true]

Use a gstreamer test pattern:

.. code:: ini

    cv.lecturesight.framesource.input.mrl=gst://videotestsrc ! identity

RTPH264 Frame Source
--------------------

The ``lecturesight-framesource-rtph264`` bundle provides a FrameSource
implementation that reads H264 video from an RTP stream. This is a
special-purpose gstreamer pipeline designed to consume video from a
RaspberryPi camera with minimal latency.

Example:

.. code:: ini

        cv.lecturesight.framesource.input.mrl=rtph264://venue1-camera.someplace.edu:8554

Videofile Frame Source
----------------------

The ``lecturesight-framesource-videofile`` bundle provides a FrameSource
implementation that reads frames from a video file using gstreamer.

It depends on the set of codecs installed in the host operating systems
what formats are supported. Standard MPEG file formats should always be
supported since they are included in most standard installations.

The *type* for this FrameSource implementation is *file*. The *path* is
the path to a video file. There are no arguments for this FrameSource
implementation.

Example, using the file ``/opt/ls/media/overview.mp4`` as a frame
source:

.. code:: ini

        cv.lecturesight.framesource.input.mrl=file:///opt/ls/media/overview.mp4
