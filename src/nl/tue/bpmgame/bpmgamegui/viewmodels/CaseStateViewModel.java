package nl.tue.bpmgame.bpmgamegui.viewmodels;

public class CaseStateViewModel implements Comparable<CaseStateViewModel> {

	private String caseid;
	private String busy;
	private String idle;
	private String complete;

	public CaseStateViewModel(String caseid, String busy, String idle, String complete) {
		super();
		this.caseid = caseid;
		this.busy = busy;
		this.idle = idle;
		this.complete = complete;
	}
	
	public String getCaseid() {
		return caseid;
	}
	public void setCaseid(String caseid) {
		this.caseid = caseid;
	}
	public String getBusy() {
		return busy;
	}
	public void setBusy(String busy) {
		this.busy = busy;
	}
	public String getIdle() {
		return idle;
	}
	public void setIdle(String idle) {
		this.idle = idle;
	}
	public String getComplete() {
		return complete;
	}
	public void setComplete(String complete) {
		this.complete = complete;
	}
	
	@Override	
	public int hashCode() {
		return caseid.hashCode();
	}
	@Override
	public int compareTo(CaseStateViewModel other) {
		return caseid.compareTo(((CaseStateViewModel)other).caseid);
	}
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CaseStateViewModel)) {
			return false;
		} else {
			return caseid.equals(((CaseStateViewModel)other).caseid);
		}
	}
}
