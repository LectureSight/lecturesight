# Frame Sources

A frame source provides images to Lecturesight for processing, typically from an overview camera.

The _FrameSourceProvider_ service in the  `lecturesight-framesource-impl` bundle provides the infrastructure responsible for managing FrameSource implementations. It discovers video input plugins and is responsible for setting up configured _FrameSources_ with the proper input plugin.

## Configuration

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
cv.lecturesight.framesource.input.mrl | v4l:///dev/video0<br>[width=320;height=240] | MRL of the video input from the overview camera
cv.lecturesight.framesource.inverted | false | Set this to true if the framesource is inverted (mounted upside-down) and should be rotated 180 degrees
cv.lecturesight.framesource.maxfps | 60 | Maximum fps rate at which frames should be read from the device
cv.lecturesight.framesource.snapshot.file | | Filename to which overview image snapshots should be saved periodically
cv.lecturesight.framesource.snapshot.interval | 60 | Interval in seconds at which overview image snapshots should be saved

 The Media Resource Locator (MRL) has the following form:

```ini
type :// path [options]
```

| MRL part  | Meaning |
|-|-|
| type | the type of the input, determines which input plugin is used |
| path | path to the input, usually a Linux device or file |
| options | additional arguments for the input plugin |

For real time operation, devices that provide raw video streams are recommended, as encoding and decoding of frames can lead to several hundred milliseconds of delay.

## V4L Framesource

The `lecturesight-framesource-v4l` bundle provides a FrameSource implementation for accquiring frames from Video4Linux and Video4Linux 2 devices.

Arguments for creation of a new FrameSource from this implementation can be provided in the FrameSource MRL. If an argument is not present in the MRL, the default value is taken from the configuration properties.

### Usage

The *type* for this FrameSource implementation is *v4l* or *v4l2*. The *path* is the path to a Linux video device such as `/dev/video`

Available arguments are `width, height, standard, channel, quality`.

### Examples

Video4Linux device `/dev/video0` as input with QVGA resolution.

```ini
    cv.lecturesight.framesource.input.mrl=v4l:///dev/video0[width=320;height=240]
```

 Video4Linux2 device `/dev/video0` as input with QVGA resolution.

```ini
    cv.lecturesight.framesource.input.mrl=v4l2:///dev/video0[width=320;height=240]
```

### Configuration

| Key                                   | Default   | Description |
|---------------------------------------|----------:|-------------------------------------------|
cv.lecturesight.framesource.v4l.channel | 0 | Default video input. Usually not used with USB webcams but rather with capture cards. This can be useful with capture cards, since they are by default set to tuner input and need to be set to composite (usually 1).
cv.lecturesight.framesource.v4l.format | YUYV | YUYV or MJPEG for webcams
cv.lecturesight.framesource.v4l.quality | 0 | Default encoding quality. Only used for devices that provide encoded video streams (such as MPEG2 or MJPEG). Value range depends on device driver.
cv.lecturesight.framesource.v4l.resolution.height | 240 | Default height for input frames.
cv.lecturesight.framesource.v4l.resolution.width | 320 | Default width for input frames.
cv.lecturesight.framesource.v4l.standard | 0 | Default video standard. Usually not used with USB webcams but rather with capture cards. Which value indicates a certain standard (eg. PAL-X/NTSC) depends on the driver of the video device.

## Gstreamer Frame Source

The `lecturesight-framesource-gst` bundle provides a Frame Source implementation that uses a user-defined
GStreamer pipeline to capture frames. The pipeline definition is given in the
format that is used in the `gst-launch` command. The implementation adds a
color space element, a capsfilter and an appsink to the user-defined pipeline
that are responsible for converting the frames to RGB format and hand them
over to the system.

### Usage

MRLs for this Frame Source contain the GStreamer pipeline definition in the
format used in the gst-launch command.

```ini
gst://(gst-launch definition)[(options)]
```

__Note:__ You must define a pipeline with at least two elements, otherwise the creation of the Frame Source will fail (issue [LS-71](https://opencast.jira.com/browse/LS-71)).

### Options

__drop__ -- optional, default: _true_

Sets the _drop_ property of the `appsink` that is handing the frames over to
the system.

For real-time frame sources such as cameras it is recommended to
have the value set to _true_ so that always the newest frames is uploaded to
the GPU for video analysis. When testing with a non-live frame source such
as video files, _drop_ may be set to _false_ so that the system gets every frame
for analysis.

More information on the `appsink` element and its _drop_ property are available
in the [GStreamer documentation](http://gstreamer.freedesktop.org/data/doc/gstreamer/head/gst-plugins-base-libs/html/gst-plugins-base-libs-appsink.html).

### Examples

Consume an RTSP stream from an Axis IP camera:

```ini
cv.lecturesight.framesource.input.mrl=gst://rtspsrc location=rtsp://venue1-camera.someplace.edu/axis-media/media.amp ! rtph264depay ! avdec_h264 ! videoconvert
```

Use a V4L2 source:

```ini
cv.lecturesight.framesource.input.mrl=gst://v4l2src device=/dev/video0 ! ffmpegcolorspace [drop=true]
```

Use a gstreamer test pattern:
```ini
cv.lecturesight.framesource.input.mrl=gst://videotestsrc ! identity
```

## RTPH264 Framesource

The `lecturesight-framesource-rtph264` bundle provides a FrameSource implementation that reads H264 video from an RTP stream. This is a special-purpose gstreamer pipeline designed to consume video from a RaspberryPi camera with minimal latency.

Example:

```ini
    cv.lecturesight.framesource.input.mrl=rtph264://venue1-camera.someplace.edu:8554
```

## Videofile Framesource

The `lecturesight-framesource-videofile` bundle provides a FrameSource implementation that reads frames from a video file using gstreamer.

It depends on the set of codecs installed in the host operating systems what formats are supported.
Standard MPEG file formats should always be supported since they are included in most standard installations.

The *type* for this FrameSource implementation is *file*. The *path* is the path to a video file.
There are no arguments for this FrameSource implementation.

Example, using the file `/opt/ls/media/overview.mp4` as a frame source:

```ini
    cv.lecturesight.framesource.input.mrl=file:///opt/ls/media/overview.mp4
```

