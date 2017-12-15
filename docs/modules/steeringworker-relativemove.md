# Steering Worker

The `lecturesight-steeringworker-relativemove` bundle provides the Relative Move Camera Steering Worker, which is responsible for moving the camera.

The steering worker is given a target position, monitors the camera position and decides whether to move the camera and at what  speed. The target position is updated by the Camera Operator, or can be set via console command or in the [PTZ Camera Control](../ui/cameracontrol) window.

In order to produce smooth camera movements, the steering worker will gradually decrease the speed of the camera movement as it gets closer to the target. Also if the target position is already near the actual position of the camera the steering worker will produce slow correction moves.

Under a certain distance the steering worker will not produce any correction moves which compensates for slightly oscillating targets.

## Configuration

> Scene limits, alpha and stop values are specified in the PTZ camera co-ordinate system.

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.left |  | Left-most limit of the scene from the overview image
| cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.right |  | Right-most limit of the scene from the overview image
| cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.top |  | Top-most limit of the scene from the overview image
| cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.bottom |  | Bottom limit of the scene from the overview image
| cv.lecturesight.ptz.steering.worker.relativemove.move.damp.pan | 1.0 | Maximum pan speed<br>(0 to 1)
| cv.lecturesight.ptz.steering.worker.relativemove.move.damp.tilt | 1.0 | Maximum tilt speed<br>(0 to 1)
| cv.lecturesight.ptz.steering.worker.relativemove.move.alpha.x | 400 | Horizontal region in which the pan speed should decrease
| cv.lecturesight.ptz.steering.worker.relativemove.move.alpha.y | 400 | Vertical region in which the tilt speed should decrease
| cv.lecturesight.ptz.steering.worker.relativemove.move.stop.x | 10 | Stop pan movement when camera is within this horizontal distance of the target
| cv.lecturesight.ptz.steering.worker.relativemove.move.stop.y | 10 | Stop tilt movement when camera is within this vertical distance of the target
| cv.lecturesight.ptz.steering.worker.relativemove.xflip | false | Flip x co-ordinates if required when camera is mounted inverted.
| cv.lecturesight.ptz.steering.worker.relativemove.yflip | false | Flip y co-ordinates if required when camera is mounted inverted.
| cv.lecturesight.ptz.steering.worker.relativemove.move.initial.delay | 2500 | Initial delay in ms after setting initial position before starting to move.
| cv.lecturesight.ptz.steering.worker.relativemove.focus.fixed | false | If true, disable auto-focus after setting initial position.
| cv.lecturesight.ptz.steering.worker.relativemove.autostart | true | Controls if the camera steering is active when the module is started.


## Console Commands

| Command                                     | Description |
|---------------------------------------|---------------|
| cs:on | Activates the camera steering.
| cs:off | Deactivates the camera steering.
| cs:move <pan\> <tilt\> | Make the camera move the specified pan and tilt coordinates. The coordinates are normalized in a value range between -1.0 and 1.0, where -1 is left / bottom and 1.0 is right / top.
| cs:zoom <zoom\> | Makes the camera set the specified zoom. The zoom value is normalized to a value range of 0.0 to 1.0. where 0 is neutral and 1 is maximum zoom.
|  cs:home | Makes the camera move to its home position. For most models this will be pan=0.0 and tilt=0.0.

