package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.PersistentLogEvent;

public interface PersistentLogEventDAO {

    public void create(PersistentLogEvent u);     
    public List<PersistentLogEvent> list();
    public void delete(long id);
    public PersistentLogEvent read(long id);
    public List<PersistentLogEvent> listByGroup(GameGroup group);
    public List<PersistentLogEvent> listByGroupOnDay(GameGroup group, long day);
    public void update(PersistentLogEvent g);
}
