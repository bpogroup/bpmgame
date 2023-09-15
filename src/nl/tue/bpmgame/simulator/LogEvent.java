package nl.tue.bpmgame.simulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LogEvent {

	String caseID;
	String nodeLabel;
	long startTime;
	long completionTime;
	String resourceName;
	Map<String,String> attributeXValue;
	
	public LogEvent(String caseID, String nodeLabel, long startTime, long completionTime, String resourceName) {
		this.caseID = caseID;
		this.nodeLabel = nodeLabel;
		this.startTime = startTime;
		this.completionTime = completionTime;
		this.resourceName = resourceName;
		attributeXValue = new HashMap<String,String>();
	}
	
	public String getCaseID() {
		return caseID;
	}
	public void setCaseID(String caseID) {
		this.caseID = caseID;
	}
	public String getNodeLabel() {
		return nodeLabel;
	}
	public void setNodeLabel(String nodeLabel) {
		this.nodeLabel = nodeLabel;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getCompletionTime() {
		return completionTime;
	}
	public void setCompletionTime(long completionTime) {
		this.completionTime = completionTime;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public void setAttributeValue(String attributeName, String attributeValue) {
		attributeXValue.put(attributeName, attributeValue);
	}
	public String getAttributeValue(String attributeName) {
		return attributeXValue.get(attributeName);
	}
	public Set<String> getAttributes() {
		return attributeXValue.keySet();
	}
}
