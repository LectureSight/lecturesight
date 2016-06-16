# Relative Move Steering Worker

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
