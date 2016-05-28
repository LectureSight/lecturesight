# ONVIF PTZ-Controller

### cv.lecturesight.onvif.camera.host

*Default:* 172.0.0.1

The host name / ip address for the camera.

### cv.lecturesight.onvif.camera.username

*Default:* admin

The username that will be used to authenticate on the camera, the user is
a ONVIF / web service specific user that has access to the web service.
The user should have Administrator privileges to be able to manage camera
presets.

### cv.lecturesight.onvif.camera.password

*Default:* admin

The password to use for authentication.

### cv.lecturesight.onvif.camera.pan.min

*Default:* -1700

The minimum pan value to use in translating LectureSight values to camera values.
ONVIF's internal values for pan range from -1 to 1. This minimum value is mapped
to that range.

### cv.lecturesight.onvif.camera.pan.max

*Default:* 1700

The maximum pan value.

### cv.lecturesight.onvif.camera.pan.maxspeed

*Default:* 100

The maximum speed for changing the pan value, internally mapped to 1.

### cv.lecturesight.onvif.camera.tilt.min

*Default:* -200

The minimum tilt value. Tilt value range is -1 to 1.

### cv.lecturesight.onvif.camera.tilt.max

*Default:* 900

The maximum tilt value.

### cv.lecturesight.onvif.camera.tilt.maxspeed

*Default:* 100

The maximum speed for changing the tilt value, internally mapped to 1.

### cv.lecturesight.onvif.camera.zoom.min

*Default:* 1

The minimum zoom level value. Zoom range is 0 to 1.

### cv.lecturesight.onvif.camera.zoom.max

*Default:* 9999

The maximum zoom level value.

### cv.lecturesight.onvif.camera.zoom.maxspeed

*Default:* 10

The maximum speed for changing the zoom level, internally mapped -1 to 1.

### cv.lecturesight.onvif.updater.interval

*Default:* 200

The interval to send responses to all the registered camera listeners.
Register camera listeners by calling *addCameraListener(CameraListener listener)*.
