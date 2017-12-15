# VAPIX PTZ Controller

VAPIX is the Axis camera API:

[http://www.axis.com/techsup/cam_servers/dev/cam_http_api_index.php](http://www.axis.com/techsup/cam_servers/dev/cam_http_api_index.php)

The communication with the camera is based around HTTP response and requests. The returning value for success is

 - HTTP_NO_CONTENT (204): Command has been sent
 - HTTP_OK (200): Command sent

and response in text format. The returning text format is structured
as [propertyName]=[propertyValue]

## Configuration

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.vapix.camera.host     | 172.0.0.1 | The host name / ip address for the camera.
| cv.lecturesight.vapix.camera.username | admin     | The username that will be used to authenticate on the camera
| cv.lecturesight.vapix.camera.password | admin     | The password to use for authentication.
| cv.lecturesight.vapix.camera.inverted | false     | Whether the camera is mounted inverted.
| cv.lecturesight.vapix.camera.pan.min  | -17000    | The minimum pan value to use in translating LectureSight values to camera values. VAPIX uses degrees with fractions for the range that the camera can pan.
| cv.lecturesight.vapix.camera.pan.max  | 17000     | The maximum pan value.
| cv.lecturesight.vapix.camera.pan.maxspeed | 100 | The maximum speed for changing the pan value.
| cv.lecturesight.vapix.camera.tilt.min | -2000 | The minimum tilt value.
| cv.lecturesight.vapix.camera.tilt.max | 9000 | The maximum tilt value.
| cv.lecturesight.vapix.camera.tilt.maxspeed | 100 | The maximum speed for changing the tilt value.
| cv.lecturesight.vapix.camera.zoom.min | 1 | The minimum zoom level value.
| cv.lecturesight.vapix.camera.zoom.max | 9999 | The maximum zoom level value.
| cv.lecturesight.vapix.camera.zoom.maxspeed | 10 | The maximum speed for changing the zoom level.
| cv.lecturesight.vapix.updater.interval | 200 | The interval to send responses to all the registered camera listeners.

The username should have Administrator privileges to be able to manage camera presets.

## Inverted camera

If the camera is mounted inverted, set these 3 properties:

```
cv.lecturesight.vapix.camera.inverted=true
cv.lecturesight.vapix.camera.tilt.min=-9000
cv.lecturesight.vapix.camera.tilt.max=2000
```

