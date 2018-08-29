Pan-Tilt-Zoom Camera Operator
=============================

The Pan-Tilt-Zoom Camera Operator follows target by moving the camera
left, right,up and down as required. This camera operator currently only
makes changes to the camera's pan and tilt. Zooming, for now, is locked
at the initial value.

This camera operator can be restricted to "pan only" by setting the
property *cv.lecturesight.cameraoperator.ptz.tilt.lock = true*

Configuration
-------------

    Pan, tilt and zoom positions are specified in the normalized
    co-ordinate system, which represents the camera's pan and tilt
    positions as ranging from -1 to 1, and the zoom position as ranging
    from 0 to 1.

+--------------------------------+---------+-----------------------------------+
| Key                            | Default | Description                       |
+================================+=========+===================================+
| cv.lecturesight.cameraoperator | 0.0     | Sets the initial pan position (-1 |
| .ptz.pan                       |         | to 1)                             |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator | 0.0     | Sets the initial tilt position    |
| .ptz.tilt                      |         | (-1 to 1)                         |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator | false   | Sets whether the tilting is       |
| .ptz.tilt.lock                 |         | disabled or not                   |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator | 0.0     | Adjust the target's tilt value,   |
| .ptz.tilt.offset               |         | for example if you want the       |
|                                |         | camera to centre on the torso,    |
|                                |         | not the head. Ignored if          |
|                                |         | *tilt.lock=true* (-1 to 1)        |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator | 0.0     | Sets the initial zoom position (0 |
| .ptz.zoom                      |         | to 1)                             |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator | 0.5     | Sets the width of the PTZ         |
| .ptz.frame.width               |         | camera's frame (0 to 2)           |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator | 0.5     | Sets the height of the PTZ        |
| .ptz.frame.height              |         | camera's frame (0 to 2)           |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator | 0.65    | Sets the proportion of the frame  |
| .ptz.frame.trigger.width       |         | width in which the target object  |
|                                |         | can move without triggering the   |
|                                |         | camera to move (0 to 1)           |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator | 0.8     | Sets the proportion of the frame  |
| .ptz.frame.trigger.height      |         | height in which the target object |
|                                |         | can move without triggering the   |
|                                |         | camera to move (0 to 1)           |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator | 2500    | Sets the time in milliseconds     |
| .ptz.target.timeout            |         | after the last target movement    |
|                                |         | after which a target will no      |
|                                |         | longer be tracked.                |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator | 60000   | Sets the time in milliseconds     |
| .ptz.tracking.timeout          |         | after the last target movement to |
|                                |         | return to the initial tracking    |
|                                |         | position (0 to disable)           |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator |         | The camera preset to move to when |
| .ptz.idle.preset               |         | idle, if set                      |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.cameraoperator |         | The camera preset to move to at   |
| .ptz.start.preset              |         | start of tracking (used instead   |
|                                |         | of pan, tilt and zoom values      |
|                                |         | above)                            |
+--------------------------------+---------+-----------------------------------+
