# Calibration

After getting the overview camera and the PTZ camera to work, we have to calibrate the system so that the two can work together correctly.

## Co-ordinates

LectureSight uses 3 co-ordinate systems:

* [Video Analysis](../modules/videoanalysis) uses the dimensions of the overview camera image (for example 640x360). Pixel counts and movement thresholds defined for object tracking therefore refer to these dimensions.

* The [Camera Operator](../modules/cameraoperator-simple) uses a normalized co-ordinate system from -1 to 1 on both the horizontal and vertical axes, where the top-left is (-1, -1), centre is (0,0) and bottom-right is (1,1). Zoom is mapped  from 0 (wide) to 1 (close).

* The [Steering Worker](../modules/steeringworker-relativemove) uses the co-ordinate system of the PTZ camera.

> [VAPIX](../modules/ptzcontrol-vapix) cameras use degrees as co-ordinates. As LectureSight manages PTZ co-ordinates as  integers, VAPIX co-ordinates are scaled up by 100 by the camera driver to preserve precision, so 37.65 degrees is represented as 3765.

## Inverted cameras

If the Overview Camera is mounted inverted (up-side down), you can invert the overview image using:

    cv.lecturesight.framesource.inverted=true

If the PTZ Camera is mounted inverted, you may need to add these properties for some [VISCA](../modules/ptzcontrol-visca) cameras:

```
cv.lecturesight.ptz.steering.worker.relativemove.xflip=true
cv.lecturesight.ptz.steering.worker.relativemove.yflip=true
```

and this property for [VAPIX](../modules/ptzcontrol-vapix) cameras:

    cv.lecturesight.vapix.camera.inverted=true

## Scene Limits

These 4 configuration properties map the overview image to the camera PTZ co-ordinates.

```
cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.left
cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.right
cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.top
cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.bottom
```

Initially these values are not set, and thus the limits of the camera's co-ordinates are used as the scene limits.

To set the scene limits, disable camera steering by using the following command on the [console](../core/console)

    cs:off
    
Move the production camera (using the PTZ Camera's remote control or web interface) so that the PTZ Camera is centred on first the top-left point of the overview image, and then the bottom-right point. Note the camera's co-ordinates at those points in the [PTZ Camera Control](../ui/cameracontrol) window. Update the `lecturesight.properties` configuration with those values, and restart LectureSight.

## Initial position

Set the initial position of the [PTZ camera](../modules/cameraoperator-simple)

```
cv.lecturesight.cameraoperator.panonly.pan=0.0
cv.lecturesight.cameraoperator.panonly.tilt=0.0
cv.lecturesight.cameraoperator.panonly.zoom=0.0
```

## Frame width

Set the frame width of the [PTZ camera](../modules/cameraoperator-simple) at the configured zoom position, relative to the width of the overview image, which is 2 in normalized co-ordinates (-1 to 1).

For example a frame.width of 0.5 means that the PTZ Camera's image is 25% of the width of the overview image (0.5 / 2).

```
cv.lecturesight.cameraoperator.panonly.frame.width=0.5
```

You can verify visually that the frame width is correct by looking at the frame boundary guides on the [PTZ Camera Control](../ui/cameracontrol) window.

## Scene Profile

Create a new [Scene Profile](../ui/profile) to restrict the area in which the system tracks objects.

