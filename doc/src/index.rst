Welcome to LectureSight
=======================

LectureSight is an open source OSGI application that uses OpenCL to
analyze a video stream in real-time and track the presenter by
controlling a PTZ camera.

Quick Start
-----------

1. Check the requirements for a `Linux server with
   GPU <hardware/server>`__, `overview
   camera <hardware/overview-camera>`__ such as a webcam, and `PTZ
   camera <hardware/ptz-camera>`__ supporting VISCA or VAPIX.
2. Check that you have the `software
   dependencies <install/dependencies>`__ installed (Java, OpenCL and
   optionally GStreamer).
3. Install LectureSight from a `release <install/release>`__ or `build
   from source <install/source>`__.
4. Update the default configuration in ``conf/lecturesight.properties``
   for your overview camera (see some `examples <config/examples>`__).
5. `Start LectureSight <install/start>`__.
6. `Calibrate <config/calibration>`__ LectureSight so that the PTZ
   camera can successfully follow objects within the overview camera's
   field of view.
7. Set up a `Scene Profile <ui/profile>`__ to ignore irrelevant regions
   of the overview image.
8. Watch LectureSight in action in the `Object
   Tracker <ui/objecttracker>`__ and `PTZ Camera
   Control <ui/cameracontrol>`__ windows, and fine-tune the
   `configuration <ui/config>`__ to optimize tracking performance and
   camera movement.
9. Configure LectureSight to start and stop tracking automatically using
   the `Scheduler <core/scheduler>`__.

Community
---------

Join the LectureSight community to ask for help, provide feedback or
give suggestions.

Email lecturesight@googlegroups.com

Subscribe by sending a mail to:
`lecturesight+subscribe@googlegroups.com <mailto:lecturesight+subscribe@googlegroups.com%5D(mailto:)>`__

Issues
------

Report bugs or file feature requests on the LectureSight `JIRA Issue
Tracker <http://opencast.jira.com/browse/LS>`__

Contents
=========

.. toctree::
   :caption: Table of Contents
   :glob:
   :maxdepth: 1

   config/index
   core/index
   develop/index
   hardware/index
   install/index
   modules/index
   ui/index
