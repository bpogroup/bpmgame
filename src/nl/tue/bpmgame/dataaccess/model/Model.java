package nl.tue.bpmgame.dataaccess.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
public class Model {

	@Id
	@GeneratedValue
	private Long id;
	@Lob
	private String xml;
	@ManyToOne
	private GameGroup gameGroup;
	@ManyToOne
	private User uploader;
	private Date deployedOn;
	private boolean isActive;
	private boolean isSafe;
	private String fileName;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	public GameGroup getGroup() {
		return gameGroup;
	}
	public void setGroup(GameGroup gameGroup) {
		this.gameGroup = gameGroup;
	}
	public User getUploader() {
		return uploader;
	}
	public void setUploader(User user) {
		this.uploader = user;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public boolean isSafe() {
		return isSafe;
	}
	public void setSafe(boolean isSafe) {
		this.isSafe = isSafe;
	}
	public void setDeployedOn(Date date) {
		deployedOn = date;
	}
	public Date getDeployedOn() {
		return deployedOn; 
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileName() {
		return fileName;
	}
}
