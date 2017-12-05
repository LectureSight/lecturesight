# Heartbeat

The `lecturesight-heartbeat` bundle provides the _Heartbeat_ service. It is responsible for controlling the execution of the video analysis services. It listens for several OpenCL service signals that indicate that all services have finished their work for the current frame and kicks off the analysis of the next frame.

## Configuration

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.heartbeat.autostart | 2500 | Delay time in ms after startup before enabling tracking and camera control. Set to -1 to make LS wait for 'ls:run' command in the console before tracking.
| cv.lecturesight.heartbeat.listens.to | VA_DONE | A comma-separated list of signal name the Heartbeat service waits for before kicking off the processing of the next frame. Do not change this property.

## Console Commands

| Command                                     | Description |
|---------------------------------------|---------------|
| ls:run | Lets the service activate the video analysis subsystem.
| ls:step <number of frames> | Lets the service run the video analysis subsystem run for the given number of frames. If the argument is omitted the video analysis is run only a single iteration. This command is especially useful for debugging when working with a video file frame source instead of a life video input. |
| ls:pause | Pauses the operation of the video analysis services without de-initializing the service.
| ls:restart | Re-initializes the heart beat service and start the video analysis subsystem. This command mighty be used when the listens.to property was changed since the internal signal barrier will be newly setup up.
| ls:stop | Stops the video analysis subsystem and de-initializes the heartbeat service.




