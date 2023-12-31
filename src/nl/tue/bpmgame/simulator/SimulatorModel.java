package nl.tue.bpmgame.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeSpan;
import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.assignment.Resource;
import nl.tue.bpmgame.assignment.Task;
import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmn.concepts.BPMNModel;
import nl.tue.bpmn.concepts.BPMNRole;
import nl.tue.util.RandomGenerator;
import nl.tue.util.TimeUtils;

/**
 * A model that simulates a given BPMN model against the given declarative ideal.
 * The model is initialized with:
 * - a BPMN model (assert: the model is correct, this must be checked earlier while uploading the model)
 * - a DeclarativeModel
 * - a DeclarativeStateSpace that is generated by the DeclarativeModel
 * - a list of case objects that is generated by the CaseGenerator and should be the same for all SimulatorModel objects
 * - the (real) time at which the simulation should start
 * Asides from these things, the model initializes:
 * - a queue for each task
 * - a queue for each skill
 *   (cases are queued for a task, but if a resource for the task does not have the required skills, it will be queued for a skill instead)
 *   (resources will first process cases for which they have the skills, at a penalty, then cases to the tasks of which they are associated)
 * - a resource simulator for each resource in the model
 * - a resource simulator for the 'General Manager'
 */
public class SimulatorModel extends Model{

	private BPMNModel bpmnm;
	private Map<Resource,ResourceSimulator> resource2simulator;
	private Map<String,ProcessQueue<CaseSimulator>> name2queue;
	private Map<String,ContDistUniform> label2time;
	private Map<String,List<ResourceSimulator>> skill2resources;
	private Map<BPMNRole,List<Resource>> role2ResourceCache;
	private List<Case> cases;
	private long startTime;
	private long nextCaseID;
	private List<LogEvent> log;
	
	//KPI's
	private long totalWaitingTime;
	private long totalServiceTime;
	private long totalThroughputTime;
	private double totalVariableCost;
	private long totalCustomerSatisfaction;
	private int nrCases;
	
	/**
	 * Creates a new SimulatorModel.
	 * 
	 * @param bpmnm a BPMN model
	 * @param dm an ideal declarative model
	 * @param dss a declarative state space generated from the declarative model
	 * @param cases a list of cases that will be simulated by the simulation model
	 * @param startTime the time at which the simulation will start in Java time (ms after 1970)
	 */
	public SimulatorModel(BPMNModel bpmnm, List<Case> cases, long startTime) {
		super(null, "", false, false);
		
		this.bpmnm = bpmnm;
		this.cases = cases;
		this.startTime = startTime;
		resource2simulator = new HashMap<Resource,ResourceSimulator>();
		name2queue = new HashMap<String,ProcessQueue<CaseSimulator>>();
		skill2resources = new HashMap<String,List<ResourceSimulator>>();
		role2ResourceCache = new HashMap<BPMNRole,List<Resource>>();
		nextCaseID = startTime;
		log = new ArrayList<LogEvent>();
		
		totalWaitingTime = 0;
		totalServiceTime = 0;
		totalThroughputTime = 0;
		totalVariableCost = 0;
		totalCustomerSatisfaction = 0;
		nrCases = 0;
	}
	
	public String description() {
		return "";
	}

	public void init() {
		//Create a process queue for each task.
		//Map each task label to the corresponding process queue
		for (Task t: Assignment.getAssignment().getTasks()) {
			name2queue.put(t.getLabel(), new ProcessQueue<CaseSimulator>(this, t.getLabel(), false, false));
		}
		//Create a process queue for each skill.
		//Map each skill to the corresponding process queue
		for (String s: Assignment.getAssignment().getSkills()) {
			name2queue.put(s, new ProcessQueue<CaseSimulator>(this, s, false, false));
		}
	}
	
	public void doInitialSchedules() {
		//Create a resource simulator for each resource.
		//Map each resource to the created simulation.
		boolean generalManagerCreated = false;
		for (BPMNRole ro: bpmnm.getRoles()) {
			for (Resource r: getResources(ro)) {
				if (resource2simulator.get(r) == null) {
					resource2simulator.put(r, new ResourceSimulator(this, r.getName(), ro));
					if (r.getName().equals(Assignment.GENERAL_MANAGER)) { generalManagerCreated = true; }
				} else {
					resource2simulator.get(r).addRole(ro);
				}
			}
		}
		//Create a resource simulator for the general manager, if this is not already done
		if (!generalManagerCreated) {
			Resource generalManager = Assignment.getAssignment().getResource(Assignment.GENERAL_MANAGER);
			resource2simulator.put(generalManager, new ResourceSimulator(this, generalManager.getName(), null));
		}
		//Create a mapping of skills to the resources that have them
		for (ResourceSimulator resource: resource2simulator.values()) {
			for (String skill: resource.getResource().getSkills()) {
				List<ResourceSimulator> resourcesWithSkill = skill2resources.get(skill);
				if (resourcesWithSkill == null) {
					resourcesWithSkill = new ArrayList<ResourceSimulator>();
					skill2resources.put(skill, resourcesWithSkill);
				}
				resourcesWithSkill.add(resource);
			}			
		}
		//Initialize the case generator
		SimulatorModelCaseGenerator cg = new SimulatorModelCaseGenerator(this, cases, startTime);
		cg.activate();			
	}

	/**
	 * Selects the first resource (from the ordered list of resources) that:
	 * 1. is not busy
	 * 2. can act in the specified role
	 * Returns null if no such resource is available
	 * 
	 * @param r a role
	 * @return a resource simulator
	 */
	public ResourceSimulator getResourceFor(BPMNRole ro) {
		for (Resource r: getResources(ro)) {
			ResourceSimulator rs = resource2simulator.get(r);
			if (!rs.isBusy()) {
				return rs;
			}
		}
		return null;
	}
	
	/**
	 * Randomly a resource that:
	 * 1. is not busy
	 * 2. has the specified skill
	 * Returns null if no such resource is available
	 * 
	 * @param skill a skill
	 * @return a resource simulator
	 */	
	public ResourceSimulator getResourceFor(String skill) {
		List<ResourceSimulator> resources = new ArrayList<ResourceSimulator>();
		resources.addAll(skill2resources.get(skill));
		while (!resources.isEmpty()) {
			//Randomly select a resource
			int i = (int) RandomGenerator.generateUniform(resources.size());
			ResourceSimulator resource = resources.get(i);
			resources.remove(i);
			//Return the resource if it is not busy
			if (!resource.isBusy()) {
				return resource;
			}
		}		
		return null;				
	}
		
	/**
	 * Returns the queue that belongs to the task or skill with the given name. 
	 * 
	 * @param name a task or skill name
	 * @return a queue
	 */
	public ProcessQueue<CaseSimulator> queueFor(String name){
		return name2queue.get(name);
	}
		
	/**
	 * Returns a sample of the time associated with the task or event with the given label.
	 * If the node with the given label is a task, the time sample is service time.
	 * If the node with the given label is an event, the time sample is waiting time.
	 * 
	 * @param nodeLabel a task or event label
	 * @return a timesample
	 */
	public TimeSpan serviceTimeSample(String nodeLabel) {
		return label2time.get(nodeLabel).sampleTimeSpan();
	}

	protected BPMNModel getBPMNModel() {
		return bpmnm;
	}
	
	/**
	 * Returns the next case identifier and increased it.
	 */
	protected long getNextCaseID() {
		return nextCaseID++;
	}
	
	protected void addLogEvent(LogEvent logEvent) {
		log.add(logEvent);
	}
	
	public List<LogEvent> getLog() {
		return log;
	}
	
	private List<Resource> getResources(BPMNRole forRole) {
		List<Resource> resources = role2ResourceCache.get(forRole);
		if (resources == null) {
			resources = new ArrayList<Resource>();
			for (String resourceLabel: forRole.getResources()) {
				resources.add(Assignment.getAssignment().getResource(resourceLabel));
			}
			role2ResourceCache.put(forRole, resources);
		}
		return resources;
	}

	public long getSimulationTime() {
		return presentTime().getTimeRounded(TimeUnit.MILLISECONDS) + startTime;
	}

	protected void storeKPIs(long waitingTime, long serviceTime, long throughputTime, double variableCost, long customerSatisfaction) {
		totalWaitingTime += waitingTime;
		totalServiceTime += serviceTime;
		totalThroughputTime += throughputTime;
		totalVariableCost += variableCost;
		totalCustomerSatisfaction += customerSatisfaction;
		nrCases ++;	
	}
	
	public double avgWaitingTime() {
		double result = ((double) totalWaitingTime)/((double) nrCases);
		return Double.isNaN(result)?Assignment.getAssignment().getWorkdayDuration():result;
	}
	
	public double avgServiceTime() {
		double result = ((double) totalServiceTime)/((double) nrCases);		
		return Double.isNaN(result)?Assignment.getAssignment().getWorkdayDuration():result;
	}
	
	public double avgThroughputTime() {
		double result = ((double) totalThroughputTime)/((double) nrCases);		
		return Double.isNaN(result)?Assignment.getAssignment().getWorkdayDuration():result;
	}
	
	public double avgCustomerSatisfaction() {
		double result = ((double) totalCustomerSatisfaction)/((double) nrCases);		
		return Double.isNaN(result)?Assignment.CUSTOMER_SATISFACTION_MIN:result;
	}
	
	public double totalCost() {
		double totalCost = totalVariableCost;
		for (Resource r: resource2simulator.keySet()) {
			if (!r.isPerHour()) {
				totalCost += TimeUtils.simulationTimeInHours(Assignment.getAssignment().getWorkdayDuration()) * r.getCostPerHour();
			}
		}
		return Double.isNaN(totalCost)?Assignment.TOTAL_COST_MAX:totalCost;
	}
	
	public int nrCases() {
		return nrCases;
	}
 }