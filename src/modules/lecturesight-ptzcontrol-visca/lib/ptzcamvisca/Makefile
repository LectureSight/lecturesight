#JAVA_HOME 	?= /usr/lib/jvm/java-6-openjdk-amd64
LIBVISCA_PATH	= ../libvisca-1.1.0/visca

CC     		= gcc
CFLAGS 		= -Wall -fPIC -O2
JAVAH		= javah
OS		:= $(shell uname -s)
MACHINE		:= $(shell uname -m)

################################################################################
default: install

libptzcamvisca.so: cv_lecturesight_ptz_visca_VISCACamera.h PTZCamVisca.o libvisca.o
	$(CC) $(CFLAGS) -shared -o libptzcamvisca.so PTZCamVisca.o $(LIBVISCA_PATH)/.libs/libvisca*.o
	
cv_lecturesight_ptz_visca_VISCACamera.h: 
	$(JAVAH) -classpath ../../target/classes cv.lecturesight.ptz.visca.VISCACamera
	
PTZCamVisca.o: PTZCamVisca.c PTZCamVisca.h VISCAPositions.h
	$(CC) $(CFLAGS) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux -c PTZCamVisca.c

libvisca.o: 
	cd $(LIBVISCA_PATH) && make

clean:
	rm -f *.o
	rm -f libptzcamvisca.so
	cd $(LIBVISCA_PATH) && make clean
	rm -f *~

install: libptzcamvisca.so
	mkdir -p ../../src/main/resources/lib/$(OS)/$(MACHINE)
	cp libptzcamvisca.so ../../src/main/resources/lib/$(OS)/$(MACHINE)/libptzcamvisca.so

rebuild: clean default
