# Pan-Tilt-Zoom Camera Operator

The Pan-Tilt-Zoom Camera Operator follows target by moving the camera left,
right,up and down as required. This camera operator currently only makes changes
to the camera's pan and tilt. Zooming, for now, is locked at the initial value.

This camera operator can be restricted to "pan only" by setting the property
_cv.lecturesight.cameraoperator.ptz.tilt.lock = true_

## Configuration

> Pan, tilt and zoom positions are specified in the normalized co-ordinate system, which represents the camera's pan and tilt positions as ranging from -1 to 1, and the zoom position as ranging from 0 to 1.

| Key                                   | Default | Description |
|---------------------------------------|---------|-------------------------------------------|
|cv.lecturesight.cameraoperator.ptz.pan | 0.0 | Sets the initial pan position (-1 to 1)|
|cv.lecturesight.cameraoperator.ptz.tilt | 0.0 | Sets the initial tilt position (-1 to 1)|
|cv.lecturesight.cameraoperator.ptz.tilt.lock | false | Sets whether the tilting is disabled or not|
|cv.lecturesight.cameraoperator.ptz.tilt.offset| 0.0 | Adjust the target's tilt value, for example if you want the camera to centre on the torso, not the head. Ignored if _tilt.lock=true_ (-1 to 1)
|cv.lecturesight.cameraoperator.ptz.zoom | 0.0 | Sets the initial zoom position (0 to 1)
|cv.lecturesight.cameraoperator.ptz.frame.width | 0.5 | Sets the width of the PTZ camera's frame (0 to 2)
|cv.lecturesight.cameraoperator.ptz.frame.height | 0.5 | Sets the height of the PTZ camera's frame (0 to 2)
|cv.lecturesight.cameraoperator.ptz.frame.trigger.width | 0.65 | Sets the proportion of the frame width in which the target object can move without triggering the camera to move (0 to 1)
|cv.lecturesight.cameraoperator.ptz.frame.trigger.height | 0.8 | Sets the proportion of the frame height in which the target object can move without triggering the camera to move (0 to 1)
|cv.lecturesight.cameraoperator.ptz.target.timeout | 2500 | Sets the time in milliseconds after the last target movement after which a target will no longer be tracked.
|cv.lecturesight.cameraoperator.ptz.tracking.timeout | 60000 | Sets the time in milliseconds after the last target movement to return to the initial tracking position (0 to disable)
|cv.lecturesight.cameraoperator.ptz.idle.preset | | The camera preset to move to when idle, if set
|cv.lecturesight.cameraoperator.ptz.start.preset | | The camera preset to move to at start of tracking (used instead of pan, tilt and zoom values above)

