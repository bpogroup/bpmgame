package nl.tue.bpmgame.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;
import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.assignment.Resource;
import nl.tue.bpmgame.assignment.Task;
import nl.tue.bpmn.concepts.BPMNNode;
import nl.tue.bpmn.concepts.BPMNRole;
import nl.tue.util.RandomGenerator;

/**
 * Simulates a resource for a particular BPMN model.
 * 
 * Behavior:
 * 1.a. If there is a (randomly selected) skill queue with a case in it, perform that case
 * 1.b. If there is a (randomly selected) task queue with a case in it:
 *      I.   If the resource has the necessary skills: perform the task
 *           (invoke startNodeExecution to obtain execution time + penalty for task recovery if any),  
 *      II.  If the resource does not have the necessary skills, but the probability that the task is executed anyway manifests itself: perform the task
 *           (invoke startNodeExecution to obtain execution time + penalty for wrong skills + penalty for task recovery if any)
 *      III. If the resource does not have the necessary skills, and the task is not executed: put it in a skill queue, wake up a resource with the appropriate skills 
 * 2. If no tasks were performed: sleep
 * Repeat 1-2
 */
public class ResourceSimulator extends SimProcess{

	private boolean isBusy;
	private Resource resource;
	private List<BPMNRole> roles; //Note that for the general manager role == null
	private SimulatorModel model;

	public ResourceSimulator(SimulatorModel model, String resourceName, BPMNRole role) {
		super(model, resourceName, false);
		isBusy = false;
		resource = Assignment.getAssignment().getResource(resourceName);
		if (role != null) {
			roles = new ArrayList<BPMNRole>();
			roles.add(role);
		}
		this.model = model;
	}

	public void lifeCycle() throws SuspendExecution {
		//Repeat indefinitely
		while (true) {
			//If this resource needs to perform any skills:
			Object skillAndCase[] = getCaseFromSkillQueue();
			if (skillAndCase != null) {
				//do it at a penalty.
				CaseSimulator c = (CaseSimulator) skillAndCase[1];
				String skillToPerform = (String) skillAndCase[0];
				String labelOfTaskToPerform = c.getDeferredToSkill(skillToPerform);
				long executionTime = c.startNodeExecution(labelOfTaskToPerform, this, true);
				long startTime = model.getSimulationTime();
				//hold
				c.unreserveResource(this);
				hold(new TimeSpan(executionTime, TimeUnit.MILLISECONDS));
				//complete execution
				c.completeNodeExecution(labelOfTaskToPerform, startTime, model.getSimulationTime(), this, true);
				c.activate();
			} else {
			//else, if it needs to perform any tasks:
				Object taskAndCase[] = getCaseFromTaskQueue();
				if (taskAndCase != null) {
					CaseSimulator c = (CaseSimulator) taskAndCase[1];
					String labelOfTaskToPerform = (String) taskAndCase[0];
					Task taskToPerform = Assignment.getAssignment().getTask(labelOfTaskToPerform);
					//if the resource has the skills for the task:
					boolean isSkilled = resource.isSkilled(taskToPerform);
					//   or with a probability will perform the task anyway (startNodeExecution will add a penalty)
					boolean willPerformTask = isSkilled || Assignment.getAssignment().performUnskilled();
					if (willPerformTask){
					//perform the task
						long executionTime = c.startNodeExecution(labelOfTaskToPerform, this, false);
						long startTime = model.getSimulationTime();
						//hold
						c.unreserveResource(this);
						hold(new TimeSpan(executionTime, TimeUnit.MILLISECONDS));
						//complete execution
						c.completeNodeExecution(labelOfTaskToPerform, startTime, model.getSimulationTime(), this, isSkilled);
						c.activate();
					} else {
					//if the resource does not have the skills and does not perform the task
						c.unreserveResource(this);
						//put the task in a skill queue instead
						ProcessQueue<CaseSimulator> q = model.queueFor(taskToPerform.getRequiredSkill());
						q.insert(c);
						c.isQueuedFor(q, true);
						c.deferToSkill(labelOfTaskToPerform, taskToPerform.getRequiredSkill());
						//find a resource with the appropriate skills that is not busy and wake it up (if it exists)
						ResourceSimulator resourceWithSkill = model.getResourceFor(taskToPerform.getRequiredSkill());
						if (resourceWithSkill != null) {
							c.reserveResource(resourceWithSkill);
							resourceWithSkill.isBusy(true);
							resourceWithSkill.activate();		
						}
					}
					
				} else {
				//else, if no tasks were found to perform: go to sleep
					isBusy(false);
					passivate();
				}
			}
		}
	}
	
	/**
	 * returns a [skill,CaseSimulator] combination for a case that in the skill queue of one this resource's skills.
	 *         null, if no such case exists
	 */
	private Object[] getCaseFromSkillQueue() {
		List<String> skills = new ArrayList<String>();
		skills.addAll(resource.getSkills());
		while (!skills.isEmpty()) {
			//Randomly select a task that this resource can perform
			int i = (int) RandomGenerator.generateUniform(skills.size());
			String skill = skills.get(i);
			skills.remove(i);
			
			//If there is a task of this type to perform
			ProcessQueue<CaseSimulator> skillQueue = model.queueFor(skill);
			if (!skillQueue.isEmpty()) {
				CaseSimulator c = skillQueue.removeFirst();
				c.isQueuedFor(skillQueue, false);
				Object result[] = {skill,c};
				return result;
			}
		}		
		return null;		
	}
	
	/**
	 * returns a [taskLabel,CaseSimulator] combination for a task that in the task queue of one this resource's tasks.
	 *         null, if no such case exists
	 */
	private Object[] getCaseFromTaskQueue() {
		//General manager has no role, only performs skill tasks
		if (roles == null) {
			return null;
		}
		//Otherwise:
		List<BPMNNode> tasks = new ArrayList<BPMNNode>();
		for (BPMNRole role: roles) {
			tasks.addAll(role.getContainedTasks());
		}
		while (!tasks.isEmpty()) {
			//Randomly select a task that this resource can perform
			int i = (int) RandomGenerator.generateUniform(tasks.size());
			BPMNNode task = tasks.get(i);
			tasks.remove(i);
			
			//If there is a task of this type to perform
			ProcessQueue<CaseSimulator> taskQueue = model.queueFor(task.getName());
			if (!taskQueue.isEmpty()) {
				CaseSimulator c = taskQueue.removeFirst();
				c.isQueuedFor(taskQueue, false);
				Object result[] = {task.getName(),c};
				return result;
			}
		}		
		return null;
	}

	public boolean isBusy() {
		return isBusy;
	}

	public Resource getResource() {
		return resource;
	}

	public void isBusy(boolean isBusy) {
		this.isBusy = isBusy;
	}

	public void addRole(BPMNRole role) {
		roles.add(role);
	}
	
}
