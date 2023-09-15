package nl.tue.bpmgame.declarative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmgame.executionmodel.State;
import nl.tue.bpmgame.executionmodel.DeclarativeStateSpace;
import nl.tue.bpmn.parser.ConditionEvaluator;

public class DeclarativeModel {

	private Map<String,String> transitionConditions;
	private Map<String,Set<String>> transitionTouches;

	public DeclarativeModel(){
		transitionConditions = new HashMap<String,String>();
		transitionTouches = new HashMap<String,Set<String>>();
	}

	/**
	 * Sets the given condition as the condition for the occurrence of the transition with the given label.
	 * If the transition with the given label already exists, the condition is overwritten.
	 * An empty condition means 'true at the start'.
	 * A transition can only be enabled by other transitions once, i.e. if the occurrence of transition B 
	 * is a condition for the occurrence of transition A, A can only be enabled if occurrences(B) > occurrences(A) 
	 * Precondition: the statespace must be finite
	 * 
	 * @param transition	the label of the transition to set the condition for
	 * @param condition		the condition to set
	 */
	public void addCondition(String transition, String condition){
		transitionConditions.put(transition, condition);
	}

	/**
	 * Adds that the given transition touches the given data item.
	 * 
	 * @param transition a transition
	 * @param dataItem a data item
	 */
	public void addTransitionTouches(String transition, String dataItem){
		Set<String> touches = transitionTouches.get(transition);
		if (touches == null){
			touches = new HashSet<String>();
			transitionTouches.put(transition, touches);
		}
		touches.add(dataItem);
	}

	/**
	 * Computes the possible statespace for the given case, i.e. the possible paths that the case can take through the process. 
	 * 
	 * @param c	the case for which the statespace must be computed
	 * @return	the statespace
	 */
	public DeclarativeStateSpace computeStateSpace(Case c){
		DeclarativeStateSpace ss = new DeclarativeStateSpace();

		State startState = new State();
		ss.addState(startState);
		Map<State,Set<String>> nonTraversedTransitions = new HashMap<State,Set<String>>();
		nonTraversedTransitions.put(startState, computeEnabled(c, startState));
		while (!nonTraversedTransitions.isEmpty()){

			//get an arbitrary non-traversed transition and remove it from the list of non-traversed transitions
			State fromState = null;
			String transition = null;
			while (!nonTraversedTransitions.isEmpty() && (transition == null)){
				Entry<State, Set<String>> stateTransition = nonTraversedTransitions.entrySet().iterator().next();
				fromState = stateTransition.getKey();
				if (stateTransition.getValue().isEmpty()){
					nonTraversedTransitions.remove(fromState);					
				}else{
					transition = stateTransition.getValue().iterator().next();
					stateTransition.getValue().remove(transition);

				}
			}

			//if a transition to traverse is still found
			if (transition != null){
				//traverse the transition, by: 
				//1. creating the new state in which the transition occurred
				//2. checking if that state already exists
				//3a. if the state does not exist, add the state to the statespace, and compute new transitions for that state
				//3b. if the state already exists, simply discard the new state
				//4. adding the transition from the old state to the new to the statespace

				//1.
				State newState = fromState.clone();
				newState.addTransition(transition);
				for (String dataItem: getTouches(transition)){
					newState.addDataItem(dataItem);
				}
				newState.setIsFinal(ConditionEvaluator.getInstance().evaluate(c.getCompletionCondition(), c, newState));
				//2.
				State toState = ss.equivalent(newState);
				if (toState == null){
					//3a.
					ss.addState(newState);
					toState = newState;
					nonTraversedTransitions.put(toState, computeEnabled(c, toState));
				}
				//4.
				ss.addTransition(fromState, transition, toState);
			}
		}

		return ss;
	}
	
	public Set<String> getTouches(String transition){
		Set<String> touches = transitionTouches.get(transition);
		if (touches == null){
			return new HashSet<String>(); 
		}
		return touches;
	}

	/**
	 * Computes the transitions that are enabled for the given case in the given state. 
	 * 
	 * @param c	the case
	 * @param s	the state
	 * @return	a set of transitions identified by their labels that is enabled
	 */
	public Set<String> computeEnabled(Case c, State s){
		Set<String> enabledTransitions = new HashSet<String>();
		for (Map.Entry<String, String> tc: transitionConditions.entrySet()){
			String transition = tc.getKey();
			String condition = tc.getValue();
			if ((condition.length() == 0) && (s.transitionOccurrences(transition) == 0)){
				enabledTransitions.add(transition);				
			}else if ((condition.length() != 0) && ConditionEvaluator.getInstance().evaluate(condition, c, s, transition)){
				enabledTransitions.add(transition);
			}
		}
		return enabledTransitions;
	}

	public Set<String> getDefinedTransitions(){
		return transitionConditions.keySet();
	}
	
	public Set<String> getDefinedTransitionTouching(){
		return transitionTouches.keySet();
	}
	
}
