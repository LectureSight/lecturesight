# VISCA Camera

The `wulff-visca-service` bundle provides a driver for cameras speaking the serial VISCA protocol defined by Sony.

On activation the service tries to initialize all VISCA cameras on the configured serial device.

Upon discovery of a VISCA camera, the driver determines camera vendor and model and tries to load a matching device profile.
If no matching profile exists, the driver loads a default profile that has the same configuration as the profile for the Sony EVI-D30.
Most VISCA cameras can be configured to run in D30 compatibility mode.

## Configuration

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
com.wulff.lecturesight.visca.port.device | /dev/ttyS0 | Serial device for camera communication
com.wulff.lecturesight.visca.port.speed | 9600 | Serial device speed
com.wulff.lecturesight.visca.port.databits | 8 | Serial device data bits
com.wulff.lecturesight.visca.port.parity | none |  Serial device parity|
com.wulff.lecturesight.visca.port.stopbits | 1 | Serial device stop bits
com.wulff.lecturesight.visca.updater.interval | 100 | Interval in ms at which camera position is requested
com.wulff.lecturesight.visca.updater.poll.focus | false | If true, report camera focus setting periodically.

## Camera Profiles

Camera profiles are defined in `wulff-visca-service/src/main/resources/profiles` for these cameras:

* Sony D70
* Sony H100S
* Sony H100V
* Sony SGR-300H
* Vaddio ClearVIEW HD-USB

The following is an example of a camera profile definition. It is the default camera profile, which is why the values for camera.vendor.id and camera.model.id are set to DEFAULT. In actual camera profiles, the values are numeric (byte) values.

```
camera.vendor.id=DEFAULT
camera.vendor.name=ACME Inc.
camera.model.id=DEFAULT
camera.model.name=Unknown Model
camera.pan.min=-32767
camera.pan.max=32767
camera.pan.maxspeed=18
camera.tilt.min=-32767
camera.tilt.max=32767
camera.tilt.maxspeed=17
camera.zoom.min=0
camera.zoom.max=65535
camera.zoom.maxspeed=7
camera.home.pan=0
camera.home.tilt=0
```
