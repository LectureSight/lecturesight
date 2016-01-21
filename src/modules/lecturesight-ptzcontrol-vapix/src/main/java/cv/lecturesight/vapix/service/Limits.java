package cv.lecturesight.vapix.service;

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
	
	public float clamp(float i) {
		if (i < min)
			return (float) min;
		else if (i > max)
			return (float) max;
		else
			return i;
	}

	public static float normalize(float f) {
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
