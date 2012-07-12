FELIX_CACHE="./felix-cache"
rm -rf $FELIX_CACHE/*
java -Dlecturesight.basedir=. -jar bin/felix.jar $FELIX_CACHE  
