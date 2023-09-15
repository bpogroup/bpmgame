package nl.tue.bpmn.concepts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.tue.bpmn.concepts.BPMNNode.NodeType;

/**
 * BPMNRole has a relation to contained nodes and to resources that can act in this role.
 * Note that:
 * 1. a resource may be able to act in multiple roles
 * 2. the list of resources that can act in a role is ordered, 
 *    a resource that is more to the front of the list will be preferred in executing a task for the role  
 */
public class BPMNRole {

	private String name;
	private Set<BPMNNode> containedNodes;
	private List<BPMNNode> containedTasks; //ordered, so we can select one randomly 	
	private List<String> resources;
	
	public BPMNRole(String name){
		this.name = name;
		containedNodes = new HashSet<BPMNNode>();
		resources = new ArrayList<String>();
		containedTasks = null;
	}
	
	public Set<BPMNNode> getContainedNodes(){
		return containedNodes;
	}
	
	public List<BPMNNode> getContainedTasks(){
		if (containedTasks == null) {
			containedTasks = new ArrayList<BPMNNode>();
			for (BPMNNode node: containedNodes) {
				if (node.getType() == NodeType.Task) {
					containedTasks.add(node);
				}
			}
		}
		return containedTasks;
	}
	
	public void addContainedNode(BPMNNode node){
		containedNodes.add(node);
		containedTasks = null;
	}
	
	public List<String> getResources(){
		return resources;
	}
	
	public void addResource(String resource){
		this.resources.add(resource);
	}
	
	public String getName(){
		return name;
	}
}
