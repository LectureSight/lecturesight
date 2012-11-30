#ifndef PTZCamVisca_H
#define PTZCamVisca_H

#include <string.h>
#include "../libvisca-1.1.0/visca/libvisca.h"
#include "cv_lecturesight_ptz_visca_LibVISCACamera.h"

#define NODEBUG

#ifndef VISCAMap
typedef struct {
	char *port;
	int initialized;
    int cams_connected;
	VISCAInterface_t *interface;
    VISCACamera_t *camera[7];
	void *next;
} VISCAMap;
#endif

/* static member */
VISCAMap *map = NULL;

/* visca map functions */
VISCAMap *get_visca_elem(const char *port);
VISCAMap *create_visca_elem(const char *port);
int      remove_visca_elem(const char *port);

/* JNI interop functions */
int   get_cam_number(JNIEnv *, jobject);
void  set_cam_number(JNIEnv *, jobject, int cam_no);
const char *get_port(JNIEnv *, jobject);
const char *get_port_name(JNIEnv *, jobject);
void  set_port(JNIEnv *, jobject, const char *);

#endif /* PTZCamVisca_H */

