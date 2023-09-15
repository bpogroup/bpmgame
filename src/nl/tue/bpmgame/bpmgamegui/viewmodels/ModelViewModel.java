package nl.tue.bpmgame.bpmgamegui.viewmodels;

import java.util.Date;

public class ModelViewModel {

	private long id;
	private String fileName;
	private String uploaderName;
	private Date deployedOn;
		
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUploaderName() {
		return uploaderName;
	}
	public void setUploaderName(String uploaderName) {
		this.uploaderName = uploaderName;
	}
	public Date getDeployedOn() {
		return deployedOn;
	}
	public void setDeployedOn(Date deployedOn) {
		this.deployedOn = deployedOn;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
}
