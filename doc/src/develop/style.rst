Style Guide
===========

Code
----

-  LectureSight uses the `maven checkstyle
   plugin <https://maven.apache.org/plugins/maven-checkstyle-plugin/>`__
   to enforce java code style. The checkstyle rules are defined in
   ``src/docs/checkstyle/lecturesight-checkstyle.xml``
-  Indents are two spaces.

Documentation
-------------

The following conventions are used in documentation:

-  LectureSight has L and S capitalized.
-  GStreamer has G and S capitalized.
-  Names that refer to classes or interfaces are italicized and follow
   the case rules used in the source, for example *HeartBeat* service.
-  Module names (src/modules/) are quoted with backticks, for example
   ``lecturesight-heartbeat`` module.
-  File names, configuration values or text entries (for example console
   commands) are quoted with backticks, for example
   ``conf/lecturesight.properties``
-  Compulstory command-line or console command arguments are shown in
   angle brackets, for example ``config:set <key> <value>``
-  Optional command-line or console command arguments are shown in
   square brackets, for example ``ls:step [frames]``
-  User Interface elements such as menu entries are referred to using
   **emphasis**
