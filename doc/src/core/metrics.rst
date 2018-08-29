Metrics
=======

The Metrics Service records LectureSight activity for later analysis and
quality improvement.

When the metrics service is enabled, metrics are saved to the file
``metrics/metrics.json`` when LectureSight exits, or at the end of each
scheduled event if the Scheduler is enabled.

Configuration
-------------

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| cv.lecturesight.util.metrics. | false    | Set to true to enable the metrics |
| enable                        |          | service                           |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.util.metrics. | true     | Enable CSV reporting              |
| csv.enable                    |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.util.metrics. | 30       | Interval to write out updates to  |
| csv.interval                  |          | CSV files                         |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.util.metrics. | true     | Enable JMX reporting              |
| jmx.enable                    |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.util.metrics. | true     | Enable log reporting              |
| log.enable                    |          |                                   |
+-------------------------------+----------+-----------------------------------+
| cv.lecturesight.util.metrics. | 300      | Interval to write out updates to  |
| log.interval                  |          | log file                          |
+-------------------------------+----------+-----------------------------------+

Console Commands
----------------

+------------------+----------------------------------------------------+
| Command          | Description                                        |
+==================+====================================================+
| metrics:list     | List the keys of all registered metrics.           |
+------------------+----------------------------------------------------+
| metrics:pause    | Suspend metric reporting.                          |
+------------------+----------------------------------------------------+
| metrics:reset    | Reset all metrics.                                 |
+------------------+----------------------------------------------------+
| metrics:resume   | Resume metric reporting.                           |
+------------------+----------------------------------------------------+
| metrics:save     | Save metrics summary to ``metrics/metrics.json``   |
+------------------+----------------------------------------------------+
| metrics:show     | Show the metrics JSON summary                      |
+------------------+----------------------------------------------------+
