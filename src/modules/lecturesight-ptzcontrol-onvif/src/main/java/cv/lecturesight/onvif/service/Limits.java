package cv.lecturesight.onvif.service;

public class Limits {

	int min;
	int max;

	public Limits(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public int min() {
		return min;
	}

	public int max() {
		return max;
	}

	public boolean in(int i) {
		return (i >= min) && (i <= max);
	}

	public int clamp(int i) {
		if (i < min)
			return min;
		else if (i > max)
			return max;
		else
			return i;
	}

	public static float normalize(float f) {
		return normalize(f, false);
	}
	
	public static float normalize(float f, boolean cut_zero) {
		if (cut_zero && (f < 0)) {
			return 0;
		}
		if (f < -1)
			return -1;
		if (f > 1)
			return 1;
		return f;
	}

	public String toString() { 
		return "("+ min +" -> "+ max+")";		
	}
}