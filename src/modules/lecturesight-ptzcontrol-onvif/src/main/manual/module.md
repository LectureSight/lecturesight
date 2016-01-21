# Module Overview
Open Network Video Interface Forum (ONVIF: http://www.onvif.org/) is a
community to standardize communication between IP-based security products, in
this case PTZ cameras.

This camera implementation is based around the ONVIF wrapper classes written
by Milgo and available on GitHub at: https://github.com/milg0/onvif-java-lib.
The onvif-java-lib is deployed under the Apache License,
Version 2.0 of January 2004 (http://www.apache.org/licenses/).

The communication with the camera is defined as a web service, the wrapper
handles most of the Simple Object Access Protocol (SOAP) messaging by using
Java Architecture for XML Binding (JAXB) to map the objects to Extensible
Markup Language (XML).

The Web Service Definition Language (WSDL) for the different versions and
devices:

Device Management:
1.0 : http://www.onvif.org/ver10/device/wsdl/devicemgmt.wsdl
2.0 : http://www.onvif.org/ver20/ptz/wsdl/ptz.wsdl

Media:
  1.0 : http://www.onvif.org/ver10/media/wsdl/media.wsdl
  2.0 : http://www.onvif.org/ver20/media/wsdl/media.wsdl

PTZ:
  1.0 : http://www.onvif.org/onvif/ver10/ptz/wsdl/ptz.wsdl
  2.0 : http://www.onvif.org/ver20/ptz/wsdl/ptz.wsdl

All:
  2.0 : http://www.onvif.org/onvif/ver20/util/operationIndex.html
