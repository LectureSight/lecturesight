# Template-Matching Video Analysis

### cv.lecturesight.videoanalysis.change.threshold

*Default:* 48

This value controls the threshold for the changed detection. A pixel in the
change images is considered as having changed if the summed difference in all
color channels of the pixel in two consecutive frames if equal or above this
value.

### cv.lecturesight.videoanalysis.cell.activation.threshold

*Default:* 0

This values controls when a cell is activated. If there are equal or more changed
pixels in a cell, the cell gets activated.

### cv.lecturesight.videoanalysis.object.cells.min

*Default:* 2

This value controls when a cluster of active cells is considered as a tracking
target.

### cv.lecturesight.videoanalysis.object.cells.max

*Default:* 128

This value controls the maximum number of cells that a tracking target may consist
of.

### cv.lecturesight.videoanalysis.object.dormant.max

*Default:* 300

This value represents the number of milliseconds that a tracking target may
lay dormant before it is discarded from the list of targets.
