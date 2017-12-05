# Logging

Logging is provided by [TinyLog](http://www.tinylog.org/), and configured in `conf/log.properties`.

The default configuration logs to the console only. To log to a file as well, use a configuration like this:

```
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
```

For available configuration options, see [TinyLog Configuration](http://www.tinylog.org/configuration).
