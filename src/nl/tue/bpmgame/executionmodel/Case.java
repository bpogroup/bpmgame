package nl.tue.bpmgame.executionmodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import nl.tue.bpmgame.assignment.Assignment;

/**
 *	A case is a customer that will pass through the process. It contains:
 *	- the information about the case.
 *	- the set of possible paths that the customer can take through the process.
 *	- the processing times of the tasks
 *  - the waiting times for the events
 *  - the random skips, redo's and repeats
 *  - the events that can occur in the case (by their label)
 *  - the condition that defines the completion of this case
 *  These values are established by the assignment on instantiation of the case type. 
 *   
 */
public class Case {
	
	private long maximumActiveTime;
	private long startTime;
	private Set<String> occurringEvents;
	private String completionCondition;
	private Map<String, String> data2valueFirstTouch;
	private Map<String, String> data2valueLaterTouches;
	private Map<String, Long> node2time;
	private DeclarativeStateSpace declarativeStateSpace;
	private Set<String> skip;
	private Set<String> redo;
	private Map<String,String> repeat; //repeat all tasks that occur between and including <from, until>
	
	public Case(){
		occurringEvents = new HashSet<String>();
		data2valueFirstTouch = new HashMap<String, String>();		
		data2valueLaterTouches = new HashMap<String, String>();
		node2time = new HashMap<String, Long>();
		completionCondition = "";
		declarativeStateSpace = null;
		
		skip = new HashSet<String>();
		redo = new HashSet<String>();
		repeat = new HashMap<String,String>();
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public long getMaximumActiveTime() {
		return maximumActiveTime;
	}

	public void setMaximumActiveTime(long maximumActiveTime) {
		this.maximumActiveTime = maximumActiveTime;
	}

	public void setCompletionCondition(String completionCondition){
		this.completionCondition = completionCondition;
	}
	
	public String getCompletionCondition(){
		return completionCondition;
	}
	
	public void addOccurringEvent(String eventLabel){
		occurringEvents.add(eventLabel);
	}
	
	public Set<String> getOccurringEvents(State currentState){
		Set<String> result = new HashSet<String>();		
		for (String occurringEvent: occurringEvents) {
			if (declarativeStateSpace.targetState(currentState, occurringEvent) != null) {
				result.add(occurringEvent);
			}
		}
		return result;
	}
	
	public void addTime(String node, long time) {
		node2time.put(node, time);
	}
	
	public long getTime(String node) {
		return node2time.get(node);
	}
	
	public void addSkip(String task) {
		skip.add(task);
	}
	
	public void addRedo(String task) {
		redo.add(task);
	}
	
	public void addRepeat(String from, String until) {
		repeat.put(from, until);
	}
	
	public boolean isSkip(String task) {
		return skip.contains(task);
	}
	
	public boolean isRedo(String task) {
		return redo.contains(task);
	}
	
	public String repeatUntil(String from) {
		return repeat.get(from);
	}
	
	public void removeSkip(String task) {
		skip.remove(task);
	}
	
	public void removeRedo(String task) {
		redo.remove(task);
	}
	
	public void removeRepeat(String from) {
		repeat.remove(from);
	}
		
	/**
	 * Sets the possible values for the data item.
	 * 
	 * @param dataItem the data item
	 * @param valueFirstTouch the value that is returned for touches == 1
	 * @param valueLaterTouches the value that is returned for touches > 1
	 */
	public void addValues(String dataItem, String valueFirstTouch, String valueLaterTouches){
		data2valueFirstTouch.put(dataItem, valueFirstTouch);
		data2valueLaterTouches.put(dataItem, valueLaterTouches);
	}
	
	/**
	 * Returns the value of the given dataitem, given the number of times the dataitem has been touched.
	 * The value can be null, if there is none or if the dataitem is not yet touched.
	 * 
	 * @param dataItem	the dataitem to get the value of
	 * @param touches	the number of times the dataitem has been touched
	 * @return			the value of the dataitem or null if there is no value of no dataitem
	 */
	public String getValue(String dataItem, int touches){
		if (touches == 1){
			return data2valueFirstTouch.get(dataItem);
		}else if (touches > 1){
			return data2valueLaterTouches.get(dataItem);
		}else{
			return null;
		}
	}

	public DeclarativeStateSpace getDeclarativeStateSpace() {		
		if (declarativeStateSpace == null) {
			declarativeStateSpace = Assignment.getAssignment().getBehavior().computeStateSpace(this);
			if (declarativeStateSpace.finalStates().isEmpty()) {
				Logger.getLogger(Case.class).error("ERROR: there is a declarative statespace without final states.");
			}
		}
		return declarativeStateSpace;
	}	
}
