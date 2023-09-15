package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.Model;

public interface ModelDAO {

    public void create(Model u);     
    public List<Model> list();
    public void delete(long id);
    public Model read(long id);
    public List<Model> listByGroup(GameGroup group);
    public Model findActiveByGroup(GameGroup group);
    public void update(Model g);
}
