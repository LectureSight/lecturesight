Build from source
=================

You can build LectureSight from source if you want to run newer code
than the last release, or need to make local changes.

Check that you have `git <https://git-scm.com/>`__ and `Apache
Maven <https://maven.apache.org/install.html>`__ installed.

Create a destination folder for LectureSight:

::

    mkdir -p /opt/ls/bundles/application

Clone the LectureSight repo:

::

    git clone https://bitbucket.org/bwulff/lecturesight.git

Check out the branch to build:

::

    cd lecturesight
    git checkout 0.3-sprint

Copy the runtime OSGI bundles and configuration files:

::

    cp -R runtime/* /opt/ls/

By default the ``demonstration`` profile will be built, this deploys a
dummy PTZ camera so you run LectureSight without access to a real PTZ
camera.

Build the LectureSight ``demonstration`` profile and install the
resulting OSGI bundles:

::

    cd src
    mvn clean install -DdeployTo=/opt/ls/bundles/application

Alternatively, edit the ``production-with-gui`` maven profile in
``src/pom.xml`` to enable or disable the camera modules that you need
for your installation, (see the ``ptz-####-only`` profiles for the
required modules).

::

    cd src
    mvn clean install -DdeployTo=/opt/ls/bundles/application -Pproduction-with-gui

A successful build should end like this:

::

    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 30.631 s
    [INFO] Finished at: 2017-12-07T17:07:41+02:00
    [INFO] Final Memory: 81M/429M
    [INFO] ------------------------------------------------------------------------

You can now `Start LectureSight <start>`__
