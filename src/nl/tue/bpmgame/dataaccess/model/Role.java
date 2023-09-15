package nl.tue.bpmgame.dataaccess.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Role {
	
	@Transient public static final String ROLE_ADMIN = "ROLE_ADMIN";
	@Transient public static final String ROLE_USER = "ROLE_USER";

	//Fields
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	
	//Getters/Setters
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}