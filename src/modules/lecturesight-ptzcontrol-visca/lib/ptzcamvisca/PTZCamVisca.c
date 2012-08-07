#include <stdio.h>
#include <stdlib.h>
#include "PTZCamVisca.h"
#include "VISCAPositions.h"

const char *PTZ_CAM_EXCEPTION = "cv/lecturesight/ptz/api/PTZCameraException";
const char *ILLEGAL_ARGUMENT_EXCEPTION = "java/lang/IllegalArgumentException";

/* Java object member geta/seta */
int get_cam_number(JNIEnv *, jobject);
void set_cam_number(JNIEnv *, jobject, int cam_no);
const jchar *get_port(JNIEnv *, jobject);
void set_port(JNIEnv *, jobject, const char *);
int initialize_position(const char *port, int cam_no);

int get_cam_number(JNIEnv *env, jobject obj)
{
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID fid = (*env)->GetFieldID(env, class, "camNo", "I");
    return (*env)->GetIntField(env, obj, fid) - 1;
}

void set_cam_number(JNIEnv *env, jobject obj, int cam_no)
{
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID fid = (*env)->GetFieldID(env, class, "camNo", "I");
    (*env)->SetIntField(env, obj, fid, cam_no);
}

const jchar *get_port(JNIEnv *env, jobject obj)
{
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID fid = (*env)->GetFieldID(env, class, "port", "Ljava/lang/String;");
    return (*env)->GetStringUTFChars(env, (jstring) ((*env)->GetObjectField(env, obj, fid)), NULL);
}

const jchar *get_port_name(JNIEnv *env, jobject obj)
{
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID fid = (*env)->GetFieldID(env, class, "portName", "Ljava/lang/String;");
    return (*env)->GetStringUTFChars(env, (jstring) ((*env)->GetObjectField(env, obj, fid)), NULL);
}

void set_port(JNIEnv *env, jobject obj, const char *port)
{
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID fid = (*env)->GetFieldID(env, class, "port", "Ljava/lang/String;");
    (*env)->SetObjectField(env, obj, fid, (*env)->NewStringUTF(env, port));
}

int throwException(JNIEnv *env, const char *exception, char *format, ...) 
{
    char message[256];
    va_list args;
    
    /* create message from variable arguments */
    va_start(args, format);
    vsprintf(message, format, args);
    va_end(args);
    
    /* throw exception */
    jclass exceptionCls = (*env)->FindClass(env, exception);
    if (exceptionCls == NULL) return 0;
    return ((*env)->ThrowNew(env, exceptionCls, message) < 0 ? 0 : 1 );
}

int initialized(VISCAMap *elem)
{
    return elem && elem->initialized && elem->interface && elem->visca_struct;
}

int initialize_position(const char *port, int cam_no) 
{
	VISCAMap *elem = get_visca_elem(port);
	
        /*get camera info*/
        VISCA_get_camera_info(elem->interface, elem->visca_struct->camera[cam_no]);
#ifdef DEBUG
        printf("vendor: 0x%04x\n model: 0x%04x\n ROM version: 0x%04x\n socket number: 0x%02x\n",
               elem->visca_struct->camera[cam_no]->vendor, elem->visca_struct->camera[cam_no]->model,
               elem->visca_struct->camera[cam_no]->rom_version, elem->visca_struct->camera[cam_no]->socket_num);
        printf("=============\n");
#endif

	return set_default_value(elem->visca_struct->camera[cam_no]->model,
		elem->visca_struct->min_position[cam_no],
		elem->visca_struct->max_position[cam_no],
		elem->visca_struct->min_speed[cam_no],
		elem->visca_struct->max_speed[cam_no],
		elem->visca_struct->speed[cam_no]);
}

JNIEXPORT jboolean JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_initialize
(JNIEnv *env, jobject obj, jstring port, jint cam_no)
{
    int i;
    const char *c_port = (*env)->GetStringUTFChars(env, port, 0);
    VISCAMap *elem;

    /*set cam no in java*/
    set_cam_number(env, obj, cam_no);
    set_port(env, obj, c_port);

    if (cam_no < 1 || cam_no > 7) return 0;

    elem = get_visca_elem(c_port);
    if (!elem) return 0;

    if (elem->initialized)
    {
        if (cam_no > elem->visca_struct->cams_connected) return 0;
        return 1;
    }

    if (VISCA_open_serial(elem->interface, c_port) != VISCA_SUCCESS)
    {
#ifdef DEBUG
        fprintf(stderr, "unable to open serial device %s\n", c_port);
#endif
        remove_visca_elem(c_port);
        return 0;
    }

#ifdef DEBUG
    printf("comm port open success!\n");
#endif

    elem->interface->broadcast = 0;
    if (VISCA_set_address(elem->interface, &(elem->visca_struct->cams_connected)) != VISCA_SUCCESS)
    {
#ifdef DEBUG
        fprintf(stderr, "unable to get number of connected cams!\n");
#endif
        remove_visca_elem(c_port);
        return 0;
    }

#ifdef DEBUG
    printf("connected cams: %d\n", elem->visca_struct->cams_connected);
    printf("=============\n");
#endif

    if (cam_no > elem->visca_struct->cams_connected)
    {
        remove_visca_elem(c_port);
        return 0;
    }

    /*initialize all connected cams*/
    for (i = 0; i < elem->visca_struct->cams_connected; i++)
    {
#ifdef DEBUG
        printf("initialize cam...\n");
#endif
        elem->visca_struct->camera[i]->address = i + 1;
        if (VISCA_clear(elem->interface, elem->visca_struct->camera[i]) != VISCA_SUCCESS)
        {
#ifdef DEBUG
            fprintf(stderr, "unable to set camera number to %d\n", i);
#endif
            remove_visca_elem(c_port);
            return 0;
        }

#ifdef DEBUG
        printf("cam no set to %d\n", i + 1);
#endif


	if (initialize_position(c_port, i) == 0)
	{
#ifdef DEBUG
            fprintf(stderr, "unable to set camera position values!\n");
#endif
            remove_visca_elem(c_port);
            return 0;
	}
    }

    elem->initialized = 1;
    return 1;
}

JNIEXPORT jboolean JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_deinitialize
(JNIEnv *env, jobject obj)
{
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

#ifdef DEBUG
	printf("deinitialize %s ", port);
#endif

    if (VISCA_close_serial(elem->interface) == VISCA_SUCCESS)
    {
#ifdef DEBUG
		printf("success!\n");
#endif
        return remove_visca_elem(port);
    }
    else
    {
#ifdef DEBUG
		printf("fail!\n");
#endif
        return 0;
    }
}

JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_getConnectedCams
  (JNIEnv *env, jobject obj)
{
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return 0;

    return elem->visca_struct->cams_connected;
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_setPower
(JNIEnv *env, jobject obj, jboolean power)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;
    

    if (VISCA_set_power(elem->interface, elem->visca_struct->camera[cam_no], (power ? 2 : 3)) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not set power %s (port '%s' camera %d)", (power?"on":"off"), port_name, cam_no+1);
    }
}

JNIEXPORT jboolean JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_isPowerOn
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    uint8_t power = 0;

    if (!initialized(elem)) return 0;
    if (VISCA_get_power(elem->interface, elem->visca_struct->camera[cam_no], &power) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not get power state (port '%s' camera %d)", port_name, cam_no+1);
        return 0;

    }
    else
    {
        return (power == 2 ? 1 : 0);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_setPanSpeed
(JNIEnv *env, jobject obj, jfloat pan_speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    uint8_t min, max;

    if (!initialized(elem)) return;

    if (pan_speed < 0 || pan_speed > 1) 
    {
        throwException(env, ILLEGAL_ARGUMENT_EXCEPTION,
                "Pan speed value out of range [0.0 .. 1.0] (port '%s' camera %d)", port_name, cam_no+1);
        return;
    }

    min = elem->visca_struct->min_speed[cam_no]->pan_speed;
    max = elem->visca_struct->max_speed[cam_no]->pan_speed;
    elem->visca_struct->speed[cam_no]->pan_speed = min + ((max - min) * pan_speed);

#ifdef DEBUG
    printf("panspeed set to %d\n", elem->visca_struct->speed[cam_no]->pan_speed);
#endif
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_setTiltSpeed
(JNIEnv *env, jobject obj, jfloat tilt_speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    uint8_t min, max;

    if (!initialized(elem)) return;
    
    if (tilt_speed < 0 || tilt_speed > 1)
    {
        throwException(env, ILLEGAL_ARGUMENT_EXCEPTION,
                "Tilt speed value out of range [0.0 .. 1.0] (port '%s' camera %d)", port_name, cam_no+1);
        return;
    }

    min = elem->visca_struct->min_speed[cam_no]->tilt_speed;
    max = elem->visca_struct->max_speed[cam_no]->tilt_speed;
    if (tilt_speed < 0 || tilt_speed > 1) return;

    elem->visca_struct->speed[cam_no]->tilt_speed = min + ((max - min) * tilt_speed);

#ifdef DEBUG
	printf("tiltspeed min: %d, max: %d, curr: %d\n", min, max, elem->visca_struct->speed[cam_no]->tilt_speed);
    printf("tiltspeed set to %d\n", elem->visca_struct->speed[cam_no]->tilt_speed);
#endif
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_setZoomSpeed
(JNIEnv *env, jobject obj, jfloat zoom_value)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    unsigned int min_tele, max_tele, min_wide, max_wide, value;

    if (!initialized(elem)) return;

    min_tele = elem->visca_struct->min_speed[cam_no]->zoom_speed_tele;
    min_wide = elem->visca_struct->min_speed[cam_no]->zoom_speed_wide;
    max_tele = elem->visca_struct->max_speed[cam_no]->zoom_speed_tele;
    max_wide = elem->visca_struct->max_speed[cam_no]->zoom_speed_wide;

    if (zoom_value < 0 || zoom_value > 1) 
    {
        throwException(env, ILLEGAL_ARGUMENT_EXCEPTION,
                "Zoom speed value out of range [0.0 .. 1.0] (port '%s' camera %d)", port_name, cam_no+1);
        return;
    }

    /*set tele speed*/
    value = min_tele + ((max_tele - min_tele) * zoom_value);
    if (VISCA_set_zoom_tele_speed(elem->interface, elem->visca_struct->camera[cam_no], value) == VISCA_SUCCESS)
    {
        elem->visca_struct->speed[cam_no]->zoom_speed_tele = value;
#ifdef DEBUG
        printf("set zoom tele speed to %d\n", value);
#endif
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not set zoom speed to %.2f (port '%s' camera %d)", zoom_value, port_name, cam_no+1);
        return;
    }

    /*set wide speed*/
    value = min_wide + ((max_wide - min_wide) * zoom_value);
    if (VISCA_set_zoom_wide_speed(elem->interface, elem->visca_struct->camera[cam_no], value) == VISCA_SUCCESS)
    {
        elem->visca_struct->speed[cam_no]->zoom_speed_wide = value;
#ifdef DEBUG
        printf("set zoom wide speed to %d\n", value);
#endif
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not set zoom speed to %.2f (port '%s' camera %d)", zoom_value, port_name, cam_no+1);
    }
}

JNIEXPORT jfloat JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_getPanSpeed
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    unsigned int min;

    if (!initialized(elem)) return 2;

#ifdef DEBUG
    printf("current panspeed is %d\n", elem->visca_struct->speed[cam_no]->pan_speed);
#endif

    min = elem->visca_struct->min_speed[cam_no]->pan_speed;
    return (jfloat) (elem->visca_struct->speed[cam_no]->pan_speed - min) / (jfloat) (elem->visca_struct->max_speed[cam_no]->pan_speed - min);
}

JNIEXPORT jfloat JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_getTiltSpeed
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    unsigned int min;

    if (!initialized(elem)) return 2;

#ifdef DEBUG
    printf("current tiltspeed is %d\n", elem->visca_struct->speed[cam_no]->tilt_speed);
#endif

    min = elem->visca_struct->min_speed[cam_no]->tilt_speed;
    return (jfloat) (elem->visca_struct->speed[cam_no]->tilt_speed - min) / (jfloat) (elem->visca_struct->max_speed[cam_no]->tilt_speed - min);
}

JNIEXPORT jfloat JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_getZoomSpeed
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    unsigned int min, max, curr;

    if (!initialized(elem)) return 2;

    min = elem->visca_struct->min_speed[cam_no]->zoom_speed_tele;
    max = elem->visca_struct->max_speed[cam_no]->zoom_speed_tele;
    curr = elem->visca_struct->speed[cam_no]->zoom_speed_tele;

    /*tele and wide speed are the same, also return one of them*/
    return (jfloat) (curr - min) / (jfloat) (max - min);
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_backlight
(JNIEnv *env, jobject obj, jboolean backlight)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    uint8_t backlight8 = (backlight ? 2 : 3);

    if (!initialized(elem)) return;

    if (VISCA_set_backlight_comp(elem->interface, elem->visca_struct->camera[cam_no], backlight8) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not set backlight '%s' (port '%s' camera %d)", (backlight?"on":"off"), port_name, cam_no+1);
    }
}

JNIEXPORT jboolean JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_isBacklight
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    uint8_t backlight = 3;

    if (!initialized(elem)) return 0;

    if (VISCA_get_backlight_comp(elem->interface, elem->visca_struct->camera[cam_no], &backlight) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not get backlight state (port '%s' camera %d)", port_name, cam_no+1);
        return 0;
    }

    return backlight == 2 ? 1 : 0;
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_zoomIn
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_zoom_tele(elem->interface, elem->visca_struct->camera[cam_no]) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not zoom in (port '%s' camera %d)", port_name, cam_no+1);
        
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_zoomOut
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_zoom_wide(elem->interface, elem->visca_struct->camera[cam_no]) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not zoom out (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_stopZoom
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_zoom_stop(elem->interface, elem->visca_struct->camera[cam_no]) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not stop zooming (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT jfloat JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_getZoom
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    uint16_t zoom_value = 0;
    int min, max;

    if (!initialized(elem)) return zoom_value;

    if (VISCA_get_zoom_value(elem->interface, elem->visca_struct->camera[cam_no], &zoom_value) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not get zoom value (port '%s' camera %d)", port_name, cam_no+1);
        return 0.0;
    }
    else
    {
#ifdef DEBUG
        printf("zoom value: %d\n", zoom_value);
#endif
    }

    min = elem->visca_struct->min_position[cam_no]->zoom;
    max = elem->visca_struct->max_position[cam_no]->zoom;
    
    return ((jfloat) (zoom_value - min) / (jfloat) (max - min));
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_setZoom
(JNIEnv *env, jobject obj, jfloat zoom_value)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    unsigned int value;
    int min, max;

    if (!initialized(elem)) return;
    if (zoom_value < 0 || zoom_value > 1)
    {
        throwException(env, ILLEGAL_ARGUMENT_EXCEPTION,
                "Zoom value out of range [0.0 .. 1.0] (port '%s' camera %d)", port_name, cam_no+1);
        return;
    }
    
    zoom_value = 1.0 - zoom_value;
    min = elem->visca_struct->min_position[cam_no]->zoom;
    max = elem->visca_struct->max_position[cam_no]->zoom;
    value = min + (((jfloat)(max - min)) * zoom_value);

    if (VISCA_set_zoom_value(elem->interface, elem->visca_struct->camera[cam_no], value) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not set zoom value to %.2f (port '%s' camera %d)", zoom_value, port_name, cam_no+1);
    }
    else
    {
#ifdef DEBUG
        printf("set zoom to %d\n", value);
#endif
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_move
(JNIEnv *env, jobject obj, jfloat pan, jfloat tilt)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    VISCAPosition_t *pos;

    if (!initialized(elem)) return;

    pos = malloc(sizeof (VISCAPosition_t));

    if (pan < -1 || tilt < -1 || pan > 1 || tilt > 1 || !pos) 
    {
        throwException(env, ILLEGAL_ARGUMENT_EXCEPTION,
                "Pan/tilt value out of range [-1.0 .. 1.0] (port '%s' camera %d)", port_name, cam_no+1);
        return;
    }

    if (pan < 0)
    {
        pos->pan = 0xFFFF - ((0xFFFF - elem->visca_struct->min_position[cam_no]->pan) * (pan * -1)) / 16;
    }
    else
    {
        pos->pan = elem->visca_struct->max_position[cam_no]->pan * pan / 16;
    }

    if (tilt < 0)
    {
        pos->tilt = 0xFFFF - ((0xFFFF - elem->visca_struct->min_position[cam_no]->tilt) * (tilt * -1)) / 16;
    }
    else
    {
        pos->tilt = elem->visca_struct->max_position[cam_no]->tilt * tilt / 16;
    }

#ifdef DEBUG
    printf("cam %d move(%.2f, %.2f) -> pan: 0x%x, tilt: 0x%x\n", cam_no, pan, tilt, pos->pan, pos->tilt);
#endif

    if (VISCA_set_pantilt_absolute_position(elem->interface, elem->visca_struct->camera[cam_no],
                                            elem->visca_struct->speed[cam_no]->pan_speed, elem->visca_struct->speed[cam_no]->tilt_speed,
                                            pos->pan, pos->tilt) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not move camera to pan-position %.2f tilt-position %.2f (port '%s' camera %d)", pan, tilt, port_name, cam_no+1);
    }

    free(pos);
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_limitPanTiltUpRight
(JNIEnv *env, jobject obj, jfloat pan, jfloat tilt)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    VISCAPosition_t *pos;

    if (!initialized(elem)) return;

    pos = malloc(sizeof (VISCAPosition_t));

    if (pan < -1 || tilt < -1 || pan > 1 || tilt > 1 || !pos) 
    {
        throwException(env, ILLEGAL_ARGUMENT_EXCEPTION,
                "Pan/tilt value out of range [-1.0 .. 1.0] (port '%s' camera %d)", port_name, cam_no+1);
        return;
    }

    if (pan < 0)
    {
        pos->pan = 0xFFFF - ((0xFFFF - elem->visca_struct->min_position[cam_no]->pan) * (pan * -1)) / 16;
    }
    else
    {
        pos->pan = elem->visca_struct->max_position[cam_no]->pan * pan / 16;
    }

    if (tilt < 0)
    {
        pos->tilt = 0xFFFF - ((0xFFFF - elem->visca_struct->min_position[cam_no]->tilt) * (tilt * -1)) / 16;
    }
    else
    {
        pos->tilt = elem->visca_struct->max_position[cam_no]->tilt * tilt / 16;
    }

#ifdef DEBUG
    printf("cam %d move(%.2f, %.2f) -> pan: 0x%x, tilt: 0x%x\n", cam_no, pan, tilt, pos->pan, pos->tilt);
#endif

    if (VISCA_set_pantilt_limit_upright(elem->interface, elem->visca_struct->camera[cam_no], pos->pan, pos->tilt) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not set move limit Up/Right to pan-position %.2f tilt-position %.2f (port '%s' camera %d)", pan, tilt, port_name, cam_no+1);
    }

    free(pos);
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_limitPanTiltDownLeft
(JNIEnv *env, jobject obj, jfloat pan, jfloat tilt)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    VISCAPosition_t *pos;

    if (!initialized(elem)) return;

    pos = malloc(sizeof (VISCAPosition_t));

    if (pan < -1 || tilt < -1 || pan > 1 || tilt > 1 || !pos) 
    {
        throwException(env, ILLEGAL_ARGUMENT_EXCEPTION,
                "Pan/tilt value out of range [-1.0 .. 1.0] (port '%s' camera %d)", port_name, cam_no+1);
        return;
    }

    if (pan < 0)
    {
        pos->pan = 0xFFFF - ((0xFFFF - elem->visca_struct->min_position[cam_no]->pan) * (pan * -1)) / 16;
    }
    else
    {
        pos->pan = elem->visca_struct->max_position[cam_no]->pan * pan / 16;
    }

    if (tilt < 0)
    {
        pos->tilt = 0xFFFF - ((0xFFFF - elem->visca_struct->min_position[cam_no]->tilt) * (tilt * -1)) / 16;
    }
    else
    {
        pos->tilt = elem->visca_struct->max_position[cam_no]->tilt * tilt / 16;
    }

#ifdef DEBUG
    printf("cam %d move(%.2f, %.2f) -> pan: 0x%x, tilt: 0x%x\n", cam_no, pan, tilt, pos->pan, pos->tilt);
#endif

    if (VISCA_set_pantilt_limit_downleft(elem->interface, elem->visca_struct->camera[cam_no], pos->pan, pos->tilt) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not set move limit Down/Left to pan-position %.2f tilt-position %.2f (port '%s' camera %d)", pan, tilt, port_name, cam_no+1);
    }

    free(pos);
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_moveDown
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_pantilt_down(elem->interface, elem->visca_struct->camera[cam_no],
                               elem->visca_struct->speed[cam_no]->pan_speed,
                               elem->visca_struct->speed[cam_no]->tilt_speed) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not move down (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_moveUp
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_pantilt_up(elem->interface, elem->visca_struct->camera[cam_no],
                             elem->visca_struct->speed[cam_no]->pan_speed,
                             elem->visca_struct->speed[cam_no]->tilt_speed) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION,
                "Can not move up (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_moveLeft
(JNIEnv *env, jobject obj)
{
printf("Moving Left");
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_pantilt_left(elem->interface, elem->visca_struct->camera[cam_no],
                               elem->visca_struct->speed[cam_no]->pan_speed,
                               elem->visca_struct->speed[cam_no]->tilt_speed) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not move left (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_moveRight
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_pantilt_right(elem->interface, elem->visca_struct->camera[cam_no],
                                elem->visca_struct->speed[cam_no]->pan_speed,
                                elem->visca_struct->speed[cam_no]->tilt_speed) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not move right (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_stopMove
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_pantilt_stop(elem->interface, elem->visca_struct->camera[cam_no], elem->visca_struct->speed[cam_no]->pan_speed, elem->visca_struct->speed[cam_no]->tilt_speed) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not stop moving (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT jfloat JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_getFocus
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    uint16_t focus;
    int focus_min, focus_max;

    if (!initialized(elem)) return 0;

    if (VISCA_get_focus_value(elem->interface, elem->visca_struct->camera[cam_no], &focus) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not get focus (port '%s' camera %d)", port_name, cam_no+1);
        return 0;
    }

    focus_min = elem->visca_struct->min_position[cam_no]->focus;
    focus_max = elem->visca_struct->max_position[cam_no]->focus;

    return 1.0 - ((jfloat)(focus - focus_min) / (jfloat) (focus_max - focus_min));
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_setFocus
(JNIEnv *env, jobject obj, jfloat focus)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    unsigned int focus_value;
    int focus_min, focus_max;

    if (!initialized(elem)) return;
    
    if (focus < 0 || focus > 1)
    {
        throwException(env, ILLEGAL_ARGUMENT_EXCEPTION,
                "Focus value out of range [0.0 .. 1.0] (port '%s' camera %d)", port_name, cam_no+1);
        return;
    }

    focus = 1 - focus;
    focus_min = elem->visca_struct->min_position[cam_no]->focus;
    focus_max = elem->visca_struct->max_position[cam_no]->focus;
    focus_value = focus_min + (((float) (focus_max - focus_min)) * focus);

#ifdef DEBUG
    printf("set focus to %.2f (0x%x)\n", focus, focus_min + (((float) (focus_max - focus_min)) * focus));
#endif

    if (VISCA_set_focus_value(elem->interface, elem->visca_struct->camera[cam_no], focus_value) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not set focus to %.2f (port '%s' camera %d)", focus, port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_focusFar
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_focus_far(elem->interface, elem->visca_struct->camera[cam_no]) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not move focus to far (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_focusNear
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_focus_near(elem->interface, elem->visca_struct->camera[cam_no]) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not move focus to near (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_focusStop
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_focus_stop(elem->interface, elem->visca_struct->camera[cam_no]) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not stop moving focus (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_setAutoFocus
(JNIEnv *env, jobject obj, jboolean autoFocus)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_focus_auto(elem->interface, elem->visca_struct->camera[cam_no], (autoFocus ? 2 : 3)) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not set autofocus %s (port '%s' camera %d)", (autoFocus?"on":"off"), port_name, cam_no+1);
    }
}

JNIEXPORT jboolean JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_isAutoFocusSet
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    uint8_t auto_focus = 3;

    if (!initialized(elem)) return auto_focus;

    if (VISCA_get_focus_auto(elem->interface, elem->visca_struct->camera[cam_no], &auto_focus) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not get autofocus value (port '%s' camera %d)", port_name, cam_no+1);
        return 0;
    }

    return auto_focus == 2 ? 1 : 0;
}

JNIEXPORT jfloat JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_getPan
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    int pan_pos = 0, tilt_pos = 0;
    float result;

    if (!initialized(elem)) return 0;

    if (VISCA_get_pantilt_position(elem->interface, elem->visca_struct->camera[cam_no], &pan_pos, &tilt_pos) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not get pan position (port '%s' camera %d)", port_name, cam_no+1);
        return 0.0;
    }
    
/*    if (pan_pos > elem->visca_struct->max_position[cam_no]->pan)
    {
        result =  -1.0 + (float)(
                (float)(pan_pos - elem->visca_struct->min_position[cam_no]->pan) / 
                (float)(0xFFFF - elem->visca_struct->min_position[cam_no]->pan));
    }
    else
    {
        result = (float)((float) pan_pos  / (float) elem->visca_struct->max_position[cam_no]->pan);
    }
*/
    if (pan_pos > 0)
    {
    	result = (float)pan_pos / (float)elem->visca_struct->max_position[cam_no]->pan;
    }
    else if (pan_pos < 0) 
    {
	result = -1.0 * ((float)pan_pos / (float)elem->visca_struct->min_position[cam_no]->pan);
    }
    else
    {
	result = 0.0;
    }
    
#ifdef DEBUG
    printf("pan : %f (%i) [%i..%i]\n", result, pan_pos, elem->visca_struct->min_position[cam_no]->pan, elem->visca_struct->max_position[cam_no]->pan);
#endif
    return result;
}

JNIEXPORT jfloat JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_getTilt
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);
    int pan_pos = 0, tilt_pos = 0;
    float result;

    if (!initialized(elem)) return 0;

    if (VISCA_get_pantilt_position(elem->interface, elem->visca_struct->camera[cam_no], &pan_pos, &tilt_pos) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not get tilt position (port '%s' camera %d)", port_name, cam_no+1);
        return 0.0;
    }

/*
    if (tilt_pos > elem->visca_struct->max_position[cam_no]->tilt)
    {
        result = -1.0 + (float)(
                (float)(tilt_pos - elem->visca_struct->min_position[cam_no]->tilt) / 
                (float)(0xFFFF - elem->visca_struct->min_position[cam_no]->tilt));
    }
    else
    {
        result = (float)((float) tilt_pos / (float) elem->visca_struct->max_position[cam_no]->tilt);
    }
*/

    if (tilt_pos > 0)
    {
    	result = (float)tilt_pos / (float)elem->visca_struct->max_position[cam_no]->tilt;
    }
    else if (tilt_pos < 0) 
    {
	result = -1.0 * (float)tilt_pos / (float)elem->visca_struct->min_position[cam_no]->tilt;
    }
    else
    {
	result = 0.0;
    }
    
#ifdef DEBUG
    printf("tilt: %f (%i) [%i..%i]\n", result, tilt_pos, elem->visca_struct->min_position[cam_no]->tilt, elem->visca_struct->max_position[cam_no]->tilt);
#endif
    return result;
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_moveUpLeft
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_pantilt_upleft(elem->interface, elem->visca_struct->camera[cam_no],
                                 elem->visca_struct->speed[cam_no]->pan_speed,
                                 elem->visca_struct->speed[cam_no]->tilt_speed) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not move up/left (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_moveUpRight
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_pantilt_upright(elem->interface, elem->visca_struct->camera[cam_no],
                                  elem->visca_struct->speed[cam_no]->pan_speed,
                                  elem->visca_struct->speed[cam_no]->tilt_speed) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not move up/right (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_moveDownLeft
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_pantilt_downleft(elem->interface, elem->visca_struct->camera[cam_no],
                                   elem->visca_struct->speed[cam_no]->pan_speed,
                                   elem->visca_struct->speed[cam_no]->tilt_speed) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not move up/left (port '%s' camera %d)", port_name, cam_no+1);
    }
}

JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_VISCACamera_moveDownRight
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    const char *port_name = get_port_name(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return;

    if (VISCA_set_pantilt_downright(elem->interface, elem->visca_struct->camera[cam_no],
                                    elem->visca_struct->speed[cam_no]->pan_speed,
                                    elem->visca_struct->speed[cam_no]->tilt_speed) != VISCA_SUCCESS)
    {
        throwException(env, PTZ_CAM_EXCEPTION, 
                "Can not move up/left (port '%s' camera %d)", port_name, cam_no+1);
    }
}

