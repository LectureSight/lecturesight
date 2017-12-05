# Scheduler

The `lecturesight-scheduler` bundle provides a service that loads a schedule from an iCalendar (RFC-2445) file and starts/stops object tracking and camera control accordingly.

Changes to the file are detected and the internal schedule is updated automatically. When the file is deleted, all events are removed.

The implementation works internally with a periodically called routine that ensures that the state of object tracking and camera control components are consistent with the schedule.
This way the system will, for instance, start to control the camera even if started after a scheduled begin of a recording.

Depending on the video analysis implementation, the tracking components may need a certain time to adapt to the scene before producing correct tracking results.
To prevent false camera movement caused by false positives the services can be configured to start camera control some time after the object tracking has been activated. 

Tracking algorithms may need a certain amount of time to adapt to the scene. In this period, false positives could lead to unwanted camera movements, so starting the camera control some time after the object tracking might be useful.

## Configuration

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.scheduler.enable | true | Whether to enable the scheduler.
| cv.lecturesight.scheduler.schedule.file|schedule.ics|The name of the iCal file holding the schedule.
| cv.lecturesight.scheduler.agent.name | | A capture agent name the service will look for in case the iCal holds schedules for more than one capture agent. If this property value is empty, the service will take every event from the iCal into account.
| cv.lecturesight.scheduler.timezone.offset | 1 | The time zone offset to add to the event times from the schedule.
cv.lecturesight.scheduler.tracking.leadtime | 0 | The time (in seconds) the service will wait after the object tracking has been activated before the camera control is activated.

## Console Commands

| Command                                     | Description |
|---------------------------------------|---------------|
| scheduler:start | Activates tracking and camera steering.
| scheduler:stop | Deactivates tracking and camera steering.
| scheduler:status | Shows the scheduler status.


