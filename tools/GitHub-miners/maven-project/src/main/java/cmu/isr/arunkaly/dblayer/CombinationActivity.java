package cmu.isr.arunkaly.dblayer;

public class CombinationActivity {

	String name;
	public String getName() {
		return name;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	int min,max;
	
	public CombinationActivity(String name, int min, int max) {
		this.name = name;
		this.min = min;
		this.max = max;
	}

	public boolean matches(int i) {
		if ((min == -1 || i >= min) && (max == -1 || i <= max))
			return true;
		return false;
	}
	
}
