# Relative Move Steering Worker

## Configuration

cv.lecturesight.ptz.steering.worker.relativemove.autostart | true
cv.lecturesight.ptz.steering.worker.relativemove.focus.fixed | false
cv.lecturesight.ptz.steering.worker.relativemove.interval | 200
cv.lecturesight.ptz.steering.worker.relativemove.move.alpha.x | 400
cv.lecturesight.ptz.steering.worker.relativemove.move.alpha.y | 400
cv.lecturesight.ptz.steering.worker.relativemove.move.damp.pan | 1.0
cv.lecturesight.ptz.steering.worker.relativemove.move.damp.tilt | 1.0
cv.lecturesight.ptz.steering.worker.relativemove.move.initial.delay | 2500
cv.lecturesight.ptz.steering.worker.relativemove.move.stop.x | 10
cv.lecturesight.ptz.steering.worker.relativemove.move.stop.y | 10
cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.bottom | none
cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.left | none
cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.right | none
cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.top | none
cv.lecturesight.ptz.steering.worker.relativemove.xflip | false
cv.lecturesight.ptz.steering.worker.relativemove.yflip | false

### cv.lecturesight.ptz.steering.worker.autostart

*Default:* true

Controls if the camera steering is active when the module is started.

### cv.lecturesight.ptz.steering.worker.yflip

*Default:* false

This property tells the system if the PTZ is mounted up-side down. If this value
is set to *true* the coordinate system is flipped.

### cv.lecturesight.ptz.steering.worker.move.alpha.x

*Default:* 400

This value controls the size of the horizontal environment in which the camera's
pan speed decreased.

### cv.lecturesight.ptz.steering.worker.move.alpha.y

*Default:* 400

This value controls the size of the vertical environment in which the camera's
tilt speed decreased.

### cv.lecturesight.ptz.steering.worker.move.damp.pan

*Default:* 1.0

Dampening value for the camera's pan movement. This value controls the maximum
pan speed of the camera. If set to 1.0 the camera's maximum movement speed will
be the maximum speed possible with the particular hardware.

### cv.lecturesight.ptz.steering.worker.move.damp.tilt

*Default:* 1.0

Dampening value for the camera's tilt movement. This value controls the maximum
tilt speed of the camera. If set to 1.0 the camera's maximum movement speed will
be the maximum speed possible with the particular hardware.

### cv.lecturesight.ptz.steering.worker.scene.limit.left

*Default:* none

This value sets the left most limit of the scene from the overview image in the
camera coordinate system.

### cv.lecturesight.ptz.steering.worker.scene.limit.right

*Default:* none

This value sets the right most limit of the scene from the overview image in the
camera coordinate system.

### cv.lecturesight.ptz.steering.worker.scene.limit.top

*Default:* none

This value sets the top most limit of the scene from the overview image in the
camera coordinate system.

### cv.lecturesight.ptz.steering.worker.scene.limit.bottom

*Default:* none

This value sets the bottom limit of the scene from the overview image in the
camera coordinate system.

## Commands

**Command domain:** cs

### cs:on

Activates the camera steering.

### cs:off

Deactivates the camera steering.

### cs:move *pan* *tilt*

Make the camera move the specified pan and tilt coordinates. The coordinates are
normalized in a value range between -1.0 and 1.0, where

* pan: -1.0 left most, 1.0 right most
* tilt: -1.0 bottom., 1.0 top most

### cs:home

Makes the camera move to its home position. For most models this will be pan=0.0
and tilt=0.0.

### cs:zoom *zoom*

Makes the camera set the specified zoom. The zoom value is normalized to a value
range of 0.0 to 1.0. where

* 0.0 neutral
* 1.0 maximal zoom
