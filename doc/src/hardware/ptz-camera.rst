PTZ Camera
==========

LectureSight steers a pan-tilt-zoom (PTZ) camera in real-time to follow
the presenter, based on video analysis of the overview image. Three
camera control protocols are supported: VISCA, VAPIX and ONVIF.

Sony VISCA Protocol
-------------------

The system supports Sony's VISCA protocol out-of-the-box. The driver
detects the model and version of the camera and loads a fitting
parameter set. If the camera model is unknown, the driver loads the
profile for the Sony EVI-D30. Most Sony PTZ cameras can be switched into
a D30 compatibility mode.

The following cameras have been tested with LectureSight and have a
dedicated camera profile:

-  Sony EVI-D30
-  Sony EVI-D70
-  Sony EVI-D100
-  Vaddio ClearVIEW HD-USB

AXIS VAPIX Protocol
-------------------

The system supports the AXIS VAPIX protocol for PTZ cameras. The
following cameras have been tested with LectureSight:

-  AXIS V5915 PTZ Network Camera

ONVIF Protocol
--------------

The system supports the ONVIF protocol for cameras. Any PTZ camera which
supports ONVIF may work with LectureSight, although only the AXIS V5915
camera has been tested with ONVIF.
