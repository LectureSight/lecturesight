VISCA Camera
============

The ``wulff-visca-service`` bundle provides a driver for cameras
speaking the serial VISCA protocol defined by Sony.

On activation the service tries to initialize all VISCA cameras on the
configured serial device.

Upon discovery of a VISCA camera, the driver determines camera vendor
and model and tries to load a matching device profile. If no matching
profile exists, the driver loads a default profile that has the same
configuration as the profile for the Sony EVI-D30. Most VISCA cameras
can be configured to run in D30 compatibility mode.

Configuration
-------------

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| com.wulff.lecturesight.visca. | /dev/tty | Serial device for camera          |
| port.device                   | S0       | communication                     |
+-------------------------------+----------+-----------------------------------+
| com.wulff.lecturesight.visca. | 9600     | Serial device speed               |
| port.speed                    |          |                                   |
+-------------------------------+----------+-----------------------------------+
| com.wulff.lecturesight.visca. | 8        | Serial device data bits           |
| port.databits                 |          |                                   |
+-------------------------------+----------+-----------------------------------+
| com.wulff.lecturesight.visca. | none     | Serial device parity              |
| port.parity                   |          |                                   |
+-------------------------------+----------+-----------------------------------+
| com.wulff.lecturesight.visca. | 1        | Serial device stop bits           |
| port.stopbits                 |          |                                   |
+-------------------------------+----------+-----------------------------------+
| com.wulff.lecturesight.visca. | 100      | Interval in ms at which camera    |
| updater.interval              |          | position is requested             |
+-------------------------------+----------+-----------------------------------+
| com.wulff.lecturesight.visca. | false    | If true, report camera focus      |
| updater.poll.focus            |          | setting periodically.             |
+-------------------------------+----------+-----------------------------------+

Camera Profiles
---------------

Camera profiles are defined in
``wulff-visca-service/src/main/resources/profiles`` for these cameras:

-  Sony D70
-  Sony H100S
-  Sony H100V
-  Sony SGR-300H
-  Vaddio ClearVIEW HD-USB

The following is an example of a camera profile definition. It is the
default camera profile, which is why the values for camera.vendor.id and
camera.model.id are set to DEFAULT. In actual camera profiles, the
values are numeric (byte) values.

::

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
