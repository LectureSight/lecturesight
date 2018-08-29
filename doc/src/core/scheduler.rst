Scheduler
=========

The ``lecturesight-scheduler`` bundle provides a service that loads a
schedule from an iCalendar (RFC-2445) file and starts and stops object
tracking and camera control for each event.

The service is designed to allow LectureSight to follow the recording
schedule of an `Opencast <http://www.opencast.org>`__ capture agent such
as
`Galicaster <https://wiki.teltek.es/display/Galicaster/Galicaster+project+Home>`__.
Changes to the file are detected and the internal schedule is updated
automatically. When the file is deleted, all events are removed.

The video analysis and tracking components may need a certain time to
adapt to the scene before producing correct tracking results. To prevent
unnecessary camera movement caused by false positives, the service can
be configured to start camera control some time after the object
tracking has been activated.

Configuration
-------------

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| cv.lecturesight.scheduler.ena | false    | Whether to enable the scheduler.  |
| ble                           |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.scheduler.sch | schedule | The filename of the iCal file     |
| edule.file                    | .ics     | holding the schedule.             |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.scheduler.age |          | A capture agent name the service  |
| nt.name                       |          | will look for in case the iCal    |
|                               |          | holds schedules for more than one |
|                               |          | capture agent. If not set, the    |
|                               |          | service will take every event     |
|                               |          | from the iCal into account.       |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.scheduler.tim | 1        | The time zone offset to add to    |
| ezone.offset                  |          | the event times from the          |
|                               |          | schedule.                         |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.scheduler.tra | 0        | The time (in seconds) the service |
| cking.leadtime                |          | will wait after the object        |
|                               |          | tracking has been activated       |
|                               |          | before the camera control is      |
|                               |          | activated.                        |
+-------------------------------+----------+-----------------------------------+

Console Commands
----------------

+--------------------+----------------------------------------------+
| Command            | Description                                  |
+====================+==============================================+
| scheduler:start    | Activates tracking and camera steering.      |
+--------------------+----------------------------------------------+
| scheduler:stop     | Deactivates tracking and camera steering.    |
+--------------------+----------------------------------------------+
| scheduler:status   | Shows the scheduler status: active or idle   |
+--------------------+----------------------------------------------+
