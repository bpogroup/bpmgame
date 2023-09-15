package nl.tue.bpmgame.executionmodel;

import java.util.HashSet;
import java.util.Set;

import nl.tue.bpmn.concepts.BPMNArc;

public class BPMNState extends State{

	private Set<BPMNArc> markedArcs;
	
	public BPMNState(){
		markedArcs = new HashSet<BPMNArc>();		
	}
	
	public void addMarking(BPMNArc arc){
		markedArcs.add(arc);
	}
	
	public void removeMarking(BPMNArc arc){
		markedArcs.remove(arc);
	}
	
	public void addMarking(Set<BPMNArc> arcs){
		markedArcs.addAll(arcs);
	}
	
	public void removeMarking(Set<BPMNArc> arcs){
		markedArcs.removeAll(arcs);
	}
	
	@Override
	public BPMNState clone(){
		BPMNState clone = (BPMNState) super.clone();
		clone.markedArcs = new HashSet<BPMNArc>();
		clone.markedArcs.addAll(markedArcs);
		return clone;
	}
	
	public Set<BPMNArc> getMarkedArcs(){
		return markedArcs;
	}
}
