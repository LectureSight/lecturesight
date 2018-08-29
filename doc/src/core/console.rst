Console
=======

The console provides a command-line interface to LectureSight services
and the OSGI container.

!!! warning The shell provides access to all local files. Do not enable
console access from non-local IPs unless you have additional security
precautions in place such as a firewall.

The console shell is provided by `Apache Felix
Gogo <http://felix.apache.org/documentation/subprojects/apache-felix-gogo.html>`__

Access
------

The console is launched on startup by the ``start_lecturesight.sh``
script. To disable launching the console, add the property:

::

    -Dgosh.args=--noi

to the ``CONFIG_OPTS`` variable in the startup script.

Connect to the console from another terminal window using telnet:

::

    telnet localhost 2501

or netcat:

::

    nc localhost 2501

Configuration
-------------

Access to the console is configured in ``conf/config.properties``:

::

    osgi.shell.telnet.ip=127.0.0.1
    osgi.shell.telnet.port=2501
    osgi.shell.telnet.maxconn=2

Commands
--------

The following commands may be helpful:

+------------+-----------------------------------------+
| Command    | Action                                  |
+============+=========================================+
| help       | List available console commands         |
+------------+-----------------------------------------+
| lb         | List bundle status                      |
+------------+-----------------------------------------+
| scr:list   | Show Service Component Runtime status   |
+------------+-----------------------------------------+

Services
--------

The following services provide console commands:

+-------------------------+-----------------------------------------------------------------------------------------+
| Command Prefix          | Service                                                                                 |
+=========================+=========================================================================================+
| config                  | `Configuration <config#commands>`__                                                     |
+-------------------------+-----------------------------------------------------------------------------------------+
| cs                      | `Camera Steering Worker <../modules/steeringworker-relativemove/#console-commands>`__   |
+-------------------------+-----------------------------------------------------------------------------------------+
| display                 | Display Service                                                                         |
+-------------------------+-----------------------------------------------------------------------------------------+
| ls                      | `Heartbeat <heartbeat/#console-commands>`__                                             |
+-------------------------+-----------------------------------------------------------------------------------------+
| metrics                 | `Metrics <metrics/#console-commands>`__                                                 |
+-------------------------+-----------------------------------------------------------------------------------------+
| scheduler               | `Scheduler <scheduler/#console-commands>`__                                             |
+-------------------------+-----------------------------------------------------------------------------------------+
| va                      | `Video Analysis <../modules/videoanalysis/#console-commands>`__                         |
+-------------------------+-----------------------------------------------------------------------------------------+
| felix, gogo, obr, scr   | Commands provided by the OSGI container                                                 |
+-------------------------+-----------------------------------------------------------------------------------------+
