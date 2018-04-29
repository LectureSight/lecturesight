# Video Analysis

The `lecturesight-videoanalysis-templ` bundle performs template-based video analysis and provides the _ObjectTracker_ service.

The tracker will follow a maximum of 6 targets, using template-matching between frames to maintain persistence. New targets are identified when cells in the overview image exceed defined change thresholds. Targets are dropped when they are inactive for a defined period.

The tracker provides the list of active targets to the [Camera Operator](cameraoperator-ptz) which is responsible for selecting one or more targets to follow. The [Object Tracker display](../ui/objecttracker) shows the targets being tracked.

## Configuration

| Key                                   | Default   | Description |
|---------------------------------------|---------|-------------------------------------------|
cv.lecturesight.videoanalysis.change.threshold | 48 | Pixel color change threshold. A pixel has changed if the summed difference in all color channels of the pixel in two consecutive frames exceeds this value.
cv.lecturesight.videoanalysis.cell.activation.threshold | 3 | Threshold for when a cell is activated. If there are more than this number of changed pixels in a cell, the cell is activated. A cell is 8x8 pixels.
cv.lecturesight.videoanalysis.object.cells.min | 2 | Minimum number of active cells in a cluster to be considered a tracking target.
cv.lecturesight.videoanalysis.object.cells.max | 128 | Maximum number of cells that a tracking target may consist of.
cv.lecturesight.videoanalysis.object.match.threshold | 15 | Template match score must exceed this value for the object at the template location to be considered the same object between successive frames.
cv.lecturesight.videoanalysis.object.move.threshold | 3 | Movement threshold: the target is considered to have moved between two successive frames if the distance between positions exceeds this value.
cv.lecturesight.videoanalysis.object.dormant.min | 400 | Minimum time in ms that a tracking target may be dormant before it is discarded from the list of targets.
cv.lecturesight.videoanalysis.object.dormant.max | 8000 | Maximum time in ms that a static tracking target may be dormant before it is discarded from the list of targets.
cv.lecturesight.videoanalysis.object.dormant.age.factor | 0.5 | Scaling factor that increases the dormant timeout value as the object ages. Older objects have a higher timeout value.
cv.lecturesight.videoanalysis.object.active.min | 500 | Minimum time in ms that a tracking target must be active before it is included in the target list provided to the camera operator.
cv.lecturesight.videoanalysis.object.timeout | 60000 | Maximum timeout in ms for target. This timeout is applied when the target has moved at least a certain distance from its original position.

Template-Matching Video Analysis uses the _ConnectedComponents_ service, which has the following configuration options:

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.blobfinder.blobs.max | 100 | The maximum number of blobs that an instance of a BlobFinder can work with. This value affects the size fo several GPU buffers. Thus this may help to solve memory shortages on older GPUs.
| cv.lecturesight.blobfinder.blobsize.min | 20 | Default minimum size (in pixels) of a valid blob. This value is usually set by the consumer when instantiating a BlobFinder.
| cv.lecturesight.blobfinder.blobsize.max | 10000 | Default maximum size (in pixels) of a valid blob. This value is usually set by the consumer when instantiating a BlobFinder.

## Commands

| Command                                     | Description |
|---------------------------------------|---------------|
| va:reset | Clears the target list and resets the tracker.

