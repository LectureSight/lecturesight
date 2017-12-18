# Welcome to LectureSight

LectureSight is an open source OSGI application that uses OpenCL to analyze a video stream in real-time and track the presenter by controlling a PTZ camera.

## Quick Start

1. Check the requirements for a [Linux server with GPU](hardware/server), [overview camera](hardware/overview-camera) such as a webcam, and [PTZ camera](hardware/ptz-camera) supporting VISCA or VAPIX.
1. Check that you have the [software dependencies](install/dependencies) installed (Java, OpenCL and optionally GStreamer).
1. Install LectureSight from a [release](install/release) or [build from source](install/source).
1. Update the default configuration in `conf/lecturesight.properties` for your overview camera (see some [examples](config/examples)).
1. [Start LectureSight](install/start).
1. [Calibrate](config/calibration) LectureSight so that the PTZ camera can successfully follow objects within the overview camera's field of view.
1. Set up a [Scene Profile](ui/profile) to ignore irrelevant regions of the overview image.
1. Watch LectureSight in action in the [Object Tracker](ui/objecttracker) and [PTZ Camera Control](ui/cameracontrol) windows, and fine-tune the [configuration](ui/config) to optimize tracking performance and camera movement.
1. Configure LectureSight to start and stop tracking automatically using the [Scheduler](core/scheduler).

## Community

Join the LectureSight community to ask for help, provide feedback or give suggestions.

Email [lecturesight@googlegroups.com](mailto:lecturesight@googlegroups.com)

Subscribe by sending a mail to: [lecturesight+subscribe@googlegroups.com](mailto:lecturesight+subscribe@googlegroups.com](mailto:))

## Issues

Report bugs or file feature requests on the LectureSight [JIRA Issue Tracker](http://opencast.jira.com/browse/LS)
