#ifndef VISCAPositions_H
#define VISCAPositions_H

#define D30_D31_MODEL 0x402
#define D70_D70P_MODEL 0x40e

int set_default_value(unsigned int model, 
			VISCAPosition_t *min_position, VISCAPosition_t *max_position, 
			VISCACamSpeed_t *min_speed, VISCACamSpeed_t *max_speed, VISCACamSpeed_t *avg_speed) 
{
	unsigned int pan_pos, tilt_pos, zoom_pos, focus_pos;
	uint8_t pan_speed, tilt_speed;
	unsigned int zoom_speed_tele, zoom_speed_wide;

	switch (model) 
	{
		case D30_D31_MODEL:
			/* set min value */
			pan_pos = 0xCA4f;
			tilt_pos = 0xEE7f;
			zoom_pos = 0x0000;
			focus_pos = 0x1000;
			pan_speed = 1;
			tilt_speed = 1;
			zoom_speed_tele = 2;
			zoom_speed_wide = 2;

			min_position->pan = pan_pos;
			min_position->tilt = tilt_pos;
			min_position->zoom = zoom_pos;
			min_speed->pan_speed = pan_speed;
			min_speed->tilt_speed = tilt_speed;
			min_speed->zoom_speed_tele = zoom_speed_tele;
			min_speed->zoom_speed_wide = zoom_speed_wide;

			/* set max value */
			pan_pos = 0x35E0;
			tilt_pos = 0x11AF;
			zoom_pos = 0x03FF;
			focus_pos = 0x9FFF;
			pan_speed = 18;
			tilt_speed = 14;
			zoom_speed_tele = 7;
			zoom_speed_wide = 7;

			max_position->pan = pan_pos;
			max_position->tilt = tilt_pos;
			max_position->zoom = zoom_pos;
			max_speed->pan_speed = pan_speed;
			max_speed->tilt_speed = tilt_speed;
			max_speed->zoom_speed_tele = zoom_speed_tele;
			max_speed->zoom_speed_wide = zoom_speed_wide;

			break;

		case D70_D70P_MODEL:
			/* set min value */
			pan_pos = 0x71DF;
			tilt_pos = 0xE6CF;
			zoom_pos = 0x0000;
			focus_pos = 0x1000;
			pan_speed = 1;
			tilt_speed = 1;
			zoom_speed_tele = 0;
			zoom_speed_wide = 0;

			min_position->pan = pan_pos;
			min_position->tilt = tilt_pos;
			min_position->zoom = zoom_pos;
			min_speed->pan_speed = pan_speed;
			min_speed->tilt_speed = tilt_speed;
			min_speed->zoom_speed_tele = zoom_speed_tele;
			min_speed->zoom_speed_wide = zoom_speed_wide;

			/* set max value */
			pan_pos = 0x8E30;
			tilt_pos = 0x4B0F; //0x195F;
			zoom_pos = 0x7AC0; 
			focus_pos = 0xC000; 
			pan_speed = 18;
			tilt_speed = 17;
			zoom_speed_tele = 7;
			zoom_speed_wide = 7;

			max_position->pan = pan_pos;
			max_position->tilt = tilt_pos;
			max_position->zoom = zoom_pos;
			max_speed->pan_speed = pan_speed;
			max_speed->tilt_speed = tilt_speed;
			max_speed->zoom_speed_tele = zoom_speed_tele;
			max_speed->zoom_speed_wide = zoom_speed_wide;

			break;
		default: return 0;
	}

	avg_speed->pan_speed = (max_speed->pan_speed - min_speed->pan_speed) / 2;
	avg_speed->tilt_speed = (max_speed->tilt_speed - min_speed->tilt_speed) / 2;
	avg_speed->zoom_speed_tele = (max_speed->zoom_speed_tele - min_speed->zoom_speed_tele) / 2;
	avg_speed->zoom_speed_wide = (max_speed->zoom_speed_wide - min_speed->zoom_speed_wide) / 2;

	return 1;
}

#endif /* VISCAPositions_H */
