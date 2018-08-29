VAPIX PTZ Controller
====================

VAPIX is the Axis camera API:

http://www.axis.com/techsup/cam_servers/dev/cam_http_api_index.php

The communication with the camera is based around HTTP response and
requests. The returning value for success is

-  HTTP\_NO\_CONTENT (204): Command has been sent
-  HTTP\_OK (200): Command sent

and response in text format. The returning text format is structured as
[propertyName]=[propertyValue]

Configuration
-------------

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| cv.lecturesight.vapix.camera. | 127.0.0. | The host name / ip address for    |
| host                          | 1        | the camera.                       |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | admin    | The username that will be used to |
| username                      |          | authenticate on the camera        |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | admin    | The password to use for           |
| password                      |          | authentication.                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | -17000   | The minimum pan value to use in   |
| pan.min                       |          | translating LectureSight values   |
|                               |          | to camera values. VAPIX uses      |
|                               |          | degrees with fractions for the    |
|                               |          | range that the camera can pan.    |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | 17000    | The maximum pan value.            |
| pan.max                       |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | 100      | The maximum speed for changing    |
| pan.maxspeed                  |          | the pan value.                    |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | -2000    | The minimum tilt value to use in  |
| tilt.min                      |          | translating LectureSight values   |
|                               |          | to camera values. VAPIX uses      |
|                               |          | degrees with fractions for the    |
|                               |          | range that the camera can tilt.   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | 9000     | The maximum tilt value.           |
| tilt.max                      |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | 100      | The maximum speed for changing    |
| tilt.maxspeed                 |          | the tilt value.                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | 1        | The minimum zoom level value.     |
| zoom.min                      |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | 9999     | The maximum zoom level value.     |
| zoom.max                      |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.camera. | 10       | The maximum speed for changing    |
| zoom.maxspeed                 |          | the zoom level.                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.vapix.updater | 200      | The interval to send responses to |
| .interval                     |          | all the registered camera         |
|                               |          | listeners.                        |
+-------------------------------+----------+-----------------------------------+

The username should have Administrator privileges to be able to manage
camera presets.

Inverted camera
---------------

If the camera is mounted inverted, set these 3 properties:

::

    cv.lecturesight.vapix.camera.inverted=true
    cv.lecturesight.vapix.camera.tilt.min=-9000
    cv.lecturesight.vapix.camera.tilt.max=2000
