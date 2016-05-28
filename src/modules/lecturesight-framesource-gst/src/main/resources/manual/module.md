
# Configurable GStreamer Frame Source
---

__Bundle File:__ lecturesight-framesource-gst.jar

This bundle provides a Frame Source implementation that uses a user-defined
GStreamer pipeline to capture frames. The pipeline definition is given in the
format that is used in the `gst-launch` command. The implementation adds a
color space element, a capsfilter and an appsink to the user-defined pipeline
that are responsible for converting the frames to RGB format and hand them
over to the system.

## Usage

MRLs for this Frame Source contain the GStreamer pipeline definition in the
format used in the gst-launch command.

```
gst://(gst-launch definition)[(options)]
```

Example:

```
cv.lecturesight.framesource.input.mrl=gst://v4l2src device=/dev/video0 ! ffmpegcolorspace [drop=true]
```

> __Notice:__ You must define a pipeline with at least two elements, otherwise the creation of the Frame Source will fail.
> The problem is already filed as [bug LS-71](https://opencast.jira.com/browse/LS-71)

## Options

__drop__ -- optional, default: _true_

Sets the _drop_ property of the `appsink` that is handing the frames over to
the system.

For real-time FrameSources such as cameras it is recommended to
have the value set to _true_ so that always the newest frames is uploaded to
the GPU for video analysis.When doing testing with a non-live FrameSource such
as video files, _drop_ may be set to _false_ so that the system gets every frame
for analysis.

More information on the `appsink` element and its _drop_ property are available
in the [GStreamer documentation](http://gstreamer.freedesktop.org/data/doc/gstreamer/head/gst-plugins-base-libs/html/gst-plugins-base-libs-appsink.html).
