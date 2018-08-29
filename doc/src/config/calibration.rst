Calibration
===========

After getting the overview camera and the PTZ camera to work, we have to
calibrate the system so that the two can work together correctly.

Co-ordinates
------------

LectureSight uses 3 co-ordinate systems:

-  `Video Analysis <../modules/videoanalysis>`__ uses the dimensions of
   the overview camera image (for example 640x360). Pixel counts and
   movement thresholds defined for object tracking therefore refer to
   these dimensions.

-  The `Camera Operator <../modules/cameraoperator-ptz>`__ uses a
   normalized co-ordinate system from -1 to 1 on both the horizontal and
   vertical axes, where the top-left is (-1, -1), centre is (0,0) and
   bottom-right is (1,1). Zoom is mapped from 0 (wide) to 1 (close).

-  The `Steering Worker <../modules/steeringworker-relativemove>`__ uses
   the co-ordinate system of the PTZ camera.

    `VAPIX <../modules/ptzcontrol-vapix>`__ cameras use degrees as
    co-ordinates. As LectureSight manages PTZ co-ordinates as integers,
    VAPIX co-ordinates are scaled up by 100 by the camera driver to
    preserve precision, so 37.65 degrees is represented as 3765.

Inverted cameras
----------------

If the Overview Camera is mounted inverted (up-side down), you can
invert the overview image using:

::

    cv.lecturesight.framesource.inverted=true

If the PTZ Camera is mounted inverted, you may need to add these
properties for some `VISCA <../modules/ptzcontrol-visca>`__ cameras:

::

    cv.lecturesight.ptz.steering.worker.relativemove.xflip=true
    cv.lecturesight.ptz.steering.worker.relativemove.yflip=true

and this property for `VAPIX <../modules/ptzcontrol-vapix>`__ cameras:

::

    cv.lecturesight.vapix.camera.inverted=true

Scene Profile
-------------

Create a new `Scene Profile <../ui/profile>`__ to restrict the area in
which the system tracks objects.

Scene Limits
------------

These 4 configuration properties map the overview image to the camera
PTZ co-ordinates.

::

    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.left
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.right
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.top
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.bottom

Initially these values are not set, and thus the limits of the camera's
co-ordinates are used as the scene limits.

Manual Calibration
~~~~~~~~~~~~~~~~~~

To set values for the four scene limits, disable camera steering by
using the following command on the `console <../core/console>`__

::

    cs:off

Move the production camera (using the PTZ Camera's remote control or web
interface) first so that the PTZ Camera is centred on the top-left point
of the overview image, and then on the bottom-right point.

Note the camera's co-ordinates at those points in the `PTZ Camera
Control <../ui/cameracontrol>`__ window. Update the
``lecturesight.properties`` configuration with those values, and restart
LectureSight.

Marker Calibration
~~~~~~~~~~~~~~~~~~

!!! info "Automatic marker calibration is only supported for
`VAPIX <../modules/ptzcontrol-vapix>`__ cameras."

Marker calibration maps overview co-ordinates to camera pan and tilt
values by matching positions on the overview image with camera preset
positions.

    The calibration process creates horizontal and vertical conversion
    models using `spline
    interpolation <https://en.wikipedia.org/wiki/Spline_interpolation>`__
    inside the range of the marker points, and linear extrapolation
    outside the range, to convert between overview image co-ordinates
    and camera positions. This will in general provide more accurate
    results than setting the scene limits manually.

To set up marker calibration:

-  In the `Scene Profile Editor <../ui/profile#calibration-markers>`__,
   identify 3 or more fixed points on the image (for example the corner
   of a fixed blackboard, or a light switch). The set of points should
   cover most of the horizontal and vertical range of the presentation
   area.
-  Create calibration markers at each point. Give each marker a name,
   for example ``m1``, ``m2``, ``m3``, ``m4``, ``m5``, and save the
   profile.
-  In the camera web interface, create a set of presets with the same
   names as the calibration markers. At each preset, the camera should
   be centred on the venue feature identified by the matching
   calibration marker.
-  Restart LectureSight, or use the `console <../core/console>`__
   command ``cs:calibrate`` to trigger marker calibration:

::

    g! cs:calibrate
    Automatic calibration completed

The calibration process discovers the pan and tilt co-ordinates of each
preset by moving the camera in turn to each position, with a pause time
of 2.5s between presets.

On subsequent startup, the `Steering
Worker <../modules/steeringworker-relativemove>`__ will report whether
automatic calibration has been used, and if successful, the values of
the calculated pan and tilt thresholds:

::

    Automatic calibration, camera pan/tilt limits: pan -3673 to 3649, tilt -2596 to 2348

Once automatic calibration has successfully run, changes to the
configured scene limits will have no effect, as the calculated values
will be used.

If no calibration markers have been created or there are too few
matching markers and presets, the log will include:

::

    Automatic calibration not possible

This can be caused by markers that are too close together in the x or y
axes. Try to adjust the marker positions so they are distributed across
the field of view, both horizontally and vertically.

Initial position
----------------

Set the initial position of the `PTZ
camera <../modules/cameraoperator-ptz>`__

::

    cv.lecturesight.cameraoperator.ptz.pan=0.0
    cv.lecturesight.cameraoperator.ptz.tilt=0.0
    cv.lecturesight.cameraoperator.ptz.zoom=0.0

Frame width
-----------

Set the frame width of the `PTZ
camera <../modules/cameraoperator-ptz>`__ at the configured zoom
position, relative to the width of the overview image, which is 2 in
normalized co-ordinates (-1 to 1).

For example a frame.width of 0.5 means that the PTZ Camera's image is
25% of the width of the overview image (0.5 / 2).

::

    cv.lecturesight.cameraoperator.ptz.frame.width=0.5

You can verify visually that the frame width is correct by looking at
the frame boundary guides on the `PTZ Camera
Control <../ui/cameracontrol>`__ window.
