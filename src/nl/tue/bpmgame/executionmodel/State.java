package nl.tue.bpmgame.executionmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *	A state is defined by how often each data item is touched
 *  and how often particular transitions have occurred.
 *  A state was reached through certain sequences of transition occurrences.
 *  A state has an identifier that identifies it uniquely within the set of states.
 *  A cloned state has a different identifier than the state from which it is cloned. 
 *
 */
public class State implements Cloneable{

	static int nextId = 0;
	
	private Map<String,Integer> transitionOccurrences; 
	private Map<String,Integer> dataItemTouches;
	private boolean isFinal;
	
	private Integer hashCode;
	private int id;
	
	public State(){
		transitionOccurrences = new HashMap<String,Integer>(); 
		dataItemTouches = new HashMap<String,Integer>();
		isFinal = false;
		
		hashCode = null;
		id = nextId;
		nextId++;
	}
	
	public boolean isFinal(){
		return isFinal;
	}
	
	public void setIsFinal(boolean isFinal){
		this.isFinal = isFinal;
	}

	@SuppressWarnings("unchecked")
	@Override
	public State clone(){
		State c = new State();
		c.transitionOccurrences = (Map<String, Integer>) ((HashMap<String,Integer>) this.transitionOccurrences).clone();
		c.dataItemTouches = (Map<String, Integer>) ((HashMap<String,Integer>) this.dataItemTouches).clone();
		c.hashCode = hashCode();
		return c;
	}
	
	@Override
	public boolean equals(Object o){
		if (!(o instanceof State)){
			return false;
		}
		State o2 = (State) o;
		if (id == o2.id){
			return true;
		}
		if (hashCode() != o2.hashCode()){
			return false;
		}
		if ((transitionOccurrences.size() != o2.transitionOccurrences.size()) || (dataItemTouches.size() != o2.dataItemTouches.size())){
			return false;
		}	
		for (Map.Entry<String, Integer> me: transitionOccurrences.entrySet()){
			if (me.getValue() != o2.transitionOccurrences.get(me.getKey())){
				return false;
			}
		}
		for (Map.Entry<String, Integer> me: dataItemTouches.entrySet()){
			if (me.getValue() != o2.dataItemTouches.get(me.getKey())){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode(){
		if (hashCode != null){
			return hashCode;
		}
		List<String> elements = new ArrayList<String>();
		elements.addAll(transitionOccurrences.keySet());
		elements.addAll(dataItemTouches.keySet());
		Collections.sort(elements);
		hashCode = String.join("", elements).hashCode();
		return hashCode;
	}

	/**
	 * Adds the given transition as the last transition into the state. This affects:
	 * 1. the transitions that are considered to have occurred before this state (i.e. occurrences(transition)++)
	 * 2. the enabling sequences into this state (i.e. for each sequence in enablingSequences: sequence = sequence.add(transition) 
	 * 
	 * @param transition the transition to add to this state.
	 */
	public void addTransition(String transition){
		Integer nrOccurrences = transitionOccurrences.get(transition);
		if (nrOccurrences == null){
			transitionOccurrences.put(transition, 1);
		}else{
			transitionOccurrences.put(transition, nrOccurrences+1);			
		}
		hashCode = null;
	}
	
	/**
	 * Increases the number of touches of the given data item before this state by 1. 
	 * 
	 * @param dataItem the data item that has has been touched once more before this state.
	 */
	public void addDataItem(String dataItem){
		Integer nrIterations = dataItemTouches.get(dataItem);
		if (nrIterations == null){
			dataItemTouches.put(dataItem, 1);
		}else{
			dataItemTouches.put(dataItem, nrIterations+1);			
		}		
		hashCode = null;
	}
	
	/**
	 * Returns the number of touches of a particular data item 
	 * 
	 * @param dataItem a data item
	 * @return the number of times the data item is touched
	 */
	public int dataItemTouches(String dataItem){
		Integer nr = dataItemTouches.get(dataItem);
		return (nr == null)?0:nr;
	}
	
	/**
	 * Returns the number of times a particular transition has occurred before reaching this state.
	 * 
	 * @param transition a transition
	 * @return the number of times a transition has occurred before reaching this state
	 */
	public int transitionOccurrences(String transition){
		Integer nr = transitionOccurrences.get(transition);
		return (nr == null)?0:nr;		
	}
	
	@Override
	public String toString(){
		String result = "{id: " + getId() + ", ";
		result += "occurrences: [";
		for (Iterator<Map.Entry<String,Integer>> i = transitionOccurrences.entrySet().iterator(); i.hasNext();){
			Map.Entry<String,Integer> me = i.next();
			result += me.getKey() + ":" + me.getValue();
			result += (i.hasNext())?", ":"";
		}
		result += "], touches: [";
		for (Iterator<Map.Entry<String,Integer>> i = dataItemTouches.entrySet().iterator(); i.hasNext();){
			Map.Entry<String,Integer> me = i.next();
			result += me.getKey() + ":" + me.getValue();
			result += (i.hasNext())?", ":"";
		}
		result += "]}";
		return result;
	}
	
	public int getId(){
		return id;
	}
	
	public static void resetNextId() {
		nextId = 0;
	}
}
