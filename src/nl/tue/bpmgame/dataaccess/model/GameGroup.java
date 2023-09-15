package nl.tue.bpmgame.dataaccess.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(indexes = { @Index(columnList = "name") }) 
public class GameGroup {

	@Id
	@GeneratedValue
	private Long id;
	private String name;

	@OneToMany(mappedBy = "group")
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<User> users = new ArrayList<User>();

	@OneToMany(mappedBy = "gameGroup")
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<Model> models = new ArrayList<Model>();

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
	public void addUser(User user) {
		users.add(user);
	}
	public void removeUser(User user) {
		users.remove(user);
	}
	public List<User> getUsers() {
		return users;
	}
	public void addModel(Model model) {
		models.add(model);
	}
	public void removeModel(Model model) {
		models.remove(model);		
	}
	public List<Model> getModels(){
		return models;
	}
}
