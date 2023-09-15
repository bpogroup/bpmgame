package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import nl.tue.bpmgame.dataaccess.model.Role;

public interface RoleDAO {

    public void create(Role r);     
    public List<Role> list();
    public void delete(long id);
    public Role read(long id);
    public void update(Role r);
    public Role findRoleByName(String name);
}
