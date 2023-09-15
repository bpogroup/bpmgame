package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.PersistentKPI;

public interface PersistentKPIDAO {

    public void create(PersistentKPI u);     
    public List<PersistentKPI> list();
    public void delete(long id);
    public PersistentKPI read(long id);
    /**
     * Returns the list of KPI's for all groups measured last on or before the given day, ordered by total score (descending). 
     * 
     * @param day 00:00 on the day in milliseconds since 1 jan 1970
     * @return a list of KPI's
     */
    public List<PersistentKPI> onDayOrdered(long day);
    /**
     * Returns the list of all KPI's for the given group, until and including the specified day, ordered by logTime (ascending).
     * 
     * @param group a group
     * @return a list of KPI's
     */
    public List<PersistentKPI> allForGroup(GameGroup group, long toDay);
    public void update(PersistentKPI g);
}
