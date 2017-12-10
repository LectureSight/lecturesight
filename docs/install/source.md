# Build from source

You can build LectureSight from source if you want to run newer code than the last release, or need to make local changes.

Check that you have [git](https://git-scm.com/) and [Apache Maven](https://maven.apache.org/install.html) installed.

Create a destination folder for LectureSight:

    mkdir -p /opt/ls/bundles/application

Clone the LectureSight repo:

    git clone https://bitbucket.org/bwulff/lecturesight.git

Check out the branch to build:

    cd lecturesight
    git checkout 0.3-sprint

Copy the runtime OSGI bundles and configuration files:

    cp -R runtime/* /opt/ls/

Edit the `production-with-gui` maven profile in `src/pom.xml` to enable or disable the camera modules that you need for your installation.

Build LectureSight and install the resulting OSGI bundles:

    cd src
    mvn clean install -DdeployTo=/opt/ls/bundles/application

A successful build should end like this:

````
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 30.631 s
[INFO] Finished at: 2017-12-07T17:07:41+02:00
[INFO] Final Memory: 81M/429M
[INFO] ------------------------------------------------------------------------
````

You can now [Start LectureSight](start)

