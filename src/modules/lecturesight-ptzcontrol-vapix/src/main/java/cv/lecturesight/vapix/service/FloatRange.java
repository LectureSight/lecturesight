package cv.lecturesight.vapix.service;

import java.util.Properties;

/**
 * Range of Float values
 */
public class FloatRange {

	protected float min;
	protected float max;

	public FloatRange() {
		min = 0f;
		max = 0f;
	}
	
	public FloatRange(float val) {
		min = val;
		max = val;
	}
	
	public FloatRange(float _min, float _max) {
		min = _min;
		max = _max;
	}
	
	/**
	 * Return min value
	 * 
	 */
	public float getMin() {
		return min;
	}

	/**
	 * Set the min value
	 * 
	 */
	public void setMin(float value) {
		this.min = value;
	}

	/**
	 * Return max
	 * 
	 */
	public float getMax() {
		return max;
	}

	/**
	 * Set the max value
	 * 
	 */
	public void setMax(float value) {
		this.max = value;
	}

	public String toStrin() {
		return "(("+ min +" -> "+ max+"))";		
	}
}
