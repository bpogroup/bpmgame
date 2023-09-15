package nl.tue.bpmgame.assignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Resource {

	private String name;
	private Set<String> skills;
	private Map<Task,Boolean> isSkilled; //A cache to keep information on which tasks this resource is skilled for
	private boolean perHour;
	private int costPerHour;
	
	protected Resource(String name, String skills[], boolean perHour, int costPerHour) {
		this.name = name;
		this.skills = new HashSet<String>();
		for (String skill: skills) {
			this.skills.add(skill);
		}
		this.isSkilled = new HashMap<Task, Boolean>();
		this.perHour = perHour;
		this.costPerHour = costPerHour;
	}

	public String getName() {
		return name;
	}

	public Set<String> getSkills() {
		return skills;
	}
	
	/**
	 * Returns true if this resource is skilled to execute the given task. 
	 * 
	 * @param Task a task
	 * @return true or false
	 */
	public boolean isSkilled(Task task) {
		Boolean result = isSkilled.get(task);
		if (result == null) {
			result = this.skills.contains(task.getRequiredSkill());
			isSkilled.put(task, result);
		}
		return result;
	}
	
	/**
	 * Returns true if the if the resource is paid only for the time it actually performs tasks.
	 * If false, the resource is assumed to be paid for the full day on which it is scheduled to do work, 
	 * no matter how much work it does during that day.
	 * 
	 * @return true or false
	 */
	public boolean isPerHour() {
		return perHour;
	}

	/**
	 * Returns the amount that the resource gets paid per hour.
	 * 
	 * @return an amount
	 */
	public int getCostPerHour() {
		return costPerHour;
	}
}
