package nl.tue.bpmn.concepts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BPMNModel {

	private Set<BPMNArc> arcs;
	private Set<BPMNNode> nodes;
	private Set<BPMNRole> roles;
	private BPMNNode startEvent;
	
	private Map<String, BPMNNode> label2Node;
	private Map<String, BPMNRole> name2Role;
	
	private Map<BPMNNode, BPMNRole> node2Role;
	
	public BPMNModel(){
		arcs = new HashSet<BPMNArc>();
		nodes = new HashSet<BPMNNode>();
		roles = new HashSet<BPMNRole>();
		
		label2Node = new HashMap<String,BPMNNode>();
		name2Role = new HashMap<String,BPMNRole>();
		
		node2Role = new HashMap<BPMNNode, BPMNRole>();
	}
	
	public Set<BPMNArc> getArcs() {
		return arcs;
	}
	
	public Set<BPMNNode> getNodes() {
		return nodes;
	}
	
	public Set<BPMNRole> getRoles() {
		return roles;
	}

	public void addRole(BPMNRole role){
		roles.add(role);
	}
	
	public void addNode(BPMNNode node){
		nodes.add(node);
	}
	
	public void addArc(BPMNArc arc){
		arcs.add(arc);
	}

	public BPMNNode getStartEvent() {
		return startEvent;
	}

	public void setStartEvent(BPMNNode startEvent) {
		this.startEvent = startEvent;
	}

	public BPMNNode getNode(String label) {
		BPMNNode result = label2Node.get(label);
		if (result == null) {
			for (BPMNNode node: nodes) {
				if (node.getName().equals(label)) {
					result = node;
					break;
				}
			}
			label2Node.put(label, result);
		}
		return result;
	}

	public BPMNRole getRole(String name) {
		BPMNRole result = name2Role.get(name);
		if (result == null) {
			for (BPMNRole role: roles) {
				if (role.getName().equals(name)) {
					result = role;
					break;
				}
			}
			name2Role.put(name, result);
		}
		return result;
	}
	
	public BPMNRole getRoleFor(BPMNNode node) {
		BPMNRole result = node2Role.get(node);
		if (result == null) {
			for (BPMNRole role: roles) {
				if (role.getContainedNodes().contains(node)) {
					result = role;
					break;
				}
			}
			node2Role.put(node, result);
		}
		return result;
	}
}
