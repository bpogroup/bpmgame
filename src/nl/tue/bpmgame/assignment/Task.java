package nl.tue.bpmgame.assignment;

public class Task {

	private String label;
	private long minTime, maxTime; //service time is specified in minutes, uniformly distributed over [minTime, maxTime]
	private String requiredSkill;
	private int customerSatisfactionEffect;
	
	public Task(String label, long minTime, long maxTime, String requiredSkill, int customerSatisfactionEffect) {
		this.label = label;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.requiredSkill = requiredSkill;
		this.customerSatisfactionEffect = customerSatisfactionEffect;
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

	public String getRequiredSkill(){
		return requiredSkill;
	}
	
	public int getSustomerSatisfactionEffect() {
		return customerSatisfactionEffect;
	}
}
