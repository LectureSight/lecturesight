# Simple Pan-Tilt-Zoom Camera Operator

### cv.lecturesight.cameraoperator.ptz.pan

*Default:* 0.0 (-1.0 to 1.0)

Sets the initial pan position.

### cv.lecturesight.cameraoperator.ptz.tilt

*Default:* 0.0

Sets the initial tilt position.

### cv.lecturesight.cameraoperator.ptz.tilt.lock

*Default:* false

Sets whether the tilting is disabled or not

### cv.lecturesight.cameraoperator.ptz.tilt.offset

*Default:* 0.0

Adjust the target's tilt value, for example if you want the camera to centre on
the torso, not the head. Ignored if _cv.lecturesight.cameraoperator.ptz.tilt.lock=true_

### cv.lecturesight.cameraoperator.ptz.zoom

*Default:* 0.0

Sets the initial zoom position in normalized coordinates

### cv.lecturesight.cameraoperator.ptz.frame.width

*Default:* 0.5

Sets the width of the PTZ camera's frame in normalized coordinates

###cv.lecturesight.cameraoperator.ptz.frame.height

*Default:* 0.5

Sets the height of the PTZ camera's frame in normalized coordinates

### cv.lecturesight.cameraoperator.ptz.frame.trigger.width

*Default:* 0.65

Sets the proportion of the frame width in which the target object can move without triggering the
camera to move.

###cv.lecturesight.cameraoperator.ptz.frame.trigger.height

*Default:* 0.8

Sets the proportion of the frame height in which the target object can move without triggering the
camera to move.

### cv.lecturesight.cameraoperator.ptz.target.timeout

*Default:* 2500

Sets the time in milliseconds after the last target movement after which a
target will no longer be tracked.

### cv.lecturesight.cameraoperator.ptz.tracking.timeout

*Default:* 60000 (0 to disable)

Sets the time in milliseconds after the last target movement to return to the
initial tracking position.

