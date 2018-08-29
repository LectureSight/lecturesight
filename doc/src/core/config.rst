Configuration
=============

The *Configuration* service in the ``lecturesight-utils`` bundle
provides configuration settings to LectureSight services.

At startup, the configuration is loaded from the files
``conf/lecturesight.properties`` and ``conf/build.properties``

When running, many configuration settings can be updated live through
the command-line `Console <console.md>`__ or the `System
Configuration <../ui/config.md>`__ user interface.

Commands
--------

+------------------------------------------+------------------+
| Command                                  | Description      |
+==========================================+==================+
| config:defaults                          | Show default     |
|                                          | configuration    |
|                                          | values           |
+------------------------------------------+------------------+
| config:load                              | Load             |
|                                          | configuration    |
|                                          | from filename    |
+------------------------------------------+------------------+
| config:save                              | Save             |
|                                          | configuration to |
|                                          | filename         |
+------------------------------------------+------------------+
| config:set                               | Set              |
|                                          | configuration    |
|                                          | key to value     |
+------------------------------------------+------------------+
| config:show [prefix]                     | Show all         |
|                                          | configuration    |
|                                          | values,          |
|                                          | optionally       |
|                                          | matching the     |
|                                          | prefix.          |
+------------------------------------------+------------------+
| config:buildinfo                         | Shows the value  |
|                                          | of the immutable |
|                                          | cv.lecturesight. |
|                                          | buildinfo        |
|                                          | property, if     |
|                                          | set.             |
+------------------------------------------+------------------+
| config:version                           | Show the value   |
|                                          | of the immutable |
|                                          | cv.lecturesight. |
|                                          | version          |
|                                          | property, if     |
|                                          | set.             |
+------------------------------------------+------------------+
