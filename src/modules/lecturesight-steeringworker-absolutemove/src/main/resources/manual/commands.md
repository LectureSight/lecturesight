# Absolute Move Steering Worker

**Command domain:** cs

### cs:on

Activates the camera steering.

### cs:off

Deactivates the camera steering.

### cs:move *pan* *tilt*

Make the camera move the specified pan and tilt coordinates. The coordinates are
normalized in a value range between -1.0 and 1.0, where

* pan: -1.0 left most, 1.0 right most
* tilt: -1.0 bottom., 1.0 top most

### cs:home

Makes the camera move to its home position. For most models this will be pan=0.0
and tilt=0.0.

### cs:zoom *zoom*

Makes the camera set the specified zoom. The zoom value is normalized to a value
range of 0.0 to 1.0. where

* 0.0 neutral
* 1.0 maximal zoom
