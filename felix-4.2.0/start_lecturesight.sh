DEBUG_PORT="8001"
DEBUG_SUSPEND="n"
DEBUG_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
FELIX_CACHE="./felix-cache"

rm -rf $FELIX_CACHE/*
java $DEBUG_OPTS -Dlecturesight.basedir=. -jar bin/felix.jar $FELIX_CACHE
