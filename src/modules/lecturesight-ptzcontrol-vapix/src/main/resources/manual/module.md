# Module Overview

VAPIX® is Axis’ own open API (Application Programming Interface) using
standard protocols enabling integration into a wide range of solutions on
different platforms. Almost all functionality available in Axis products can
be controlled using VAPIX®. VAPIX® is continuously developed and the main
interface to our products.

http://www.axis.com/techsup/cam_servers/dev/cam_http_api_index.php

The communication with the camera is based around Hypertext Transfer Protocol
(HTTP) response and requests. The returning value for success is
 - HTTP_NO_CONTENT (204): Command has been sent
 - HTTP_OK (200): Command sent

and response in text format. The returning text format is structured
as [propertyName]=[propertyValue]
