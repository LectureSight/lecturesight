# Template-Matching Video Analysis

## Video Analysis Configuration

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
cv.lecturesight.videoanalysis.change.threshold | 48 | Threshold for change detection. A pixel in the change images is considered changed if the summed difference in all color channels of the pixel in two consecutive frames equals or exceeds this value.
cv.lecturesight.videoanalysis.cell.activation.threshold | 3 | Threshold for when a cell is activated. If there are equal or more changed pixels in a cell, the cell is activated.
cv.lecturesight.videoanalysis.object.cells.min | 2 | Minimum number of active cells in a cluster to be considered a tracking target.
cv.lecturesight.videoanalysis.object.cells.max | 128 | Maximum number of cells that a tracking target may consist of.
cv.lecturesight.videoanalysis.object.dormant.min | 400 | Minimum number of milliseconds that a tracking target may be dormant before being discarded from the list of targets.
cv.lecturesight.videoanalysis.object.dormant.max | 8000 | Maximum number of milliseconds that a tracking target may be dormant before being discarded from the list of targets.
cv.lecturesight.videoanalysis.object.timeout | 60000
cv.lecturesight.videoanalysis.object.dormant.age.factor | 0.5
cv.lecturesight.videoanalysis.object.active.min | 500
cv.lecturesight.videoanalysis.object.move.threshold | 3
cv.lecturesight.videoanalysis.object.match.threshold | 15

## Connected Component Configuration

The Template-Matching Video Analysis Service uses the Connected Components service, which has the following configuration options:

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.blobfinder.blobs.max | 100 | The maximum number of blobs that an instance of a BlobFinder can work with. This value affects the size fo several GPU buffers. Thus this may help to solve memory shortages on older GPUs.
| cv.lecturesight.blobfinder.blobsize.min | 20 | Default minimum size (in pixels) of a valid blob. This value is usually set by the consumer when instantiating a BlobFinder.
| cv.lecturesight.blobfinder.blobsize.max | 10000 | Default maximum size (in pixels) of a valid blob. This value is usually set by the consumer when instantiating a BlobFinder.

## Console Commands
**Command domain:** va

### va:reset

Clears the target list and resets the tracker.

