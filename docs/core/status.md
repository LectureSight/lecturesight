# Status Service

The _StatusService_ provided by the `lecturesight-status` bundle sends LectureSight configuration and status information to a remote service such as a dashboard.

## Configuration

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.status.enable | false | Set to true to enable status updates
| cv.lecturesight.status.url | | The URL of a remote service endpoint for HTTP POST updates
| cv.lecturesight.status.name | lecturesight | A descriptive name of the server or venue
| cv.lecturesight.status.interval | 60 | Interval in seconds between status updates

## POST data

The status service sends an HTTP POST request with the following multipart/form-data fields:

| Field name                         | Type   | Description |
|-------------------------------|-----------|-------------------------------------------|
| name | text/plain | Name configured in `cv.lecturesight.status.name`
| status | text/plain | Tracking status: active or idle
| metrics | application/json | Metrics summary from the [Metrics Service](metrics)
| profile | text/plain | The active [scene profile](profile) definition
| overview-image | application/octet-stream | The overview image snapshot if [configured](framesource/#configuration): file contents of `cv.lecturesight.framesource.snapshot.file`
