Heartbeat
=========

The ``lecturesight-heartbeat`` bundle provides the *Heartbeat* service.
It is responsible for controlling the execution of the video analysis
services. It listens for several OpenCL service signals that indicate
that all services have finished their work for the current frame and
kicks off the analysis of the next frame.

Configuration
-------------

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| cv.lecturesight.heartbeat.aut | 2500     | Delay time in ms after startup    |
| ostart                        |          | before enabling tracking and      |
|                               |          | camera control. Set to -1 to make |
|                               |          | LS wait for 'ls:run' command in   |
|                               |          | the console before tracking.      |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.heartbeat.lis | VA\_DONE | A comma-separated list of signal  |
| tens.to                       |          | name the Heartbeat service waits  |
|                               |          | for before kicking off the        |
|                               |          | processing of the next frame. Do  |
|                               |          | not change this property.         |
+-------------------------------+----------+-----------------------------------+

Console Commands
----------------

+------------------------------------------+------------------+
| Command                                  | Description      |
+==========================================+==================+
| ls:run                                   | Activate the     |
|                                          | video analysis   |
|                                          | subsystem.       |
+------------------------------------------+------------------+
| ls:step [frames]                         | Run the video    |
|                                          | analysis         |
|                                          | subsystem run    |
|                                          | for the given    |
|                                          | number of        |
|                                          | frames. If the   |
|                                          | argument is      |
|                                          | omitted, the     |
|                                          | video analysis   |
|                                          | is run for 1     |
|                                          | frame. This      |
|                                          | command is       |
|                                          | especially       |
|                                          | useful for       |
|                                          | debugging when   |
|                                          | working with a   |
|                                          | video file frame |
|                                          | source instead   |
|                                          | of a live video  |
|                                          | input.           |
+------------------------------------------+------------------+
| ls:pause                                 | Pause the video  |
|                                          | analysis service |
|                                          | without          |
|                                          | de-initializing  |
|                                          | the service.     |
+------------------------------------------+------------------+
| ls:restart                               | Re-initializes   |
|                                          | the heart beat   |
|                                          | service and      |
|                                          | start the video  |
|                                          | analysis         |
|                                          | subsystem. This  |
|                                          | command might be |
|                                          | used when the    |
|                                          | listens.to       |
|                                          | property was     |
|                                          | changed since    |
|                                          | the internal     |
|                                          | signal barrier   |
|                                          | will be newly    |
|                                          | setup up.        |
+------------------------------------------+------------------+
| ls:stop                                  | Stops the video  |
|                                          | analysis         |
|                                          | subsystem and    |
|                                          | de-initializes   |
|                                          | the heartbeat    |
|                                          | service.         |
+------------------------------------------+------------------+
