# Camera Operator

The Pan-Only Camera Operator follows target by moving the camera left and right as required.

If multiple targets are provided by the object tracker, the oldest active target is selected. Camera tilt and zoom are fixed and must be configured.

The Pan-Only Camera Operator is provided by the  `lecturesight-cameraoperator-simple` bundle.

## Configuration

> Pan, tilt and zoom positions are specified in the normalized co-ordinate system, which represents the camera's pan and tilt positions as ranging from -1 to 1, and the zoom position as ranging from 0 to 1.

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.cameraoperator.panonly.pan | 0.0 | Sets the initial pan position<br> (-1 to 1)
| cv.lecturesight.cameraoperator.panonly.tilt | 0.0 | Sets the initial tilt position  <br> (-1 to 1)
| cv.lecturesight.cameraoperator.panonly.zoom | 0.0 | Sets the initial zoom position  <br> (0 to 1)
| cv.lecturesight.cameraoperator.panonly.frame.width | 0.5 | The width of the PTZ camera frame at the configured zoom level, relative to the overview image<br>(0 to 2)
| cv.lecturesight.cameraoperator.panonly.frame.trigger | 0.65 | The trigger point in the PTZ frame at which the camera should start to move, as a proportion of the frame width. 0=move camera when target moves, 1=move camera only when target reaches the frame edge<br>(0 to 1)
| cv.lecturesight.cameraoperator.panonly.target.limit | 3 | Do not acquire a new tracking target if the total number of available targets exceeds this threshold. This avoids excessive camera movement during busy scenes.
| cv.lecturesight.cameraoperator.panonly.target.timeout | 2500 | Sets the time in milliseconds after the last target movement after which a target will no longer be tracked.
| cv.lecturesight.cameraoperator.panonly.tracking.timeout | 60000 | Time in ms after the last target movement to return to the initial tracking position or the idle preset<br> (0 to disable)
| cv.lecturesight.cameraoperator.panonly.idle.preset | -1 | The camera preset to move to when idle (-1 to disable)
