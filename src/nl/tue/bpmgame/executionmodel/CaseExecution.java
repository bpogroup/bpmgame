package nl.tue.bpmgame.executionmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmn.concepts.BPMNArc;
import nl.tue.bpmn.concepts.BPMNModel;
import nl.tue.bpmn.concepts.BPMNNode;
import nl.tue.bpmn.concepts.BPMNNode.NodeType;
import nl.tue.bpmn.parser.ConditionEvaluator;
import nl.tue.util.RandomGenerator;

/**
 *	This class facilitates the execution of a particular case in a BPMN model.
 *	The execution is governed by a declarative model, i.e. if the BPMN model 
 *	wants to perform a transition that is not enabled by the declarative model,
 *  the transition cannot be execution. Instead, the case execution will provide
 *  a suggestion for a transition that can be executed on a path through the
 *  declarative statespace from which the transition can be executed.
 *
 *  Main methods of the class are:
 *  - nextExecution, returns the labels of the next transitions to execute
 *  - fixForNextExecution, returns the fixes necessary to execute the next transitions, fixForNextExecution[i] applied to nextExecution[i]
 *  - currentState, returns the current state, which originally equals the initial state of the BPMN model
 *  - execute, executes a transition both in the BPMN and in the declarative model
 *  - recover, executes a transition in the declarative model, but not in the BPMN model (effectively 'recovering' the execution of the BPMN model) 
 *  - skip, executes a transition in the BPMN model, but not in the declarative model (effectively 'skipping' it)
 *  
 *  In addition, the class has methods that represent the execution semantics of a BPMN model:
 *  determineInitialState, determineEnabledNodes, fireTransition 
 */
public class CaseExecution {
	
	public enum Fix {None, PrematureCompletion, TaskRecovery, TaskSkip, ElephantStep};
	
	BPMNModel bpmnModel;
	Case executionCase;
	BPMNState currentState;
	List<String> nextExecution;
	List<Fix> fixForNextExecution;
	
	List<String> toRepeat;
	String repeatUntil;

	public CaseExecution(BPMNModel bpmnModel, Case executionCase) {
		this.bpmnModel = bpmnModel;
		this.executionCase = executionCase;
		currentState = determineInitialState();
		repeatUntil = null;
		toRepeat = new ArrayList<String>();
		determineNextExecution(null);
	}

	/**
	 * Returns the state of the BPMN model that is the result of the start event firing. 
	 * Assumes that there is one start event that has exactly one outgoing arc.
	 * 
	 * @param bpmnModel a BPMN model
	 * @return a BPMN state
	 */
	public BPMNState determineInitialState() {
		BPMNArc initialArc = bpmnModel.getStartEvent().getOutgoing().iterator().next();
		BPMNState initialState = new BPMNState();
		initialState.addMarking(initialArc);
		initialState.addTransition(bpmnModel.getStartEvent().getName());
		for (String dataItem: Assignment.getAssignment().getBehavior().getTouches(bpmnModel.getStartEvent().getName())){
			initialState.addDataItem(dataItem);
		}
		initialState = traverseControlNodes(initialState);
		return initialState;
	}

	/**
	 * Returns the nodes that are enabled in the BPMN model in the given state.
	 * A node is a task, an intermediate event or an end event.
	 * Assumes that:
	 * - arcs are always connected (i.e. have a source and target node)
	 * 
	 * @param bpmnModel a BPMN model
	 * @param bpmnState a BPMN state
	 * @return a set of nodes that are enabled in the BPMN model in the given state
	 */
	public Set<BPMNNode> determineEnabledNodes(BPMNState bpmnState) {
		Set<BPMNNode> enabledNodes = new HashSet<BPMNNode>();
		for (BPMNArc bpmnArc: bpmnState.getMarkedArcs()){
			BPMNNode targetNode = bpmnArc.getTarget(); 
			if ((targetNode.getType() == NodeType.EndEvent) || (targetNode.getType() == NodeType.Task)){
				enabledNodes.add(targetNode);
			}else if ((targetNode.getType() == NodeType.IntermediateEvent) && (executionCase.getOccurringEvents(currentState).contains(targetNode.getName()))){
				enabledNodes.add(targetNode);
			}
		}
		return enabledNodes;
	}
	
	/**
	 * Calculates the state that results from firing the given BPMN node in the given
	 * BPMN state. There are some assumptions:
	 * - control nodes will be traversed a maximum number of times to prevent lifelock, after that the case is stopped by removing all markings
	 * - if an exclusive or event based split has multiple enabled outgoing arcs, one is traversed at random
	 * - gateways are either split or join and have either one incoming and multiple outgoing arcs (in case of a split) or multiple incoming and one outgoing (in case of a join) 
	 * 
	 * @param bpmnState a BPMN state
	 * @param bpmnNode a BPMN node (must be a task, intermediate event or end event)
	 * @param silent if !silent, the occurrence of the bpmnNode is registered as well as the data items that are touched, if silent, these are not registered
	 * @return the BPMN state that results from firing the given BPMN Node in the given BPMN state, null if the transition cannot fire
	 */
	public BPMNState fireTransition(BPMNState bpmnState, BPMNNode bpmnNode, boolean silent){
		BPMNArc enablingArc = null;
		for (BPMNArc a: bpmnState.getMarkedArcs()){
			if (a.getTarget() == bpmnNode){
				enablingArc = a;
				break;
			}
		}
		if (enablingArc == null){
			return null;
		}else{
			bpmnState.removeMarking(enablingArc);
			if (!bpmnNode.getOutgoing().isEmpty()){
				bpmnState.addMarking(bpmnNode.getOutgoing().iterator().next());
			}
			if (!silent) {
				bpmnState.addTransition(bpmnNode.getName());
				for (String dataItem: Assignment.getAssignment().getBehavior().getTouches(bpmnNode.getName())){
					bpmnState.addDataItem(dataItem);
				}
			}
			bpmnState = traverseControlNodes(bpmnState);
			return bpmnState;
		}
	}
	
	/**
	 * Equals fireTransition(bpmnState, bpmnNode, false)
	 */
	public BPMNState fireTransition(BPMNState bpmnState, BPMNNode bpmnNode){
		return fireTransition(bpmnState, bpmnNode, false);
	}
	
	private BPMNState traverseControlNodes(BPMNState bpmnState){
		boolean moreControlNodesToTraverse = true;
		int numberTraversed = 0;
		int MAX_TO_TRAVERSE = 10;
		while (moreControlNodesToTraverse && numberTraversed < MAX_TO_TRAVERSE){
			moreControlNodesToTraverse = false;
			Set<BPMNArc> marksToRemove = new HashSet<BPMNArc>();
			Set<BPMNArc> marksToAdd = new HashSet<BPMNArc>();
			boolean traversedAControlNode = false;
			for (Iterator<BPMNArc> arcIterator = bpmnState.getMarkedArcs().iterator(); arcIterator.hasNext() && !traversedAControlNode;){
				BPMNArc arc = arcIterator.next(); 
				BPMNNode targetNode = arc.getTarget();
				switch (targetNode.getType()){
				case ParallelSplit:
					marksToRemove.add(arc);
					marksToAdd.addAll(targetNode.getOutgoing());
					traversedAControlNode = true;
					break;
				case ParallelJoin:
					boolean allIncomingArcsMarked = true;
					for (BPMNArc incomingArc: targetNode.getIncoming()){
						if (!bpmnState.getMarkedArcs().contains(incomingArc)){
							allIncomingArcsMarked = false;
							break;
						}
					}
					if (allIncomingArcsMarked){
						marksToRemove.addAll(targetNode.getIncoming());
						marksToAdd.addAll(targetNode.getOutgoing());
						traversedAControlNode = true;
					}
					break;
				case ExclusiveSplit:
					marksToRemove.add(arc);
					Set<BPMNArc> enabledOutgoing = new HashSet<BPMNArc>();
					for (BPMNArc outgoingArc: targetNode.getOutgoing()){
						if (conditionMet(bpmnState, outgoingArc)){
							enabledOutgoing.add(outgoingArc);
						}
					}
					if (!enabledOutgoing.isEmpty()){
						marksToAdd.add((BPMNArc) enabledOutgoing.toArray()[(int) RandomGenerator.generateUniform(enabledOutgoing.size())]);
					}
					traversedAControlNode = true;
					break;
				case ExclusiveJoin:
					marksToRemove.add(arc);
					marksToAdd.add(targetNode.getOutgoing().iterator().next());
					traversedAControlNode = true;
					break;
				case EventBasedGateway:
					marksToRemove.add(arc);
					BPMNArc selectedArc = null; //Select the outgoing arc with the enabled event that occurs first
					Set<String> occurringEvents = executionCase.getOccurringEvents(currentState);
					for (BPMNArc outgoingArc: targetNode.getOutgoing()){
						if (occurringEvents.contains(outgoingArc.getTarget().getName())){
							if (selectedArc == null) {
								selectedArc = outgoingArc;
							}else{
								String existingEvent = selectedArc.getTarget().getName();
								String newEvent = outgoingArc.getTarget().getName();
								if (executionCase.getTime(newEvent) < executionCase.getTime(existingEvent)) {									
									selectedArc = outgoingArc;
								}
							} 							
						}
					}
					if (selectedArc != null){
						marksToAdd.add(selectedArc);
					}
					traversedAControlNode = true;
					break;
				default:
					break;
				}
			}
			if (traversedAControlNode){
				moreControlNodesToTraverse = true;
				bpmnState.removeMarking(marksToRemove);
				bpmnState.addMarking(marksToAdd);
				numberTraversed++;
			}
		}
		if (numberTraversed >= MAX_TO_TRAVERSE){
			return new BPMNState();
		}
		return bpmnState;
	}
	
	/**
	 * Returns true if the condition on the arc is met.
	 * The condition is met, if there is no condition and if the condition is true. 
	 * 
	 * @param bpmnState a BPMN state
	 * @param arc an arc
	 * @return true, iff the condition on the arc is met
	 */
	private boolean conditionMet(BPMNState bpmnState, BPMNArc arc){
		if (arc.getCondition().length() == 0){
			return true;
		}
		String condition = arc.getCondition();
		return ConditionEvaluator.getInstance().evaluate(condition, executionCase, bpmnState);
	}

	/**
	 * Computes the next transitions that should be executed from the current state of the BPMN model, 
	 * taking into account the declarative model that describes the allowed executions. 
	 * Returns the next BPMN nodes that should be executed.
	 * 
	 * The next node to execute is:
	 * - {},   if the current state is not valid according to the declarative model
	 *         (fix == PrematureCompletion)
	 * - {},   if the process was run to completion, the current state is a final state in the declarative model 
	 *         (fix == None)
	 * - {},   if the process was run to completion, the current state is NOT a final state in the declarative model,
	 *         and there exists NO transition from this state to take it towards a final state
	 *         (fix == PrematureCompletion) 
	 * - {t},  if the process was run to completion, the current state is NOT a final state in the declarative model,
	 *         and there exists a transition t from this state to take it towards a final state
	 * 		   (fix == TaskRecovery)
	 * If the next nodes that are enabled in the BPMN model is the set ts, the resultset contains for each t in ts:
	 * - t,    if transition t is enabled in the declarative model
	 *         (fix == None)
	 * - u,    if transition t is not enabled in the declarative model, but transition u takes the model towards a state in
	 *         which transition t is enabled
	 *         (fix == TaskRecovery)
	 * - t,    if transition t is not enabled in the declarative model, and there exists no transition u to take it
	 *         towards a state in which transition t is enabled 
	 *         (fix == TaskSkip)
	 *         in this case, the current state is also updated by firing t in the BPMN model 'silently'
	 *         (i.e. not updating the occurred transitions and touched data)
	 *  
	 * @return a set of labels of the node that should be executed from the current state 
	 */
	public List<String> nextExecution(){
		return nextExecution;
	}
	
	/**
	 * Returns the fixes that are necessary to make the next executions possible.
	 * The fixForNextExecution in position i relates to the nextExecution in position i,
	 * unless the case completed, in which case there is no nextExecution and one fixForNextExecution.  
	 * Fix is one of the following:
	 * - None, if no fix is necessary.
	 * - TaskRecovery, if the next execution is an attempt to reach a state in the declarative model that 
	 *                 enables a valid move in the BPMN model.
	 * - PrematureCompletion, if the declarative model cannot be brought into a state that allows a valid move
	 *                        in the BPMN model, or no move is possible and the declarative model cannot be brought
	 *                        into a final state.
	 * - TaskSkip, if the next node can be executed in the BPMN model, but not in the declarative model and it
	 *             is not possible to bring the declarative model into a state in which the node can be executed.
	 * 
	 * @return a Fix
	 */
	public List<Fix> fixForNextExecution(){		
		return fixForNextExecution;
	}
	
	/**
	 * Executes a node that can be executed both in the BPMN and in the declarative model.
	 * If labelToExecute is not in nextExecution(), nothing happens.
	 * Pre-condition: if labelToExecute is nextExecution(), it should be an executable task (i.e. fix == None).
	 * Moves the current state, into a state in which:
	 * - the occurred transitions are updated by increasing the occurrence of nextExecution() by 1
	 * - the touches are updated accordingly
	 * - the marking of the BPMN model is updated
	 */
	public void execute(String labelToExecute){
		int taskIndex = nextExecution.indexOf(labelToExecute);
		if (taskIndex == -1) {
			return;
		}		
		assert(fixForNextExecution.get(taskIndex) == Fix.None); //Pre-condition: labelToExecute should be an executable task
		BPMNNode nodeToExecute = null;
		for (BPMNNode node: determineEnabledNodes(currentState)) {
			if (node.getName().equals(labelToExecute)) {
				nodeToExecute = node;
				break;
			}
		}		
		if (nodeToExecute != null) {
			//If the transition can fire in the BPMN model, simply execute firetransition
			currentState = fireTransition(currentState, nodeToExecute);
		} else {
			//This should not happen. It may cause a livelock, so be careful when this happens.
			Logger.getLogger(CaseExecution.class).warn("WARNING: transition " + labelToExecute + " cannot be executed in the BPMN model: it is not enabled.");			
		}
		determineNextExecution(labelToExecute);
	}

	/**
	 * Executes a node that can be executed in the declarative model, but not in the BPMN model.
	 * If labelToRecover is not in nextExecution(), nothing happens.
	 * Pre-condition: if labelToRecover is in nextExecution(), it should be a recoverable task (i.e. fix == TaskRecovery).
	 * Moves the current state, into a state in which:
	 * - the occurred transitions are updated by increasing the occurrence of nextExecution() by 1
	 * - the touches are updated accordingly
	 */
	public void recover(String labelToRecover){
		int taskIndex = nextExecution.indexOf(labelToRecover);
		if (taskIndex == -1) {
			return;
		}				
		assert(fixForNextExecution.get(taskIndex) == Fix.TaskRecovery); //Pre-condition: labelToExecute should be a recoverable task
		currentState.addTransition(labelToRecover);
		for (String dataItem: Assignment.getAssignment().getBehavior().getTouches(labelToRecover)){
			currentState.addDataItem(dataItem);
		}
		determineNextExecution(labelToRecover);
	}

	/**
	 * Executes a node that can be executed in the BPMN model, but not in the declarative model.
	 * If labelToSkip is not in nextExecution(), nothing happens.
	 * Pre-condition: if labelToSkip is in nextExecution(), it should be a skippable task (i.e. fix == Skip).
	 * Moves the current state, into a state in which:
	 * - the marking of the BPMN model is updated
	 */
	public void skip(String labelToSkip) {
		int taskIndex = nextExecution.indexOf(labelToSkip);
		if (taskIndex == -1) {
			return;
		}				
		assert(fixForNextExecution.get(taskIndex) == Fix.TaskSkip); //Pre-condition: labelToSkip should be a skippable task
		BPMNNode nodeToExecute = null;
		for (BPMNNode node: determineEnabledNodes(currentState)) {
			if (node.getName().equals(labelToSkip)) {
				nodeToExecute = node;
				break;
			}
		}		
		if (nodeToExecute != null) {
			//If the transition can fire in the BPMN model, simply execute firetransition
			currentState = fireTransition(currentState, nodeToExecute, true);
		} else {
			//This should not happen. It may cause a livelock, so be careful when this happens.
			Logger.getLogger(CaseExecution.class).warn("WARNING: transition " + labelToSkip + " cannot be skipped in the BPMN model: it is not enabled.");
		}
		determineNextExecution(labelToSkip);
	}
	
	/**
	 * Executes an elephant trail step. This does not affect the execution state. 
	 */
	public void elephantStep(String task) {
		determineNextExecution(task);
	}

	/**
	 * Returns the current state of the declarative and the BPMN model
	 * 
	 * @return the current state
	 */
	public BPMNState currentState(){
		return currentState;
	}
	
	private void determineNextExecution(String previousTask) {
		//Check elephant trails
		if (previousTask != null) {
			//Redo
			if (executionCase.isRedo(previousTask)) {
				executionCase.removeRedo(previousTask);
				nextExecution = new ArrayList<String>(Arrays.asList(new String[] {previousTask}));
				fixForNextExecution = new ArrayList<Fix>(Arrays.asList(new Fix[] {Fix.ElephantStep}));
				return;				
			}
			//Repeat			
			String untilActivity = executionCase.repeatUntil(previousTask);
			if (untilActivity != null) { //Reached a 'from' activity
				repeatUntil = untilActivity;
				toRepeat = new ArrayList<String>();
				executionCase.removeRepeat(previousTask);
			}
			if (repeatUntil != null) { //Recording activities to repeat
				toRepeat.add(previousTask);
				if (repeatUntil.equals(previousTask)) { //Reached the 'until' activity
					repeatUntil = null;
				}
			}
			if ((repeatUntil == null) && !toRepeat.isEmpty()) { //Repeating activities
				nextExecution = new ArrayList<String>(Arrays.asList(new String[] {toRepeat.remove(0)}));
				fixForNextExecution = new ArrayList<Fix>(Arrays.asList(new Fix[] {Fix.ElephantStep}));
				return;								
			}			
		}
		
		//Find the BPMN state in the declarative statespace
		State declarativeState = executionCase.getDeclarativeStateSpace().equivalent(currentState);
		
		//If no equivalent state exists, the case is closed: there is no nextExecution and mark as premature completion
		if (declarativeState == null) {
			nextExecution = new ArrayList<String>();
			fixForNextExecution = new ArrayList<Fix>(Arrays.asList(new Fix[] {Fix.PrematureCompletion}));
			return;
		}
		
		//There is an equivalent state
		//If there are no more enabled transitions in the BPMN state
		Set<BPMNNode> enabledNodes = determineEnabledNodes(currentState);
		if (enabledNodes.isEmpty()) {
			//Check if the BPMN state is indeed a final state
			if (declarativeState.isFinal()) {
				//If the state is a final state
				//The case is closed, mark as no fix
				nextExecution = new ArrayList<String>();
				fixForNextExecution = new ArrayList<Fix>(Arrays.asList(new Fix[] {Fix.None}));
				return;
			}else {
				//If the state is not a final state
				//Find a transition that leads from this state to a final state according to the declarative model
				String fixingTransition = executionCase.getDeclarativeStateSpace().firstTransitionOnPath(declarativeState, executionCase.getDeclarativeStateSpace().finalStates());
				if (fixingTransition == null) {
					//If there is no such transition: the process is done and mark for premature completion				
					nextExecution = new ArrayList<String>();
					fixForNextExecution = new ArrayList<Fix>(Arrays.asList(new Fix[] {Fix.PrematureCompletion}));
					return;					
				}else {
					//If there is such a transition: return the transition and mark for task recovery
					nextExecution = new ArrayList<String>(Arrays.asList(new String[] {fixingTransition}));
					fixForNextExecution = new ArrayList<Fix>(Arrays.asList(new Fix[] {Fix.TaskRecovery}));
					return;
				}				
			}			
		}

		//Iterate over all enabled nodes
		nextExecution = new ArrayList<String>();
		fixForNextExecution = new ArrayList<Fix>();			
		for (BPMNNode enabledNode: enabledNodes) {
			if (executionCase.getDeclarativeStateSpace().targetState(declarativeState, enabledNode.getName()) != null) {
				//If the transition can be performed according to the declarative model
				//return it, mark as no fix
				nextExecution.add(enabledNode.getName());
				fixForNextExecution.add(Fix.None);
			} else {
				//If the transition cannot be performed according to the declarative model
				//find another transition that leads (indirectly) to a state in which the transition can be performed
				String fixingTransition = executionCase.getDeclarativeStateSpace().firstTransitionOnPath(declarativeState, executionCase.getDeclarativeStateSpace().getEnablingStates(enabledNode.getName()));
				if (fixingTransition != null) {
					//If such a transition exists, 
					//return it and mark for task recovery
					nextExecution.add(fixingTransition);
					fixForNextExecution.add(Fix.TaskRecovery);
				} else {
					//If no such transition exists, skip the task in a final attempt to fix the process
					nextExecution.add(enabledNode.getName());
					fixForNextExecution.add(Fix.TaskSkip);
				}
			}
		}
		//Check elephant trails
		processSkips();
	}
	
	/*
	 * Processes all the tasks that are marked as 'skipped' in the Case.
	 * These tasks are executed/skipped/recovered as usual, such that data is registered. 
	 */
	private void processSkips() {
		for (int i = 0; i < nextExecution.size(); i++) {
			String task = nextExecution.get(i);
			if (executionCase.isSkip(task)) {
				executionCase.removeSkip(task);
				switch (fixForNextExecution.get(i)) {
				case None:
					execute(task);
					break;
				case TaskSkip:
					skip(task);
					break;
				case TaskRecovery:
					recover(task);
					break;
				default:
				}
			}
		}
	}
}
