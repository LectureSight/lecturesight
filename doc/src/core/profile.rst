Scene Profile
=============

The Scene Profile defines the regions of the overview image that are
ignored for tracking purposes.

Profiles can be edited by the `Scene Profile Editor <../ui/profile/>`__
and are stored in the ``profiles`` directory.

Configuration
-------------

+-------------------------------+----------+-----------------------------------+
| Key                           | Default  | Description                       |
+===============================+==========+===================================+
| cv.lecturesight.profile.manag | default  | Name of the active profile.       |
| er.active.profile             |          |                                   |
+-------------------------------+----------+-----------------------------------+

Note that the default profile cannot be saved, so to use a custom
profile, create a new profile and update this setting with the new
profile name.
