# ONVIF PTZ Controller

Open Network Video Interface Forum ([ONVIF](http://www.onvif.org/)) is a community to standardize communication
between IP-based security products, in this case PTZ cameras.

The communication with the camera is defined as a web service. A wrapper
handles most of the Simple Object Access Protocol (SOAP) messaging by using
Java Architecture for XML Binding (JAXB) to map the objects to Extensible
Markup Language (XML).

!!! warning "Beta code"
    The ONVIF PTZ Controller has not been extensively tested or used in production.

## Configuration

| Key                                   | Default   | Description |
|---------------------------------------|----------:|-------------------------------------------|
| cv.lecturesight.onvif.camera.host | 127.0.0.1 | The host name / ip address for the camera.
| cv.lecturesight.onvif.camera.username | admin | The username that will be used to authenticate on the camera, the user is a ONVIF / web service specific user that has access to the web service. The user should have Administrator privileges to be able to manage camera presets.
| cv.lecturesight.onvif.camera.password | admin | The password to use for authentication.
| cv.lecturesight.onvif.camera.pan.min | -1700 | The minimum pan value to use in translating LectureSight values to camera values. ONVIF's internal values for pan range from -1 to 1. This minimum value is mapped to that range.
| cv.lecturesight.onvif.camera.pan.max | 1700 | The maximum pan value.
| cv.lecturesight.onvif.camera.pan.maxspeed | 100 | The maximum speed for changing the pan value, internally mapped to 1.
| cv.lecturesight.onvif.camera.tilt.min | -200 | The minimum tilt value. Tilt value range is -1 to 1.
| cv.lecturesight.onvif.camera.tilt.max | 900 | The maximum tilt value.
| cv.lecturesight.onvif.camera.tilt.maxspeed | 100 | The maximum speed for changing the tilt value, internally mapped to 1.
| cv.lecturesight.onvif.camera.zoom.min | 1 | The minimum zoom level value. Zoom range is 0 to 1.
| cv.lecturesight.onvif.camera.zoom.max | 9999 | The maximum zoom level value.
| cv.lecturesight.onvif.camera.zoom.maxspeed | 10 | The maximum speed for changing the zoom level, internally mapped -1 to 1.
| cv.lecturesight.onvif.updater.interval | 80 | The interval in ms to request position updates.

## WSDL

The Web Service Definition Language (WSDL) for the different versions and devices:

| API | Version | WSDL |
| --- | --- | --- |
| Device Management | 1.0 | [http://www.onvif.org/ver10/device/wsdl/devicemgmt.wsdl](http://www.onvif.org/ver10/device/wsdl/devicemgmt.wsdl)
| | 2.0 | [http://www.onvif.org/ver20/ptz/wsdl/ptz.wsdl](http://www.onvif.org/ver20/ptz/wsdl/ptz.wsdl)
| Media | 1.0 | [http://www.onvif.org/ver10/media/wsdl/media.wsdl](http://www.onvif.org/ver10/media/wsdl/media.wsdl)
| | 2.0 | [http://www.onvif.org/ver20/media/wsdl/media.wsdl](http://www.onvif.org/ver20/media/wsdl/media.wsdl)
| PTZ | 1.0 | [http://www.onvif.org/onvif/ver10/ptz/wsdl/ptz.wsdl](http://www.onvif.org/onvif/ver10/ptz/wsdl/ptz.wsdl)
| | 2.0 | [http://www.onvif.org/ver20/ptz/wsdl/ptz.wsdl](http://www.onvif.org/ver20/ptz/wsdl/ptz.wsdl)
| All | 2.0 | [http://www.onvif.org/onvif/ver20/util/operationIndex.html](http://www.onvif.org/onvif/ver20/util/operationIndex.html)

# ONVIF Library

This camera implementation is based around the ONVIF wrapper classes written by Milgo and available on GitHub
at: [https://github.com/milg0/onvif-java-lib](https://github.com/milg0/onvif-java-lib).

The onvif-java-lib is deployed under the [Apache License, Version 2.0 of January 2004](http://www.apache.org/licenses/).
