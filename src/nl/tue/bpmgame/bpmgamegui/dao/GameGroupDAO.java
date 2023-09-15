package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import nl.tue.bpmgame.dataaccess.model.GameGroup;

public interface GameGroupDAO {

    public void create(GameGroup u);     
    public List<GameGroup> list();
    public void delete(long id);
    public GameGroup read(long id);
    public GameGroup findByName(String email);
    public void update(GameGroup g);
}
