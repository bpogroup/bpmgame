package nl.tue.bpmgame.bpmgamegui.viewmodels;

public class TaskStateViewModel {
	private String task;
	private String caseid;
	private String resource;
	private Long active;
	
	public TaskStateViewModel(String task, String caseid, String resource, Long active) {
		super();
		this.task = task;
		this.caseid = caseid;
		this.resource = resource;
		this.active = active;
	}
	
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public String getCaseid() {
		return caseid;
	}
	public void setCaseid(String caseid) {
		this.caseid = caseid;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public Long getActive() {
		return active;
	}
	public void setActive(Long active) {
		this.active = active;
	}
}
