#include <stdio.h>
#include <stdlib.h>
#include "PTZCamVisca.h"

const char *PTZ_CAM_EXCEPTION = "cv/lecturesight/ptz/api/PTZCameraException";
const char *ILLEGAL_ARGUMENT_EXCEPTION = "java/lang/IllegalArgumentException";

/************************************************************/
/* visca map functions                                      */
/************************************************************/

VISCAMap *get_visca_elem(const char *port)
{
	VISCAMap *elem = NULL;

	if (!map)
	{
		map = create_visca_elem(port);
		return map;
	} 
    else 
    {
		elem = map;
		while (elem)
		{
			if (strcmp(port, elem->port) == 0)
			{
				return elem;
			}
            elem = elem->next;
		}

		/*no element exist (with the same port), create one*/
		elem = create_visca_elem(port);
		elem->next = map;
		map = elem;
		return elem;
	}
}

VISCAMap *create_visca_elem(const char *port)
{	
	VISCAMap *elem;

	elem = malloc(sizeof(VISCAMap));
	if (!elem)
	{
		fprintf(stderr, "can not malloc for VISCAMap!\n");
		return NULL;
	}

	elem->interface = malloc(sizeof(VISCAInterface_t));
	if (!elem->interface)
	{
		fprintf(stderr, "can not malloc for VISCAInterface_t!\n");
		remove_visca_elem(port);
		return NULL;
	}

    int i;
    for (i = 0; i < 7; i++)
    {
        elem->camera[i] = malloc(sizeof(VISCACamera_t));
    }
        
    elem->port = malloc(sizeof(char)*strlen(port));
    strcpy(elem->port, port);
        
    elem->initialized = 0;
    elem->cams_connected = 0;
	elem->next = NULL;
	return elem;
}

int remove_visca_elem(const char *port)
{
	VISCAMap *elem, *prev, *tmp;

	if (!port) return 0; /*fail*/
	if (!map) return 1; /*ready*/

	prev = NULL;
	elem = NULL;
	tmp = map;
	while (tmp) 
	{
		if (strcmp(port, tmp->port) != 0)
		{
			prev = tmp;
			tmp = tmp->next;
		} else {
			elem = tmp;
			tmp = NULL;
		}
	}
	if (!prev)
	{
		/*first element*/
		map = elem->next;
	} else {
		prev->next = elem->next;
	}
        
	free(elem->interface);
	free(elem->port);
	free(elem);
	return 1;
}


/************************************************************/
/* JNI interop functions                                    */
/************************************************************/

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

const char *get_port(JNIEnv *env, jobject obj)
{
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID fid = (*env)->GetFieldID(env, class, "port", "Ljava/lang/String;");
    return (*env)->GetStringUTFChars(env, (jstring) ((*env)->GetObjectField(env, obj, fid)), NULL);
}

const char *get_port_name(JNIEnv *env, jobject obj)
{
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID fid = (*env)->GetFieldID(env, class, "name", "Ljava/lang/String;");
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

void throwVISCAError(JNIEnv *env, const char *msg, uint32_t error_code)
{
    const char * error;

    switch (error_code)
    {
        case VISCA_ERROR_MESSAGE_LENGTH:
            error = "VISCA_ERROR_MESSAGE_LENGTH";
            break;
        case VISCA_ERROR_SYNTAX:
            error = "VISCA_ERROR_SYNTAX";
            break;
        case VISCA_ERROR_CMD_BUFFER_FULL:
            error = "VISCA_ERROR_CMD_BUFFER_FULL";
            break;
        case VISCA_ERROR_CMD_CANCELLED:
            error = "VISCA_ERROR_CMD_CANCELLED";
            break;
        case VISCA_ERROR_NO_SOCKET:
            error = "VISCA_ERROR_NO_SOCKET";
            break;
        case VISCA_ERROR_CMD_NOT_EXECUTABLE:
            error = "VISCA_ERROR_CMD_NOT_EXECUTABLE";
            break;
        default:
            error = "Unknown error";
    }

    throwException(env, PTZ_CAM_EXCEPTION, "%s: %s", error, msg);
}


/************************************************************/
/* JNI exported functions                                   */
/************************************************************/

int initialized(VISCAMap *elem)
{
    return elem && elem->initialized && elem->interface;
}

JNIEXPORT jboolean JNICALL 
Java_cv_lecturesight_ptz_visca_LibVISCACamera_initialize
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
        if (cam_no > elem->cams_connected) return 0;
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

    elem->interface->broadcast = 0;
    if (VISCA_set_address(elem->interface, &(elem->cams_connected)) != VISCA_SUCCESS)
    {
#ifdef DEBUG
        fprintf(stderr, "unable to get number of connected cams!\n");
#endif
        remove_visca_elem(c_port);
        return 0;
    }

#ifdef DEBUG
    printf("connected cams: %d\n", elem->cams_connected);
    printf("=============\n");
#endif

    if (cam_no > elem->cams_connected)
    {
        remove_visca_elem(c_port);
        return 0;
    }

    /*initialize all connected cams*/
    for (i = 0; i < elem->cams_connected; i++)
    {
#ifdef DEBUG
        printf("initialize cam...\n");
#endif
        elem->camera[i]->address = i + 1;
        if (VISCA_clear(elem->interface, elem->camera[i]) != VISCA_SUCCESS)
        {
#ifdef DEBUG
            fprintf(stderr, "unable to set camera number to %d\n", i);
#endif
            remove_visca_elem(c_port);
            return 0;
        }

        if (VISCA_get_camera_info(elem->interface, elem->camera[i]) != VISCA_SUCCESS)
        {
#ifdef DEBUG
            fprintf(stderr, "unable to get camera info");
#endif
            remove_visca_elem(c_port);
            return 0;
        }

#ifdef DEBUG
        printf("cam %d: model id = %lu, vendor id = %lu \n", i + 1, (unsigned long)elem->camera[i]->model, (unsigned long)elem->camera[i]->vendor);
#endif
    }

    elem->initialized = 1;
    return 1;
}

JNIEXPORT jboolean JNICALL 
Java_cv_lecturesight_ptz_visca_LibVISCACamera_deinitialize
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

JNIEXPORT jint JNICALL 
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getConnectedCams
(JNIEnv *env, jobject obj)
{
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (!initialized(elem)) return 0;

    return elem->cams_connected;
}

/* setters */

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPower
(JNIEnv *env, jobject obj, jboolean power)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint8_t power_value = power ? 2 : 3;
        int ret = VISCA_set_power(elem->interface, elem->camera[cam_no], power_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_power", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setIrLed
(JNIEnv *env, jobject obj, jboolean power)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint8_t power_value = power ? 2 : 3;
        int ret = VISCA_set_ir_led(elem->interface, elem->camera[cam_no], power_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_ir_led", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setZoomStop
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        int ret = VISCA_set_zoom_stop(elem->interface, elem->camera[cam_no]);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_zoom_stop", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setZoomTeleSpeed
(JNIEnv *env, jobject obj, jint speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t speed_value = speed;
        int ret = VISCA_set_zoom_tele_speed(elem->interface, elem->camera[cam_no], speed_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_zoom_tele_speed", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}


JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setZoomWideSpeed
(JNIEnv *env, jobject obj, jint speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t speed_value = speed;
        int ret = VISCA_set_zoom_wide_speed(elem->interface, elem->camera[cam_no], speed_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_zoom_wide_speed", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setZoomValue
(JNIEnv *env, jobject obj, jint zoom)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t zoom_value = zoom;
        int ret = VISCA_set_zoom_value(elem->interface, elem->camera[cam_no], zoom_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_zoom_value", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltUp
(JNIEnv *env, jobject obj, jint tilt_speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = 0;
        uint32_t tilt_value = tilt_speed;        
        int ret = VISCA_set_pantilt_up(elem->interface, elem->camera[cam_no], pan_value, tilt_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_up", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltDown
(JNIEnv *env, jobject obj, jint tilt_speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = 0;
        uint32_t tilt_value = tilt_speed;        
        int ret = VISCA_set_pantilt_down(elem->interface, elem->camera[cam_no], pan_value, tilt_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_down", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltLeft
(JNIEnv *env, jobject obj, jint pan_speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = pan_speed;
        uint32_t tilt_value = 0;        
        int ret = VISCA_set_pantilt_left(elem->interface, elem->camera[cam_no], pan_value, tilt_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_left", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltRight
(JNIEnv *env, jobject obj, jint pan_speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = pan_speed;
        uint32_t tilt_value = 0;        
        int ret = VISCA_set_pantilt_right(elem->interface, elem->camera[cam_no], pan_value, tilt_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_right", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltUpLeft
(JNIEnv *env, jobject obj, jint pan_speed, jint tilt_speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = pan_speed;
        uint32_t tilt_value = tilt_speed;        
        int ret = VISCA_set_pantilt_upleft(elem->interface, elem->camera[cam_no], pan_value, tilt_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_upleft", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltUpRight
(JNIEnv *env, jobject obj, jint pan_speed, jint tilt_speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = pan_speed;
        uint32_t tilt_value = tilt_speed;        
        int ret = VISCA_set_pantilt_upright(elem->interface, elem->camera[cam_no], pan_value, tilt_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_upright", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltDownLeft
(JNIEnv *env, jobject obj, jint pan_speed, jint tilt_speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = pan_speed;
        uint32_t tilt_value = tilt_speed;        
        int ret = VISCA_set_pantilt_downleft(elem->interface, elem->camera[cam_no], pan_value, tilt_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_upleft", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltDownRight
(JNIEnv *env, jobject obj, jint pan_speed, jint tilt_speed)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = pan_speed;
        uint32_t tilt_value = tilt_speed;        
        int ret = VISCA_set_pantilt_downright(elem->interface, elem->camera[cam_no], pan_value, tilt_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_downright", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltStop
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = 0;
        uint32_t tilt_value = 0;        
        int ret = VISCA_set_pantilt_stop(elem->interface, elem->camera[cam_no], pan_value, tilt_value);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_stop", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltAbsolutePosition
(JNIEnv *env, jobject obj, jint pan_speed, jint tilt_speed, jint pan_position, jint tilt_position)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = pan_speed;
        uint32_t tilt_value = tilt_speed;
        int pan_pos = pan_position;
        int tilt_pos = tilt_position;        
        int ret = VISCA_set_pantilt_absolute_position(elem->interface, elem->camera[cam_no], pan_value, tilt_value, pan_pos, tilt_pos);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_absolute_position", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltRelativePosition
(JNIEnv *env, jobject obj, jint pan_speed, jint tilt_speed, jint pan_position, jint tilt_position) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint32_t pan_value = pan_speed;
        uint32_t tilt_value = tilt_speed;
        int pan_pos = pan_position;
        int tilt_pos = tilt_position;        
        int ret = VISCA_set_pantilt_relative_position(elem->interface, elem->camera[cam_no], pan_value, tilt_value, pan_pos, tilt_pos);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_relative_position", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltHome
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        int ret = VISCA_set_pantilt_home(elem->interface, elem->camera[cam_no]);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_home", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltReset
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        int ret = VISCA_set_pantilt_reset(elem->interface, elem->camera[cam_no]);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_reset", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltLimitUpRight
(JNIEnv *env, jobject obj, jint pan_limit, jint tilt_limit) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        int pan_pos = pan_limit;
        int tilt_pos = tilt_limit;        
        int ret = VISCA_set_pantilt_limit_upright(elem->interface, elem->camera[cam_no], pan_pos, tilt_pos);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_limit_upright", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltLimitDownLeft
(JNIEnv *env, jobject obj, jint pan_limit, jint tilt_limit) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        int pan_pos = pan_limit;
        int tilt_pos = tilt_limit;        
        int ret = VISCA_set_pantilt_limit_downleft(elem->interface, elem->camera[cam_no], pan_pos, tilt_pos);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_limit_downleft", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltLimitDownLeftClear
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        int ret = VISCA_set_pantilt_limit_downleft_clear(elem->interface, elem->camera[cam_no]);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_limit_downleft_clear", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

JNIEXPORT void JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltLimitUprightClear
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        int ret = VISCA_set_pantilt_limit_upright_clear(elem->interface, elem->camera[cam_no]);
        if (ret != VISCA_SUCCESS) 
        {
            throwVISCAError(env, "Failed to VISCA_set_pantilt_limit_upright_clear", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
}

/* getters */

JNIEXPORT jint JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getVendor
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        return elem->camera[cam_no]->vendor;
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
        return 0;
    }    
}

JNIEXPORT jint JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getModel
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        return elem->camera[cam_no]->model;
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
        return 0;
    }    
}

JNIEXPORT jint JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getROMVersion
(JNIEnv *env, jobject obj)
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        return elem->camera[cam_no]->rom_version;
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
        return 0;
    }    
}

JNIEXPORT jboolean JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getPower
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint8_t power = 0;
        int ret = VISCA_get_power(elem->interface, elem->camera[cam_no], &power);
        if (ret == VISCA_SUCCESS) 
        {
            return (power == 2 ? 1 : 0);
        }
        else
        {
            throwVISCAError(env, "Failed to VISCA_get_power", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }    
    return 0;
}

JNIEXPORT jboolean JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getIrLed
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint8_t power = 0;
        int ret = VISCA_get_ir_led(elem->interface, elem->camera[cam_no], &power);
        if (ret == VISCA_SUCCESS) 
        {
            return (power == 2 ? 1 : 0);
        }
        else
        {
            throwVISCAError(env, "Failed to VISCA_get_ir_led", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getZoomValue
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint16_t zoom = 0;
        int ret = VISCA_get_zoom_value(elem->interface, elem->camera[cam_no], &zoom);
        if (ret == VISCA_SUCCESS) 
        {
            return zoom;
        }
        else
        {
            throwVISCAError(env, "Failed to VISCA_get_zoom_value", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
    return 0;
}

JNIEXPORT jboolean JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getFocusAuto
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint8_t fauto = 0;
        int ret = VISCA_get_focus_auto(elem->interface, elem->camera[cam_no], &fauto);
        if (ret == VISCA_SUCCESS) 
        {
            return (fauto == 2 ? 1 : 0);
        }
        else
        {
            throwVISCAError(env, "Failed to VISCA_get_focus_auto", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getFocusValue
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint16_t focus = 0;
        int ret = VISCA_get_focus_value(elem->interface, elem->camera[cam_no], &focus);
        if (ret == VISCA_SUCCESS) 
        {
            return focus;
        }
        else
        {
            throwVISCAError(env, "Failed to VISCA_get_focus_value", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getPanMaxSpeed
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint8_t pan_speed = 0;
        uint8_t tilt_speed = 0;
        int ret = VISCA_get_pantilt_maxspeed(elem->interface, elem->camera[cam_no], &pan_speed, &tilt_speed);
        if (ret == VISCA_SUCCESS) 
        {
            return pan_speed;
        }
        else
        {
            throwVISCAError(env, "Failed to VISCA_get_pantilt_maxspeed", ret);
        }        
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getPanPosition
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        int pan_pos = 0;
        int tilt_pos = 0;
        int ret = VISCA_get_pantilt_position(elem->interface, elem->camera[cam_no], &pan_pos, &tilt_pos);
        if (ret == VISCA_SUCCESS) 
        {
            return pan_pos;
        }
        else
        {
            throwVISCAError(env, "Failed to VISCA_get_pantilt_position", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getTiltMaxSpeed
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        uint8_t pan_speed = 0;
        uint8_t tilt_speed = 0;
        int ret = VISCA_get_pantilt_maxspeed(elem->interface, elem->camera[cam_no], &pan_speed, &tilt_speed);
        if (ret == VISCA_SUCCESS) 
        {
            return tilt_speed;
        }
        else
        {
            throwVISCAError(env, "Failed to VISCA_get_pantilt_maxspeed", ret);
        }      
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_cv_lecturesight_ptz_visca_LibVISCACamera_getTiltPosition
(JNIEnv *env, jobject obj) 
{
    int cam_no = get_cam_number(env, obj);
    const char *port = get_port(env, obj);
    VISCAMap *elem = get_visca_elem(port);

    if (initialized(elem)) 
    {
        int pan_pos = 0;
        int tilt_pos = 0;
        int ret = VISCA_get_pantilt_position(elem->interface, elem->camera[cam_no], &pan_pos, &tilt_pos);
        if (ret == VISCA_SUCCESS) 
        {
            return tilt_pos;
        }
        else
        {
            throwVISCAError(env, "Failed to VISCA_get_pantilt_position", ret);
        }
    }
    else
    {
        throwException(env, PTZ_CAM_EXCEPTION, "Not initialized!");
    }
    return 0;
}


