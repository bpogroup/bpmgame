package nl.tue.bpmgame.dataaccess.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(indexes = { @Index(columnList = "startTime") }) 
public class PersistentLogEvent {

	@Id
	@GeneratedValue
	private Long id;
	@ManyToOne
	private GameGroup gameGroup;
	private Long startTime;
	private Long completionTime;
	private String caseID;
	private String event;
	private String resource;
	
	@OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<LogAttribute> attributes = new ArrayList<LogAttribute>();
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public GameGroup getGroup() {
		return gameGroup;
	}
	public void setGroup(GameGroup gameGroup) {
		this.gameGroup = gameGroup;
	}
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getCompletionTime() {
		return completionTime;
	}
	public void setCompletionTime(Long completionTime) {
		this.completionTime = completionTime;
	}
	public String getCaseID() {
		return caseID;
	}
	public void setCaseID(String caseID) {
		this.caseID = caseID;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public void addAttribute(LogAttribute attribute) {
		attributes.add(attribute);		
	}
	public List<LogAttribute> getAttributes(){
		return attributes;
	}
	
}
