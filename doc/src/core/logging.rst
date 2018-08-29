Logging
=======

Logging is provided by `TinyLog <http://www.tinylog.org/>`__, and
configured in ``conf/log.properties``.

!!! tip `LS-187 <http://opencast.jira.com/browse/LS-187>`__: Exceptions
during bundle startup are not logged by TinyLog, and will be visible on
the console only

The default configuration logs to the console. This configuration will
log to console and file:

::

    tinylog.level = debug

    # INFO/ERROR Logging to console
    tinylog.writer1 = console
    tinylog.writer1.format = {date:HH:mm:ss} {level}: {class_name}.{method}() -- {message}
    tinylog.writer1.level = info

    # DEBUG/INFO/ERROR logging to file
    tinylog.writer2 = file
    tinylog.writer2.filename = log/ls.log
    tinylog.writer2.level = DEBUG
    tinylog.writer2.format = {date:yyyy-MM-dd HH:mm:ss.SSS} {{level}|min-size=7} {thread} {class_name}.{method}() : {message}

Available configuration options are described in `TinyLog
Configuration <http://www.tinylog.org/configuration>`__.
