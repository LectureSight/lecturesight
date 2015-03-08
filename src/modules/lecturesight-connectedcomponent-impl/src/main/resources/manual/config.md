# Configuration Parameters

**cv.lecturesight.blobfinder.blobs.max**

*Default:* 100

The maximum number of blobs that an instance of a BlobFinder can work with. This
value affects the size fo several GPU buffers. Thus this may help to solve
memory shortages on older GPUs.

**cv.lecturesight.blobfinder.blobsize.min**

*Default:* 20

Default minimum size (in pixels) of a valid blob. This value is usually set by
the consumer when instantiating a BlobFinder.

**cv.lecturesight.blobfinder.blobsize.max** 

*Default:* 10000

Default maximum size (in pixels) of a valid blob. This value is usually set by
the consumer when instantiating a BlobFinder.
