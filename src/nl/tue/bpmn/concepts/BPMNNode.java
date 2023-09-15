package nl.tue.bpmn.concepts;

import java.util.HashSet;
import java.util.Set;

public class BPMNNode {
	
	public enum NodeType {Task, EventBasedGateway, ExclusiveSplit, ExclusiveJoin, ParallelSplit, ParallelJoin, StartEvent, EndEvent, IntermediateEvent};
	
	private String name;
	private NodeType type;
	private Set<BPMNArc> incoming;
	private Set<BPMNArc> outgoing;
	
	public BPMNNode(String name, NodeType type, Set<BPMNArc> incoming, Set<BPMNArc> outgoing) {
		this.name =  name;
		this.type = type;
		this.incoming = incoming;
		this.outgoing = outgoing;
	}
	public BPMNNode(String name, NodeType type) {
		this.name =  name;
		this.type = type;
		incoming = new HashSet<BPMNArc>();
		outgoing = new HashSet<BPMNArc>();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public NodeType getType() {
		return type;
	}
	public void setType(NodeType type) {
		this.type = type;
	}
	public Set<BPMNArc> getIncoming() {
		return incoming;
	}
	public void setIncoming(Set<BPMNArc> incoming) {
		this.incoming = incoming;
	}
	public Set<BPMNArc> getOutgoing() {
		return outgoing;
	}
	public void setOutgoing(Set<BPMNArc> outgoing) {
		this.outgoing = outgoing;
	}
	public void addIncoming(BPMNArc arc){
		this.incoming.add(arc);
	}
	public void addOutgoing(BPMNArc arc){
		this.outgoing.add(arc);
	}
	
	@Override
	public String toString(){
		String result = "";
		switch (type){
		case Task:
			result += "Task("+name+")";
			break;
		case EventBasedGateway:
			result += "Event-Split";
			break;
		case ExclusiveSplit:
			result += "XOR-Split";
			break;
		case ExclusiveJoin:
			result += "XOR-Join";
			break;
		case ParallelSplit:
			result += "AND-Split";
			break;
		case ParallelJoin:
			result += "AND-Join";
			break;
		case StartEvent:
			result += "StartEvent("+name+")";
			break;
		case EndEvent:
			result += "EndEvent("+name+")";
			break;
		case IntermediateEvent:
			result += "IntermediateEvent("+name+")";
			break;
		default:
		}
		return result;
	}
}
