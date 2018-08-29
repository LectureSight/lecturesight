Video Analysis
==============

The ``lecturesight-videoanalysis-templ`` bundle performs template-based
video analysis and provides the *ObjectTracker* service.

The tracker will follow a maximum of 6 targets, using template-matching
between frames to maintain persistence. New targets are identified when
cells in the overview image exceed defined change thresholds. Targets
are dropped when they are inactive for a defined period.

The tracker provides the list of active targets to the `Camera
Operator <cameraoperator-ptz>`__ which is responsible for selecting one
or more targets to follow. The `Object Tracker
display <../ui/objecttracker>`__ shows the targets being tracked.

Configuration
-------------

+--------------------------------+---------+-----------------------------------+
| Key                            | Default | Description                       |
+================================+=========+===================================+
| cv.lecturesight.videoanalysis. | 48      | Pixel color change threshold. A   |
| change.threshold               |         | pixel has changed if the summed   |
|                                |         | difference in all color channels  |
|                                |         | of the pixel in two consecutive   |
|                                |         | frames exceeds this value.        |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.videoanalysis. | 3       | Threshold for when a cell is      |
| cell.activation.threshold      |         | activated. If there are more than |
|                                |         | this number of changed pixels in  |
|                                |         | a cell, the cell is activated. A  |
|                                |         | cell is 8x8 pixels.               |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.videoanalysis. | 2       | Minimum number of active cells in |
| object.cells.min               |         | a cluster to be considered a      |
|                                |         | tracking target.                  |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.videoanalysis. | 128     | Maximum number of cells that a    |
| object.cells.max               |         | tracking target may consist of.   |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.videoanalysis. | 15      | Template match score must exceed  |
| object.match.threshold         |         | this value for the object at the  |
|                                |         | template location to be           |
|                                |         | considered the same object        |
|                                |         | between successive frames.        |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.videoanalysis. | 3       | Movement threshold: the target is |
| object.move.threshold          |         | considered to have moved between  |
|                                |         | two successive frames if the      |
|                                |         | distance between positions        |
|                                |         | exceeds this value.               |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.videoanalysis. | 400     | Minimum time in ms that a         |
| object.dormant.min             |         | tracking target may be dormant    |
|                                |         | before it is discarded from the   |
|                                |         | list of targets.                  |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.videoanalysis. | 8000    | Maximum time in ms that a static  |
| object.dormant.max             |         | tracking target may be dormant    |
|                                |         | before it is discarded from the   |
|                                |         | list of targets.                  |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.videoanalysis. | 0.5     | Scaling factor that increases the |
| object.dormant.age.factor      |         | dormant timeout value as the      |
|                                |         | object ages. Older objects have a |
|                                |         | higher timeout value.             |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.videoanalysis. | 500     | Minimum time in ms that a         |
| object.active.min              |         | tracking target must be active    |
|                                |         | before it is included in the      |
|                                |         | target list provided to the       |
|                                |         | camera operator.                  |
+--------------------------------+---------+-----------------------------------+
| cv.lecturesight.videoanalysis. | 60000   | Maximum timeout in ms for target. |
| object.timeout                 |         | This timeout is applied when the  |
|                                |         | target has moved at least a       |
|                                |         | certain distance from its         |
|                                |         | original position.                |
+--------------------------------+---------+-----------------------------------+

Template-Matching Video Analysis uses the *ConnectedComponents* service,
which has the following configuration options:

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| cv.lecturesight.blobfinder.bl | 100      | The maximum number of blobs that  |
| obs.max                       |          | an instance of a BlobFinder can   |
|                               |          | work with. This value affects the |
|                               |          | size fo several GPU buffers. Thus |
|                               |          | this may help to solve memory     |
|                               |          | shortages on older GPUs.          |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.blobfinder.bl | 20       | Default minimum size (in pixels)  |
| obsize.min                    |          | of a valid blob. This value is    |
|                               |          | usually set by the consumer when  |
|                               |          | instantiating a BlobFinder.       |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.blobfinder.bl | 10000    | Default maximum size (in pixels)  |
| obsize.max                    |          | of a valid blob. This value is    |
|                               |          | usually set by the consumer when  |
|                               |          | instantiating a BlobFinder.       |
+-------------------------------+----------+-----------------------------------+

Commands
--------

+------------+--------------------------------------------------+
| Command    | Description                                      |
+============+==================================================+
| va:reset   | Clears the target list and resets the tracker.   |
+------------+--------------------------------------------------+
