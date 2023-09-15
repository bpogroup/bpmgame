package nl.tue.bpmgame.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.SimProcess;
import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.assignment.Task;
import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmgame.executionmodel.CaseExecution;
import nl.tue.bpmgame.executionmodel.CaseExecution.Fix;
import nl.tue.bpmn.concepts.BPMNRole;
import nl.tue.util.TimeUtils;

/**
 * Simulates a case for a particular BPMN model in an assignment with a particular declarative statespace.
 * Has:
 * - activeNodes: tasks in a queue for processing and events with an eventHolder for awaiting occurrence 
 * - executableNodes: nodes that are enabled (including activeNodes)
 * - skill2deferredTasks: activeNodes that were in a task queue, but could not be executed by a resource due to lack of skill, then deferred to skill queue 
 * 
 * Behavior:
 * 0. determine the initially executableNodes
 * 1. for all executableNodes:
 *   a. if the node is skipped in the declarative model, skip it with a probability
 *   b. if the node is an event, add it to activeNodes, activate waiting for its occurrence, if it is recovered in the declarative model add a penalty
 *   c. if the node is a task, add it to activeNodes, put it in the corresponding queue, activate a resource that is associated with that queue
 * 2. if there are activeNodes or executableNodes, sleep to wait for their completion
 * Repeat 1-2
 *
 * Other functions:
 * - startNodeExecution:
 *   invoked when a resource starts executing a task, returns a sample duration of the task + penalties for recovery and wrong skill if any
 * - completeNodeExecution:
 *   invoked when a resource completes executing a task or event, removed the node from activeNodes, recalculates executableNodes
 */
public class CaseSimulator extends SimProcess{

	private SimulatorModel model;
	private CaseExecution execution;
	private Case c;
	private Set<String> activeNodes; //labels of the nodes that waiting in queue to be executed or are being executed (subset of executableNodes)
	private Set<String> executingNodes; //labels of the nodes that are being executed (subset of activeNodes)
	private List<String> executableNodes; //labels of nodes that can be executed
	private List<Fix> executionFixes; //fixes needed before executing executableNodes
	private Map<String,List<String>> skill2deferredTasks; //for each skill activeNodes that have been deferred to it
	private long caseID;
	private Set<ResourceSimulator> reservedResources; //resources that have been reserved to execute tasks for this case
	private Set<ProcessQueue<CaseSimulator>> queuedFor; //queues that contain this case
	
	//KPI's are being kept per case and are stored in the model upon completion of the case
	private long customerSatisfaction; //between Assignment.CUSTOMER_SATISFACTION_MIN and Assignment.CUSTOMER_SATISFACTION_MAX 
	private long serviceTime; //in ms
	private long waitingTime; //in ms
	private long lastCompletionTime; //in ms, the completion time of the last task (= caseStartTime, initially), used to compute waiting time
	private long caseStartTime; //in ms simulation time, used to compute the throughput time
	private double variableCost; //the costs incurred by using a time-based resource

	public CaseSimulator(SimulatorModel model, Case c) {
		super(model, "", false);
		this.model = model;
		this.execution = new CaseExecution(model.getBPMNModel(), c);
		this.c = c;
		activeNodes = new HashSet<String>();
		executingNodes = new HashSet<String>();
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		skill2deferredTasks = new HashMap<String,List<String>>();
		caseID = model.getNextCaseID();
		reservedResources = new HashSet<ResourceSimulator>();
		queuedFor = new HashSet<ProcessQueue<CaseSimulator>>();
		
		customerSatisfaction = Assignment.CUSTOMER_SATISFACTION_MAX;
		serviceTime = 0;
		waitingTime = 0;
		variableCost = 0;		
	}

	public void lifeCycle() throws SuspendExecution {
		caseStartTime = model.getSimulationTime();
		lastCompletionTime = caseStartTime;
		
		boolean maximumActiveTimeExceeded = false;
		while ((!executableNodes.isEmpty() || !activeNodes.isEmpty()) && !maximumActiveTimeExceeded) {
			//Iterate over the enabled nodes
			for (int i = 0; i < executableNodes.size(); i++) {
				String label = executableNodes.get(i);
				//If the node is not already being executed && If the node will not be skipped
				boolean skipNode = (executionFixes.get(i) == Fix.TaskSkip) && !Assignment.getAssignment().executeSkip();
				if (!activeNodes.contains(label) && !skipNode) {
					//If the node is an event
					if (Assignment.getAssignment().isEvent(label)) {
						//Add it to the active nodes
						activeNodes.add(label);
						//Hold for the time required by the node
						//If the node is recovered, execute it with a penalty
						long penalty = 0;
						if (executionFixes.get(i) == Fix.TaskRecovery) {
							penalty = Assignment.getAssignment().getPenaltyTime();
						}
						EventHolder eh = new EventHolder(model, this, label, c.getTime(label) + penalty);
						eh.activate();
					} else {
					//If the node is a task (we assume that labels are proper, i.e. if it is not an event, it is a task)
						//Add it to the active nodes
						activeNodes.add(label);
						//Get a resource that can act in the role that is associated with the task and that is not busy
						BPMNRole role = model.getBPMNModel().getRoleFor(model.getBPMNModel().getNode(label));
						ResourceSimulator resource = null;
						//if there is a role
						if (role != null) {
							//Add the task it to the appropriate queue
							ProcessQueue<CaseSimulator> q = model.queueFor(label);
							q.insert(this);
							isQueuedFor(q, true);
							//Set the resource 						
							resource = model.getResourceFor(role);
						} else {
						//if there is no role, the task is a recovery
							//Add the task to a skill queue instead
							String requiredSkill = Assignment.getAssignment().getTask(label).getRequiredSkill();
							ProcessQueue<CaseSimulator> q = model.queueFor(requiredSkill);
							q.insert(this);
							isQueuedFor(q, true);
							deferToSkill(label, requiredSkill);
							//find a resource with the appropriate skills that is not busy and wake it up (if it exists)
							resource = model.getResourceFor(requiredSkill);
						}
						//Activate the resource
						if (resource != null) {
							reserveResource(resource);
							resource.isBusy(true);
							resource.activate();		
						}
					}
				}
				//if the node must be skipped, do so
				if (skipNode) {
					execution.skip(label);
					executableNodes = execution.nextExecution();
					executionFixes = execution.fixForNextExecution();
				}
			}
			
			//If there are activeNodes: wait until one of them completes
			if (!activeNodes.isEmpty()) {
				passivate();
			}
			
			maximumActiveTimeExceeded = (model.getSimulationTime() - caseStartTime) > c.getMaximumActiveTime();
		}
		
		//The case is over: process the KPI's
		long throughputTime = model.getSimulationTime() - caseStartTime;
		if ((execution.fixForNextExecution().size() > 0) && (execution.fixForNextExecution().get(0) == Fix.PrematureCompletion)) {
			//The case terminated unexpectedly
			throughputTime = c.getMaximumActiveTime();
			waitingTime = c.getMaximumActiveTime();
			serviceTime = c.getMaximumActiveTime();
			customerSatisfaction = Assignment.CUSTOMER_SATISFACTION_MIN;
			Logger.getLogger(CaseSimulator.class).warn("WARNING: An unexpected termination occurred. This is untested.");
		} else if (maximumActiveTimeExceeded){
			customerSatisfaction = Assignment.CUSTOMER_SATISFACTION_MIN;
			releaseReservedResources();
			unqueue();
		}
		model.storeKPIs(waitingTime, serviceTime, throughputTime, variableCost, customerSatisfaction);
	}
	
	/**
	 * Starts the execution of the node with the given label.
	 * Returns the time that the execution will take. 
	 * This time is randomly sampled from the node's duration and
	 * includes the penalty, if any.
	 * 
	 * @param nodeLabel the label of the node to start executing
	 * @param resource the ResourceSimulator that will execute the task
	 * @param isSkillExecution true if the node is executed because it was in a skill queue (in which case its gets a penalty) 
	 * @return the duration of the task in ms 
	 */
	public long startNodeExecution(String nodeLabel, ResourceSimulator resourceSimulator, boolean isSkillExecution) {
		
		//Process KPI's
		if (executingNodes.isEmpty()) {
			//If nothing was processing, add the time between now and the last execution to the waiting time
			waitingTime += (model.getSimulationTime() - lastCompletionTime);
		}
		
		executingNodes.add(nodeLabel);
		Task task = Assignment.getAssignment().getTask(nodeLabel);
		int i = executableNodes.indexOf(nodeLabel);
		long penalty = 0;
		if (i == -1) {
			//The task was probably put in a list of tasks that must be started. Then another task was completed that made this task not executable anymore.
			//Basically, this means that parallelism is incorrect in this model, so penalize the model.
			penalty = Assignment.getAssignment().getPenaltyTime();
		}else if (executionFixes.get(i) == Fix.TaskRecovery) {
			penalty = Assignment.getAssignment().getPenaltyTime();
		}
		if (!resourceSimulator.getResource().isSkilled(task) || isSkillExecution) {
			penalty += Assignment.getAssignment().getPenaltyTime();
		}
		long executionTime = c.getTime(nodeLabel);
		return executionTime + penalty;
	}
	
	/**
	 * Completes the execution of the node with the given label.
	 * The completion removes the label from the active node labels,
	 * computes the new state of the execution model and
	 * sets the nodes that are executable in the new state,
	 * along with their necessary fixes.
	 * Stores the completion as a log event. 
	 * 
	 * @param nodeLabel a task or event label
	 * @param startTime the time at which the node started execution (same as completion time in case of an event)
	 * @param completionTime the time at which the node completed execution
	 * @param resourceSimulator the resource that executed the node (null in case of an event)
	 * @param isSkilled true if the resource executing the task is skilled
	 */
	public void completeNodeExecution(String nodeLabel, long startTime, long completionTime, ResourceSimulator resourceSimulator, boolean isSkilled) {
		//remove from active nodes.
		activeNodes.remove(nodeLabel);
		executingNodes.remove(nodeLabel);
				
		//Process the KPI's
		boolean isTask = Assignment.getAssignment().isTask(nodeLabel); 
		//Reduce customer satisfaction if applicable, make sure it is not reduced below the minimum
		customerSatisfaction -= (isSkilled)?0:Assignment.getAssignment().getPenaltySatisfaction();
		if (isTask) {
			Task task = Assignment.getAssignment().getTask(nodeLabel);
			customerSatisfaction += task.getSustomerSatisfactionEffect();
		}
		customerSatisfaction = (customerSatisfaction < Assignment.CUSTOMER_SATISFACTION_MIN)?Assignment.CUSTOMER_SATISFACTION_MIN:customerSatisfaction;
		customerSatisfaction = (customerSatisfaction > Assignment.CUSTOMER_SATISFACTION_MAX)?Assignment.CUSTOMER_SATISFACTION_MAX:customerSatisfaction;
		
		if (isTask) {
			long taskServiceTime = completionTime - startTime;
			serviceTime += taskServiceTime; 
			if ((resourceSimulator != null) && (resourceSimulator.getResource().isPerHour())) {
				variableCost += TimeUtils.simulationTimeInHours(taskServiceTime) * resourceSimulator.getResource().getCostPerHour(); 
			}
		}
		lastCompletionTime = completionTime;
		//Done processing the KPI's
		
		//Do a state transition on the executable model.
		int i = executableNodes.indexOf(nodeLabel);
		//Check i >= 0. If 1 == -1, this is because the task started in parallel with another task. 
		//              When the other task completed, it recalculated the executable nodes and this task was no longer necessary. 
		if (i >= 0) {
			switch (executionFixes.get(i)) {
			case None:
				execution.execute(nodeLabel);
				break;
			case TaskRecovery:
				execution.recover(nodeLabel);
				break;
			case TaskSkip:
				execution.skip(nodeLabel);
				break;
			case ElephantStep:
				execution.elephantStep(nodeLabel);
				break;
			default:
				assert(false); //This is not supposed to happen
			}
		}
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();

		//Store the execution as a log event. NOTE: must be done after execution to properly set the attribute values.
		LogEvent logEvent = new LogEvent(Long.toString(caseID), nodeLabel, startTime, completionTime, (resourceSimulator == null)?null:resourceSimulator.getResource().getName());
		for (String touchedDataItem: Assignment.getAssignment().getBehavior().getTouches(nodeLabel)) {
			String dataItemValue = c.getValue(touchedDataItem, execution.currentState().dataItemTouches(touchedDataItem));
			logEvent.setAttributeValue(touchedDataItem, dataItemValue);
		}
		model.addLogEvent(logEvent);
	}
	
	/**
	 * Defers the node with the given label to be executed by the given skill. 
	 * (NB this does not affect queues, this case must still be taken from a queue and put in the skill queue)
	 * 
	 * @param nodeLabel the label of a node
	 * @param skill the skill to which the node must be deferred
	 */
	public void deferToSkill(String nodeLabel, String skill) {
		List<String> nodes = skill2deferredTasks.get(skill);
		if (nodes == null) {
			nodes = new ArrayList<String>();
			skill2deferredTasks.put(skill, nodes);
		}
		nodes.add(nodeLabel);
	}
	
	/**
	 * Returns a node that was deferred to the specified skill (in a FIFO manner).
	 * Deleted the node from the nodes that were deferred to specified skill.
	 * 
	 * @param skill a skill
	 * @return the label of a node
	 */
	public String getDeferredToSkill(String skill) {
		List<String> nodes = skill2deferredTasks.get(skill);
		return nodes.remove(0);
	}
	
	/**
	 * Reserves a resource to execute a task, ensuring that no other case can reserve it.
	 * Resources are unreserved when they start executing the task. However, they remain busy.
	 * Reserved resources can be released when the maximum active time passes.
	 * 
	 * @param rs the resource to reserve
	 */
	public void reserveResource(ResourceSimulator rs) {
		reservedResources.add(rs);
	}
	
	/**
	 * Unreserve a resource, either:
	 * - when it starts executing a task
	 * - when it is found to be unfit to execute a task (i.e. does not have the appropriate skills)
	 * 
	 * @param rs
	 */
	public void unreserveResource(ResourceSimulator rs) {
		reservedResources.remove(rs);
	}
		
	/**
	 * Release reserved resources.
	 * This should only be done when the maximum active time passes and the reserved resources will no longer be used.
	 * The released resources are also made 'not busy' and activated, such that they can check if there are other cases
	 * waiting in queue.
	 */
	public void releaseReservedResources() {
		for (ResourceSimulator rs: reservedResources) {
			rs.isBusy(false);
			rs.activate();
		}
		reservedResources = new HashSet<ResourceSimulator>();
	}
	
	/**
	 * Keeps a record of the queues that contain this case, so they can be released.
	 * 
	 * @param name the name of a skill or task queue
	 */
	public void isQueuedFor(ProcessQueue<CaseSimulator> pq, boolean isQueued) {
		if (isQueued) {
			queuedFor.add(pq);
		} else {
			queuedFor.remove(pq);
		}
	}
	
	/**
	 * Releases this case from all queues in which it is queued.
	 */
	public void unqueue() {
		for (ProcessQueue<CaseSimulator> pq: queuedFor) {
			pq.remove(this);
		}
	}

	public long getServiceTime() {
		return serviceTime;
	}

	public long getWaitingTime() {
		return waitingTime;
	}
	
	public long getThroughputTime() {
		return serviceTime + waitingTime;
	}
	
}
