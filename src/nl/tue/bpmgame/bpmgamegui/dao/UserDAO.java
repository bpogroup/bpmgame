package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import nl.tue.bpmgame.dataaccess.model.User;

public interface UserDAO {

    public void create(User u);     
    public List<User> list();
    public void delete(long id);
    public User read(long id);
    public User findByEmail(String email);
    public User findByUid(String uid);
    public void update(User u);
}
