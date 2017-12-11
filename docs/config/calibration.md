# Calibration

After getting the overview camera and the PTZ camera to work, we have to calibrate the system so that the two can work together correctly.

## Co-ordinates

LectureSight uses 3 co-ordinate systems:

* [Video Analysis](../modules/videoanalysis) uses the dimensions of the overview camera image (for example 640x360). Pixel counts and movement thresholds defined for object tracking therefore refer to these dimensions.

* The [Camera Operator](../modules/cameraoperator-simple) uses a normalized co-ordinate system from -1 to 1 on both the horizontal and vertical axes, where the top-left is (-1, -1), centre is (0,0) and bottom-right is (1,1). Zoom is mapped  from 0 (wide) to 1 (close).

* The [Steering Worker](../modules/steeringworker-relativemove) uses the co-ordinate system of the PTZ camera.

## Scene Bounds

## Scene Profile

First we have to tell the PTZ camera where in it's field of operation the bound of the scene lie that the overview camera observes.

Lastly, in most lecture or seminar rooms, it is a good idea to restrict the area in which the systems tracks objects.
