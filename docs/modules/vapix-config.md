# VAPIX® PTZ-Controller

### cv.lecturesight.vapix.camera.host

*Default:* 172.0.0.1

The host name / ip address for the camera.

### cv.lecturesight.vapix.camera.username

*Default:* admin

The username that will be used to authenticate on the camera, the user is a
standard security user with Administrator privileges to be able to manage camera
presets.

### cv.lecturesight.vapix.camera.password

*Default:* admin

The password to use for authentication.

### cv.lecturesight.vapix.camera.pan.min

*Default:* -17000

The minimum pan value to use in translating LectureSight values to camera values.
VAPIX uses degrees with fractions for the range that the camera can pan.

### cv.lecturesight.vapix.camera.pan.max

*Default:* 17000

The maximum pan value.

### cv.lecturesight.vapix.camera.pan.maxspeed

*Default:* 100

The maximum speed for changing the pan value.

### cv.lecturesight.vapix.camera.tilt.min

*Default:* -2000

The minimum tilt value.

### cv.lecturesight.vapix.camera.tilt.max

*Default:* 9000

The maximum tilt value.

### cv.lecturesight.vapix.camera.tilt.maxspeed

*Default:* 100

The maximum speed for changing the tilt value.

### cv.lecturesight.vapix.camera.zoom.min

*Default:* 1

The minimum zoom level value.

### cv.lecturesight.vapix.camera.zoom.max

*Default:* 9999

The maximum zoom level value.

### cv.lecturesight.vapix.camera.zoom.maxspeed

*Default:* 10

The maximum speed for changing the zoom level.

### cv.lecturesight.vapix.updater.interval

*Default:* 200

The interval to send responses to all the registered camera listeners.
Register camera listeners by calling *addCameraListener(CameraListener listener)*.
