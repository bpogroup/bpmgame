package nl.tue.bpmgame.assignment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import nl.tue.bpmgame.declarative.DeclarativeModel;
import nl.tue.bpmn.parser.DistributionEvaluator;

/**
 *	The assignment that defines the way in which cases can be executed in the process.
 *	This class is abstract, it must be subclassed and the subclass must implement
 *	the createAssignment method, which actually creates the assignment, by defining:
 *  - the tasks and events that can happen
 *  - the processing times of the tasks and the waiting times for the events
 *  - the effect of certain tasks on customer satisfaction
 *  - the skills required to execute a task
 *  - the resources that exist, the costs of these resources and their skills
 *  - a declarative model that defines the conditions for each task or event
 *  - the case types, along with their interarrival times, the data they produce, their completion conditions, their maximum active times, their occurring events, their completion condition 
 *  - the insert, delete and swap probabilities 
 */
public abstract class Assignment {
	
	//The factor for which each KPI counts in the overall score.
	//Customer satisfaction should be high, because it is also used to punish incomplete executions, and
	//incomplete executions could have really low waiting/service time and cost.	
	public static int CUSTOMER_SATISFACTION_MAX = 10;
	public static int CUSTOMER_SATISFACTION_MIN = 0;	

	public static final double TOTAL_COST_MAX = 480;
	public static final String ACTIVE_ASSIGNMENT = "nl.tue.bpmgame.assignment.AssignmentLoan";

	public static String GENERAL_MANAGER = "General Manager";
	public static String INFORMATION_SYSTEM = "Information System";
	public static String SYSTEM_SKILL = "system";	
	
	private long WORKDAY_START = 30600000l; //8:30
	private long WORKDAY_DURATION = 28800000l; //8:00
	private long WORKDAY_GENERATION_DURATION = 27000000l; //7:30
	
	private Set<Task> tasks;
	private Set<Event> events;
	private Set<Resource> resources;
	private Set<CaseType> caseTypes;
	private Set<String> skills;
	private Map<String,List<String>> dataItems;

	private Set<String> eventLabels; 
	private Set<String> taskLabels;
	
	private Map<String, Task> label2Task;
	private Map<String, Event> label2Event;
	private Map<String, Resource> name2Resource;
	
	protected DeclarativeModel behavior;
	
	private static Assignment singleton;
	
	protected Assignment() {
		tasks = new HashSet<Task>();
		events = new HashSet<Event>();
		resources = new HashSet<Resource>();
		caseTypes = new HashSet<CaseType>();
		skills = new HashSet<String>(); 
		dataItems = new HashMap<String,List<String>>();
				
		createAssignment();
		
		label2Task = new HashMap<String,Task>();
		label2Event = new HashMap<String,Event>();
		name2Resource = new HashMap<String,Resource>();
		eventLabels = new HashSet<String>();
		taskLabels = new HashSet<String>();
		for (Task t: tasks) {
			taskLabels.add(t.getLabel());
			label2Task.put(t.getLabel(), t);
		}
		for (Event e: events) {
			eventLabels.add(e.getLabel());
			label2Event.put(e.getLabel(), e);
		}
		for (Resource r: resources) {
			name2Resource.put(r.getName(), r);
		}

		checkAssignment();
	}
	
	private void checkAssignment() {
		Logger logger = Logger.getLogger(Assignment.class);
		
		List<String> assignmentErrors = new ArrayList<String>();
		
		Resource generalManager = name2Resource.get(GENERAL_MANAGER);		
		Resource informationSystem = name2Resource.get(INFORMATION_SYSTEM);
		if (informationSystem != null) {
			if ((informationSystem.getSkills().size() != 1) || (!informationSystem.getSkills().contains(SYSTEM_SKILL))) {
				assignmentErrors.add("The information system must have the system skill and only the system skill.");
			}
			if (generalManager.getSkills().size() != skills.size() - 1) {
				assignmentErrors.add("The general manager must have all possible skills except for the information system skill.");
			}
		} else {
			if (!generalManager.getSkills().containsAll(skills)) {
				assignmentErrors.add("The general manager must have all possible skills.");
			}
		}
		Set<String> usedSkills = new HashSet<String>();
		for (Task t: tasks) {
			usedSkills.add(t.getRequiredSkill());
		}
		if (!skills.containsAll(usedSkills)) {
			assignmentErrors.add("Not all required skills are defined.");
		}
		Set<String> providedSkills = new HashSet<String>();
		for (Resource r: resources) {
			providedSkills.addAll(r.getSkills());
		}
		if (!skills.containsAll(providedSkills)) {
			assignmentErrors.add("Not all defined skills are provided.");
		}
		for (String skill: skills) {			
			if (taskLabels.contains(skill)) {
				assignmentErrors.add("Task and skill labels must not overlap.");			
			}
		}
		for (CaseType ct: caseTypes) {
			for (Map.Entry<String, String> attribute: ct.getAttributes().entrySet()) {				
				List<String> errors = DistributionEvaluator.getInstance().validate(attribute.getValue());
				if (!errors.isEmpty()) {
					String errorText = "The distribution of attribute " + attribute.getKey() + " contains the following errors: ";
					for (String error: errors) {
						errorText += error + " ";
					}
					assignmentErrors.add(errorText);
				}
			}
		}
		
		for (String elt: behavior.getDefinedTransitions()) {
			if ((label2Task.get(elt) == null) && (label2Event.get(elt) == null)) {
				assignmentErrors.add("Declarative behavior has condition for undefined element " + elt);
			}
		}

		for (String elt: behavior.getDefinedTransitionTouching()) {
			if ((label2Task.get(elt) == null) && (label2Event.get(elt) == null)) {
				assignmentErrors.add("Declarative behavior has undefined data element toucher " + elt);
			}
		}
		
		for (CaseType ct: caseTypes) {
			for (Object skips[]: ct.getSkipElephantTrails()) {
				@SuppressWarnings("unchecked")
				Set<String> skippedElements = (Set<String>) skips[1];
				for (String elt: skippedElements) {
					if ((label2Task.get(elt) == null) && (label2Event.get(elt) == null)) {
						assignmentErrors.add("There is an undefined skipped element " + elt);
					}					
				}
			}
			for (Object redos[]: ct.getRedoElephantTrails()) {
				@SuppressWarnings("unchecked")
				Set<String> redoElements = (Set<String>) redos[1];
				for (String elt: redoElements) {
					if ((label2Task.get(elt) == null) && (label2Event.get(elt) == null)) {
						assignmentErrors.add("There is an undefined redo element " + elt);
					}					
				}
			}
			for (Object repeats[]: ct.getRepeatElephantTrails()) {
				if ((label2Task.get((String)repeats[1]) == null) && (label2Event.get((String)repeats[1]) == null)) {
					assignmentErrors.add("There is an undefined repeat element " + (String)repeats[1]);
				}					
				if ((label2Task.get((String)repeats[2]) == null) && (label2Event.get((String)repeats[2]) == null)) {
					assignmentErrors.add("There is an undefined repeat element " + (String)repeats[2]);
				}					
			}
		}

		if (!assignmentErrors.isEmpty()) {
			for (String anError: assignmentErrors) {
				logger.error(anError);
			}
			System.exit(1);
		}
	}

	protected abstract void createAssignment();
	
	public static Assignment getAssignment() {
		return singleton;
	}
	
	public static void initializeAssignment(String assignmentClassName) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		Class<?> assignmentClass = Class.forName(assignmentClassName);
		Constructor<?> assignmentConstructor = assignmentClass.getConstructor(new Class[] {});		
		singleton = (Assignment) assignmentConstructor.newInstance(new Object[] {});
	}
	
	protected void addBehavior(DeclarativeModel behavior) {
		this.behavior = behavior;
	}
	
	public DeclarativeModel getBehavior() {
		return behavior;
	}
	
	protected void addTask(Task t) {
		tasks.add(t);
	}
	
	protected void addEvent(Event e) {
		events.add(e);
	}
	
	protected void addResource(Resource r) {
		resources.add(r);
	}
	
	protected void addCaseType(CaseType ct) {
		caseTypes.add(ct);
	}

	public Set<Task> getTasks() {
		return tasks;
	}

	public Set<Event> getEvents() {
		return events;
	}

	public Set<Resource> getResources() {
		return resources;
	}	
	
	public Set<CaseType> getCaseTypes() {
		return caseTypes;
	}
	
	public void addDataItem(String dataItem, String[] values) {
		dataItems.put(dataItem, new ArrayList<String>(Arrays.asList(values)));
	}
	
	public Set<String> getDataItems() {
		return dataItems.keySet();
	}
	
	/**
	 * Returns true iff the data item exists.
	 * 
	 * @param dataItem a data item
	 * @return true or false
	 */
	public boolean checkDataItemExists(String dataItem) {
		List<String> values = dataItems.get(dataItem);
		return values != null;
	}
	
	/**
	 * Returns true if the data item exists and the value is defined for that data item.
	 * @param dataItem a data item
	 * @param value a data value
	 * @return true or false
	 */
	public boolean checkDataValueExists(String dataItem, String value) {
		List<String> values = dataItems.get(dataItem);
		if (values == null) {
			return false;
		} else {
			return values.contains(value);
		}		
	}
	
	public boolean isEvent(String label) {
		return eventLabels.contains(label);
	}
	
	public boolean isTask(String label) {
		return taskLabels.contains(label);
	}
	
	public Task getTask(String taskLabel) {
		return label2Task.get(taskLabel);
	}
	
	public Event getEvent(String eventLabel) {
		return label2Event.get(eventLabel);
	}
	
	public Resource getResource(String resourceName) {
		return name2Resource.get(resourceName);
	}
	
	public Set<String> getSkills(){
		return skills;
	}
	
	public void addSkill(String skill) {
		skills.add(skill);
	}
	
	/**
	 * 
	 * @return the hour (after 00:00:00) at which a workday starts.
	 */
	public long getWorkdayStart() {
		return this.WORKDAY_START;
	}
	
	/**
	 * 
	 * @return the number of hours a workday takes
	 */
	public long getWorkdayDuration() {
		return this.WORKDAY_DURATION;
	}
	
	/**
	 * 
	 * @return the number of hours of a workday new cases are generated
	 */
	public long getGenerationDuration() {
		return this.WORKDAY_GENERATION_DURATION;
	}

	/**
	 * Returns a sample of penalty time.
	 * 
	 * @return penalty time in milliseconds
	 */
	public abstract long getPenaltyTime();
	
	/**
	 * Samples if a skipped node should be executed or not.
	 * 
	 * @return true or false
	 */
	public abstract boolean executeSkip();
	
	/**
	 * Samples if an unskilled resource should perform a task anyway
	 * 
	 * @return true or false
	 */
	public abstract boolean performUnskilled();
	
	/**
	 * Samples a penalty to the customer satisfaction
	 * 
	 * @return a penalty (positive integer)
	 */
	public abstract int getPenaltySatisfaction();

}
