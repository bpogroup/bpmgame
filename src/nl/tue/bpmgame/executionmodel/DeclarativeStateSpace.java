package nl.tue.bpmgame.executionmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.util.RandomGenerator;

public class DeclarativeStateSpace {

	Map<State,State> states;
	Map<State,Map<String,State>> transitions;
	Map<String,Set<State>> enabledInState;
	Set<State> finalStates;
	State initialState;
	
	boolean shortestPathsAreDirty;
	Map<Integer,Integer> stateIdToMatrixRowColumn;
	int[][] pathLengthBetweenStates;
	String[][] firstTransitionBetweenStates;
		
	public DeclarativeStateSpace(){
		states = new HashMap<State,State>();
		transitions = new HashMap<State,Map<String,State>>();
		enabledInState = new HashMap<String,Set<State>>();
		finalStates = null;
		initialState = null;
		shortestPathsAreDirty = true;
	}

	/**
	 * Adds a state.
	 * 
	 * @param s a state
	 */
	public void addState(State s){
		states.put(s,s);
		finalStates = null;
		initialState = null;
		shortestPathsAreDirty = true;		
	}
	
	/**
	 * Adds a transition between two states.
	 * 
	 * @param s1 a state
	 * @param t a transition
	 * @param s2 a state
	 */
	public void addTransition(State s1, String t, State s2){
		Map<String,State> targetMap = transitions.get(s1);
		if (targetMap == null){
			targetMap = new HashMap<String,State>();
			transitions.put(s1, targetMap);
		}
		targetMap.put(t, s2);
		Set<State> enablingStates = enabledInState.get(t);
		if (enablingStates == null){
			enablingStates = new HashSet<State>();
			enabledInState.put(t, enablingStates);
		}
		enablingStates.add(s1);
		finalStates = null;
		initialState = null;
		shortestPathsAreDirty = true;
	}
	
	/**
	 * Returns the state that will be reached by firing the given transition from the given state. 
	 * 
	 * @param s a state
	 * @param t a transition
	 * @return a state
	 */
	public State targetState(State s, String t){
		Map<String,State> targetMap = transitions.get(s);
		if (targetMap == null){
			return null;
		}else{
			return targetMap.get(t);
		}
	}
	
	/**
	 * Returns a state that is equivalent to the given state. A state is equivalent, if it has the name occurrences and touches.
	 * 
	 * @param s a state
	 * @return an equivalent state
	 */
	public State equivalent(State s){
		return states.get(s);
	}

	/**
	 * Returns the set of states in which the given transition is enabled. 
	 * 
	 * @param transition a transition
	 * @return a set of states
	 */
	public Set<State> getEnablingStates(String transition){		
		Set<State> result = enabledInState.get(transition);
		return (result == null)?new HashSet<State>():result;
	}
	
	/**
	 * Returns the set of final states.
	 * 
	 * @return a set of states
	 */
	public Set<State> finalStates(){
		if (finalStates == null){
			finalStates = new HashSet<State>();
			for (State s: states.keySet()){
				if (s.isFinal()){
					finalStates.add(s);
				}
			}
		}
		return finalStates;
	}
	
	/**
	 * Returns the initial state of the statespace. Returns an exception if there are multiple initial states. 
	 * 
	 * @return a state
	 * @throws Exception if there are multiple initial states of no initial states.
	 */
	public State initialState() throws Exception{
		if (initialState == null){
			Set<State> initialStates = new HashSet<State>();
			initialStates.addAll(states.keySet());
			for (Map.Entry<State, Map<String,State>> ts: transitions.entrySet()) {
				initialStates.removeAll(ts.getValue().values());
			}
			if (initialStates.size() == 0) {
				throw new Exception("There is no initial state in the statespace.");
			}else if (initialStates.size() > 1) {
				throw new Exception("There is more than one initial state in the statespace.");
			}else {
				initialState = initialStates.iterator().next();
			}
		}
		return initialState;
	}
	
	@Override
	public String toString(){
		String result = "{\tstates: [";
		for (Iterator<State> i = states.keySet().iterator(); i.hasNext();){
			State s = i.next();
			result += "\n\t\t" + s.toString();
			result += i.hasNext()?",":"";
		}
		result += "\n\t],\n\ttransitions: [";
		for (Iterator<State> i = transitions.keySet().iterator(); i.hasNext();){
			State fromState = i.next();
			for (Iterator<String> j = transitions.get(fromState).keySet().iterator(); j.hasNext();){
				String transition = j.next();
				State toState = transitions.get(fromState).get(transition);
				result += "\n\t\t("+fromState.getId()+", " + transition + ", " + toState.getId() + ")";
			}
		}
		result += "\n\t]\n}";
		return result;
	}
	
	/**
	 * Returns the set of states of this statespace. 
	 * 
	 * @return a set of states
	 */
	public Set<State> getStates(){
		return states.keySet();
	}
	
	/**
	 * Returns the next transition that takes the declarative statespace from the given state
	 * to a state in the given set. Returns null if there is no such transition.
	 * Returns "" if the given state is already in the set of states. 
	 * If there are multiple transitions, randomly returns one of them. 
	 * 
	 * @param fromState the state from which to determine the next transition
	 * @param toStates the set of states of which the model must transition to one of
	 * @return a transition, null, or ""
	 */
	public String firstTransitionOnPath(State fromState, Set<State> toStates) {
		List<String> possibleTransitions = new ArrayList<String>();
		for (State toState: toStates) {
			String transition = firstTransitionOnPath(fromState, toState);
			if (transition != null) {
				if (transition.length() == 0) {
					return "";
				} else {
					possibleTransitions.add(transition);
				}
			}
		}
		if (possibleTransitions.size() == 0) {
			return null;
		}else if (possibleTransitions.size() == 1) {
			return possibleTransitions.get(0);
		}else {
			return possibleTransitions.get((int) RandomGenerator.generateUniform(possibleTransitions.size()));
		}		
	}
	
	/**
	 * Returns the first transition on the shortest path from one state to another.
	 * Returns null if there is no such path.
	 * Returns "" if the states are identical.
	 * 
	 * @param fromState a State
	 * @param toState a State
	 * @return a transition, null, or ""
	 */
	public String firstTransitionOnPath(State fromState, State toState) {
		if (shortestPathsAreDirty) {
			recomputeShortestPaths();
		}
		return firstTransitionBetweenStates[stateIdToMatrixRowColumn.get(fromState.getId())][stateIdToMatrixRowColumn.get(toState.getId())];
	}
	
	private void recomputeShortestPaths() {
		int nrVertices = states.keySet().size(); 
		
		//let dist be a |V| × |V| array of minimum distances initialized to infinity
		//(we use infinity == nrVertices, because this is the theoretical maximum length of a path between two vertices)
		pathLengthBetweenStates = new int[nrVertices][];
		firstTransitionBetweenStates = new String[nrVertices][];
		for (int i = 0; i < nrVertices; i++) {
			pathLengthBetweenStates[i] = new int[nrVertices];
			firstTransitionBetweenStates[i] = new String[nrVertices];
			for (int j = 0; j < nrVertices; j++) {
				pathLengthBetweenStates[i][j] = nrVertices;
				firstTransitionBetweenStates[i][j] = null;
			}
		}
			
		//for each vertex v
		//   dist[v][v] <- 0
		int v = 0;
		stateIdToMatrixRowColumn = new HashMap<Integer,Integer>();
		for (State s: states.keySet()) {
			stateIdToMatrixRowColumn.put(s.getId(), v);
			pathLengthBetweenStates[v][v] = 0;
			firstTransitionBetweenStates[v][v] = "";
			v++;
		}
		
		//for each edge (u,v)
		//   dist[u][v] <- w(u,v)  // the weight of the edge (u,v)
		for (Map.Entry<State, Map<String,State>> ts: transitions.entrySet()) {
			State source = ts.getKey();
			for (Map.Entry<String,State> t: ts.getValue().entrySet()) {
				String transition = t.getKey();
				State target = t.getValue();
				pathLengthBetweenStates[stateIdToMatrixRowColumn.get(source.getId())][stateIdToMatrixRowColumn.get(target.getId())] = 1;
				firstTransitionBetweenStates[stateIdToMatrixRowColumn.get(source.getId())][stateIdToMatrixRowColumn.get(target.getId())] = transition;
			}
		}
		
		//for k from 1 to |V|
		//   for i from 1 to |V|
		//      for j from 1 to |V|
		//         if dist[i][j] > dist[i][k] + dist[k][j] 
		//             dist[i][j] <- dist[i][k] + dist[k][j]
		//         end if
		for (int k = 0; k < nrVertices; k++) {
			for (int i = 0; i < nrVertices; i++) {
				for (int j = 0; j < nrVertices; j++) {
					if (pathLengthBetweenStates[i][j] > pathLengthBetweenStates[i][k] + pathLengthBetweenStates[k][j]) {
						pathLengthBetweenStates[i][j] = pathLengthBetweenStates[i][k] + pathLengthBetweenStates[k][j];
						firstTransitionBetweenStates[i][j] = firstTransitionBetweenStates[i][k];
					}
				}
			}
		}
		
		shortestPathsAreDirty = false;
	}
}
