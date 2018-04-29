# Metrics

The Metrics Service records LectureSight activity for later analysis and quality improvement.

When the metrics service is enabled, metrics are saved to the file `metrics/metrics.json` when LectureSight
exits, or at the end of each scheduled event if the Scheduler is enabled.

## Configuration

| Key                                   | Default   | Description |
|---------------------------------------|-----------|-------------------------------------------|
| cv.lecturesight.util.metrics.enable | false | Set to true to enable the metrics service
| cv.lecturesight.util.metrics.csv.enable | true | Enable CSV reporting
| cv.lecturesight.util.metrics.csv.interval | 30 | Interval to write out updates to CSV files
| cv.lecturesight.util.metrics.jmx.enable | true | Enable JMX reporting
| cv.lecturesight.util.metrics.log.enable | true | Enable log reporting
| cv.lecturesight.util.metrics.log.interval | 300 | Interval to write out updates to log file

## Console Commands

| Command                                     | Description |
|---------------------------------------|---------------|
| metrics:list  | List the keys of all registered metrics.
| metrics:pause | Suspend metric reporting.
| metrics:reset | Reset all metrics.
| metrics:resume | Resume metric reporting.
| metrics:save | Save metrics summary to `metrics/metrics.json`
| metrics:show | Show the metrics JSON summary
