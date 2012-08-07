#ifndef PTZCamVisca_H
#define PTZCamVisca_H

#include <string.h>
#include "../libvisca-1.1.0/visca/libvisca.h"
#include "cv_lecturesight_ptz_visca_VISCACamera.h"

#define DEBUG

#ifndef VISCACamSpeed_t
	typedef struct {
		uint8_t pan_speed, tilt_speed;
		unsigned int zoom_speed_tele, zoom_speed_wide;
	} VISCACamSpeed_t;
#endif

#ifndef VISCAPosition_t
	typedef struct {
		int pan, tilt, zoom, focus;
	} VISCAPosition_t;
#endif

#ifndef VISCAStruct
	typedef struct {
		VISCACamera_t *camera[7];
		VISCACamSpeed_t *speed[7];
		VISCACamSpeed_t *min_speed[7];
		VISCACamSpeed_t *max_speed[7];
		VISCAPosition_t *min_position[7];
		VISCAPosition_t *max_position[7];
		int cams_connected;
	} VISCAStruct;
#endif

#ifndef VISCAMap
	typedef struct {
		char *port;
		unsigned int initialized;
		VISCAInterface_t *interface;
		VISCAStruct *visca_struct;
		void *next;
	} VISCAMap;
#endif

VISCAMap *get_visca_elem(const char *port);
VISCAMap *create_visca_elem(const char *port);
int remove_visca_elem(const char *port);

VISCAStruct *create_visca_struct();
int delete_visca_struct(VISCAStruct *visca_struct);

/*static member*/
VISCAMap *map = NULL;


/************************************************************/
/* VISCAMap                                                 */
/************************************************************/

VISCAMap *get_visca_elem(const char *port)
{
	VISCAMap *elem = NULL;

	if (!map)
	{
		map = create_visca_elem(port);
		return map;
	} else {
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
		fprintf(stderr, "can not create VISCAMap!\n");
		return NULL;
	}

	elem->visca_struct = create_visca_struct();
	if (!elem->visca_struct)
	{
		fprintf(stderr, "can not create VISCAStruct!\n");
		remove_visca_elem(port);
		return NULL;
	}

	elem->interface = malloc(sizeof(VISCAInterface_t));
	if (!elem->interface)
	{
		fprintf(stderr, "can not create VISCAInterface!\n");
		remove_visca_elem(port);
		return NULL;
	}
        
        elem->port = malloc(sizeof(char)*strlen(port));
        strcpy(elem->port, port);
        
	elem->initialized = 0;
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
        
	delete_visca_struct(elem->visca_struct);
	free(elem->interface);
	free(elem->port);
	free(elem);
	return 1;
}

/************************************************************/
/* VISCAStruct                                              */
/************************************************************/

VISCAStruct *create_visca_struct()
{
	int i;
	VISCAStruct *visca_struct = malloc(sizeof(VISCAStruct));
	if (!visca_struct) return NULL;

	for (i = 0; i < 7; i++)
	{
		visca_struct->camera[i] = malloc(sizeof(VISCACamera_t));
		visca_struct->speed[i] = malloc(sizeof(VISCACamSpeed_t));
		visca_struct->min_speed[i] = malloc(sizeof(VISCACamSpeed_t));
		visca_struct->max_speed[i] = malloc(sizeof(VISCACamSpeed_t));
		visca_struct->min_position[i] = malloc(sizeof(VISCAPosition_t));
		visca_struct->max_position[i] = malloc(sizeof(VISCAPosition_t));
	}
	visca_struct->cams_connected = 0;
	return visca_struct;
}

int delete_visca_struct(VISCAStruct *visca_struct) 
{
	int i;

	if (!visca_struct) return 1;
	for (i = 0; i < 7; i++)
	{
		free(visca_struct->camera[i]);
		free(visca_struct->speed[i]);
		free(visca_struct->min_speed[i]);
		free(visca_struct->max_speed[i]);
		free(visca_struct->min_position[i]);
		free(visca_struct->max_position[i]);
	}
	free(visca_struct);
	return 1;
}

#endif /* PTZCamVisca_H */

