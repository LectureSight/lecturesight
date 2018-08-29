Status Service
==============

The *StatusService* provided by the ``lecturesight-status`` bundle sends
LectureSight configuration and status information to a remote service
such as a dashboard.

Configuration
-------------

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| cv.lecturesight.status.enable | false    | Set to true to enable status      |
|                               |          | updates                           |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.status.url    |          | The URL of a remote service       |
|                               |          | endpoint for HTTP POST updates    |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.status.name   | lectures | A descriptive name of the server  |
|                               | ight     | or venue                          |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.status.interv | 60       | Interval in seconds between       |
| al                            |          | status updates                    |
+-------------------------------+----------+-----------------------------------+

POST data
---------

The status service sends an HTTP POST request with the following
multipart/form-data fields:

+----------------------------+-----------+--------------------------------------+
| Field name                 | Type      | Description                          |
+============================+===========+======================================+
| name                       | text/plai | Name configured in                   |
|                            | n         | ``cv.lecturesight.status.name``      |
+----------------------------+-----------+--------------------------------------+
| status                     | text/plai | Tracking status: active or idle      |
|                            | n         |                                      |
+----------------------------+-----------+--------------------------------------+
| metrics                    | applicati | Metrics summary from the `Metrics    |
|                            | on/json   | Service <metrics>`__                 |
+----------------------------+-----------+--------------------------------------+
| profile                    | text/plai | The active `scene                    |
|                            | n         | profile <profile>`__ definition      |
+----------------------------+-----------+--------------------------------------+
| overview-image             | applicati | The overview image snapshot if       |
|                            | on/octet- | `configured <framesource/#configurat |
|                            | stream    | ion>`__:                             |
|                            |           | file contents of                     |
|                            |           | ``cv.lecturesight.framesource.snapsh |
|                            |           | ot.file``                            |
+----------------------------+-----------+--------------------------------------+
