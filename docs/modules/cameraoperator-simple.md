# Pan-Only Camera Operator

The Pan-Only camera operator follows the target by moving left and right as required. The camera tilt and zoom must be
pre-configured.

# Configuration

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.cameraoperator.panonly.pan | 0.0 | Sets the initial pan position <br> (-1 to 1)
| cv.lecturesight.cameraoperator.panonly.tilt | 0.0 | Sets the initial tilt position  <br> (-1 to 1)
| cv.lecturesight.cameraoperator.panonly.zoom | 0.0 | Sets the initial zoom position  <br> (0 to 1)
| cv.lecturesight.cameraoperator.panonly.target.timeout | 2500 | Sets the time in milliseconds after the last target movement after which a target will no longer be tracked.
| cv.lecturesight.cameraoperator.panonly.tracking.timeout | 60000 | Sets the time in milliseconds after the last target movement to return to the initial tracking position  <br> (0 to disable)
| cv.lecturesight.cameraoperator.panonly.frame.trigger | 0.65
| cv.lecturesight.cameraoperator.panonly.frame.width | 0.5
| cv.lecturesight.cameraoperator.panonly.idle.preset | -1
| cv.lecturesight.cameraoperator.panonly.target.limit | 3
| cv.lecturesight.cameraoperator.panonly.target.timeout | 2500

