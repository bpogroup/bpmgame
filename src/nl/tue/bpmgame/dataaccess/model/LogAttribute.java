package nl.tue.bpmgame.dataaccess.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class LogAttribute {

	@Id
	@GeneratedValue
	private Long id;
	private String attributeName;
	private String attributeValue;

	@ManyToOne
	private PersistentLogEvent event;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public String getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
	public PersistentLogEvent getEvent() {
		return event;
	}
	public void setEvent(PersistentLogEvent event) {
		this.event = event;
	}

}
