package nl.tue.bpmn.concepts;

public class BPMNArc {
	String condition;
	BPMNNode source;
	BPMNNode target;

	public BPMNArc(){
	}
	
	public BPMNArc(String condition, BPMNNode source, BPMNNode target) {
		this();
		this.condition =  condition;
		this.source = source;
		this.target = target;
	}
	
	public BPMNArc(String condition) {
		this();
		this.condition = condition;
	}

	public String getCondition() {
		if (condition == null){
			return "";
		}
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public BPMNNode getSource() {
		return source;
	}
	public void setSource(BPMNNode source) {
		this.source = source;
	}
	public BPMNNode getTarget() {
		return target;
	}
	public void setTarget(BPMNNode target) {
		this.target = target;
	}
	
	@Override
	public String toString(){
		String result = "";
		result += source.toString() + " -" + ((condition!=null)?condition:"") + "-> " + target.toString();
		return result;
	}
}
