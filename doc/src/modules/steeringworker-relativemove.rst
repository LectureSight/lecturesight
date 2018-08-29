Steering Worker
===============

The ``lecturesight-steeringworker-relativemove`` bundle provides the
Relative Move Camera Steering Worker, which is responsible for moving
the camera.

The steering worker is given a target position, monitors the camera
position and decides whether to move the camera and at what speed. The
target position is updated by the Camera Operator, or can be set via
console command or in the `PTZ Camera Control <../ui/cameracontrol>`__
window.

In order to produce smooth camera movements, the steering worker will
gradually decrease the speed of the camera movement as it gets closer to
the target. Also if the target position is already near the actual
position of the camera the steering worker will produce slow correction
moves.

Under a certain distance the steering worker will not produce any
correction moves which compensates for slightly oscillating targets.

Configuration
-------------

    Scene limits, alpha and stop values are specified in the PTZ camera
    co-ordinate system.

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| cv.lecturesight.ptz.steering. |          | Left-most limit of the scene from |
| worker.relativemove.scene.lim |          | the overview image                |
| it.left                       |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. |          | Right-most limit of the scene     |
| worker.relativemove.scene.lim |          | from the overview image           |
| it.right                      |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. |          | Top-most limit of the scene from  |
| worker.relativemove.scene.lim |          | the overview image                |
| it.top                        |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. |          | Bottom limit of the scene from    |
| worker.relativemove.scene.lim |          | the overview image                |
| it.bottom                     |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | 1.0      | Maximum pan speed(0 to 1)         |
| worker.relativemove.move.damp |          |                                   |
| .pan                          |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | 1.0      | Maximum tilt speed(0 to 1)        |
| worker.relativemove.move.damp |          |                                   |
| .tilt                         |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | 400      | Horizontal region in which the    |
| worker.relativemove.move.alph |          | pan speed should decrease         |
| a.x                           |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | 400      | Vertical region in which the tilt |
| worker.relativemove.move.alph |          | speed should decrease             |
| a.y                           |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | 10       | Stop pan movement when camera is  |
| worker.relativemove.move.stop |          | within this horizontal distance   |
| .x                            |          | of the target                     |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | 10       | Stop tilt movement when camera is |
| worker.relativemove.move.stop |          | within this vertical distance of  |
| .y                            |          | the target                        |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | false    | Flip x co-ordinates if required   |
| worker.relativemove.xflip     |          | when camera is mounted inverted.  |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | false    | Flip y co-ordinates if required   |
| worker.relativemove.yflip     |          | when camera is mounted inverted.  |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | 2500     | Initial delay in ms after setting |
| worker.relativemove.move.init |          | initial position before starting  |
| ial.delay                     |          | to move.                          |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | false    | If true, disable auto-focus after |
| worker.relativemove.focus.fix |          | setting initial position.         |
| ed                            |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.ptz.steering. | true     | Controls if the camera steering   |
| worker.relativemove.autostart |          | is active when the module is      |
|                               |          | started.                          |
+-------------------------------+----------+-----------------------------------+

Console Commands
----------------

+------------------------------------------+------------------+
| Command                                  | Description      |
+==========================================+==================+
| cs:on                                    | Activates the    |
|                                          | camera steering. |
+------------------------------------------+------------------+
| cs:off                                   | Deactivates the  |
|                                          | camera steering. |
+------------------------------------------+------------------+
| cs:move                                  | Make the camera  |
|                                          | move the         |
|                                          | specified pan    |
|                                          | and tilt         |
|                                          | coordinates. The |
|                                          | coordinates are  |
|                                          | normalized in a  |
|                                          | value range      |
|                                          | between -1.0 and |
|                                          | 1.0, where -1 is |
|                                          | left / bottom    |
|                                          | and 1.0 is right |
|                                          | / top.           |
+------------------------------------------+------------------+
| cs:zoom                                  | Makes the camera |
|                                          | set the          |
|                                          | specified zoom.  |
|                                          | The zoom value   |
|                                          | is normalized to |
|                                          | a value range of |
|                                          | 0.0 to 1.0.      |
|                                          | where 0 is       |
|                                          | neutral and 1 is |
|                                          | maximum zoom.    |
+------------------------------------------+------------------+
| cs:home                                  | Makes the camera |
|                                          | move to its home |
|                                          | position. For    |
|                                          | most models this |
|                                          | will be pan=0.0  |
|                                          | and tilt=0.0.    |
+------------------------------------------+------------------+
