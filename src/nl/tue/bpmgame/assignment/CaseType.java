package nl.tue.bpmgame.assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmn.parser.DistributionEvaluator;
import nl.tue.util.RandomGenerator;

/**
 * A case type describes a generator for cases of a particular type. Each case type has:
 * - an interarrival times (uniformly distributed)
 * - data it produces
 * - a completion conditions
 * - a maximum active time
 * - a set of events that occur for it 
 * It is possible to generateCase from a CaseType
 */
public class CaseType {

	private String name;
	private long interarrivalTimeMin, interarrivalTimeMax;
	private Map<String,String> attributes; 
	private String completionCondition;
	private long maximumActiveTime;
	private Set<String> occurringEvents;
	private List<Object[]> skipElephantTrails; //Each item in the list is [p,Set<String>], where p is the probability that items from the Set<String> are skipped
	private List<Object[]> redoElephantTrails; //Each item in the list is [p,Set<String>], where p is the probability that items from the Set<String> are redone
	private List<Object[]> repeatElephantTrails; //Each item in the list is [p,String,String], where p is the probability that tasks that occur between and including [String,String] are repeated	
	
	protected List<Object[]> getSkipElephantTrails() {
		return skipElephantTrails;
	}

	protected List<Object[]> getRedoElephantTrails() {
		return redoElephantTrails;
	}

	protected List<Object[]> getRepeatElephantTrails() {
		return repeatElephantTrails;
	}

	public CaseType(long interarrivalTimeMin, long interarrivalTimeMax, String completionCondition, long maximumActiveTime, String occurringEvents[]) {
		this.interarrivalTimeMin = interarrivalTimeMin;
		this.interarrivalTimeMax = interarrivalTimeMax;
		this.attributes = new HashMap<String,String>();
		this.completionCondition = completionCondition;
		this.maximumActiveTime = maximumActiveTime; 
		this.occurringEvents = new HashSet<String>();
		for (String occurringEvent: occurringEvents) {
			this.occurringEvents.add(occurringEvent);
		}
		skipElephantTrails = new ArrayList<Object[]>();
		redoElephantTrails = new ArrayList<Object[]>();
		repeatElephantTrails = new ArrayList<Object[]>();	
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 	 * Generates a Case object from this case type.
	 * The Case object has:
	 * - an arrival time, which is the sum of the previous arrival time and a sample from the interarrival time distribution
	 * - attributes with attribute values generated according to the CaseType's attribute distributions 
	 * - a completion condition, which is a clone of the CaseType's completion condition
	 * - a maximum active time, which is a clone of the CaseType's maximum active time
	 * - a set of occurring events, which is a clone of the CaseType's occurring events
	 * - the processing times of tasks and the waiting times for events, taken as a sample from the corresponding distributions
	 * 
	 * @param previousArrivalTime the arrival time of the previous case of this type
	 * @return a Case
	 */
	@SuppressWarnings("unchecked")
	public Case generateCase(long previousArrivalTime) {
		Case result = new Case();
		long interarrivalTimeSample = interarrivalTimeMin + RandomGenerator.generateUniform(interarrivalTimeMax-interarrivalTimeMin);
		result.setStartTime(previousArrivalTime + interarrivalTimeSample);
		result.setMaximumActiveTime(maximumActiveTime);
		result.setCompletionCondition(completionCondition);
		for (String occurringEvent: occurringEvents) {
			result.addOccurringEvent(occurringEvent);
		}
		for (Map.Entry<String, String> attribute: attributes.entrySet()) {
			DistributionEvaluator.getInstance().evaluate(attribute.getValue());
			result.addValues(attribute.getKey(), DistributionEvaluator.getInstance().getValueFirstTime(), DistributionEvaluator.getInstance().getValueSecondTime());
		}
		for (Task task: Assignment.getAssignment().getTasks()) {
			long processingTimeSample = task.getMinTime() + RandomGenerator.generateUniform(task.getMaxTime() - task.getMinTime());
			result.addTime(task.getLabel(), processingTimeSample);
		}
		for (Event event: Assignment.getAssignment().getEvents()) {
			long waitingTimeSample = event.getMinTime() + RandomGenerator.generateUniform(event.getMaxTime() - event.getMinTime());
			result.addTime(event.getLabel(), waitingTimeSample);
		}
		for (Object[] skipElephantTrail: skipElephantTrails) {
			if (RandomGenerator.generateUniform(100) < ((Integer)skipElephantTrail[0])) {
				for (String task: (Set<String>) skipElephantTrail[1]) {
					result.addSkip(task);
				}
			}
		}
		for (Object[] redoElephantTrail: redoElephantTrails) {
			if (RandomGenerator.generateUniform(100) < ((Integer)redoElephantTrail[0])) {
				for (String task: (Set<String>) redoElephantTrail[1]) {
					result.addRedo(task);
				}
			}
		}
		for (Object[] repeatElephantTrail: repeatElephantTrails) {
			if (RandomGenerator.generateUniform(100) < ((Integer)repeatElephantTrail[0])) {
				result.addRepeat((String)repeatElephantTrail[1], (String)repeatElephantTrail[2]);;
			}
		}
		return result;
	}
	
	public long getInterarrivalTimeMin() {
		return interarrivalTimeMin;
	}

	public long getInterarrivalTimeMax() {
		return interarrivalTimeMax;
	}
	
	public String getCompletionCondition() {
		return completionCondition;
	}

	public long getMaximumActiveTime() {
		return maximumActiveTime;
	}

	public Set<String> getOccurringEvents() {
		return occurringEvents;
	}
	
	public void addAttribute(String attribute, String distribution) {
		attributes.put(attribute, distribution);
	}

	public boolean isOccurringEvent(String label) {
		return occurringEvents.contains(label);
	}
	
	public void addSkipElephantTrail(Integer probability, Set<String> tasksToSkip) {
		Object skipElephantTrail[] = {probability, tasksToSkip};
		skipElephantTrails.add(skipElephantTrail);
	}

	public void addRedoElephantTrail(Integer probability, Set<String> tasksToRedo) {
		Object redoElephantTrail[] = {probability, tasksToRedo};
		redoElephantTrails.add(redoElephantTrail);		
	}

	public void addRepeatElephantTrail(Integer probability, String repeatFrom, String repeatUntil) {
		Object repeatElephantTrail[] = {probability, repeatFrom, repeatUntil};
		repeatElephantTrails.add(repeatElephantTrail);				
	}
	
	protected Map<String,String> getAttributes() {
		return this.attributes;
	}
}
