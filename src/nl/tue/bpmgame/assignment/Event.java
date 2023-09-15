package nl.tue.bpmgame.assignment;

public class Event {

	private String label;
	private long minTime, maxTime; //waiting time is specified in minutes, uniformly distributed over [minTime, maxTime]
	
	public Event(String label, long minTime, long maxTime) {
		this.label = label;
		this.minTime = minTime;
		this.maxTime = maxTime;
	}
	
	public String getLabel() {
		return label;
	}

	public long getMinTime() {
		return minTime;
	}

	public long getMaxTime() {
		return maxTime;
	}
}
