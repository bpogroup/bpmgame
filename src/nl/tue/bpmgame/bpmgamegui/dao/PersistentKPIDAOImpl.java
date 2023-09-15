package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.PersistentKPI;

@Repository("persistentKPIDAO")
@Transactional
public class PersistentKPIDAOImpl extends AbstractDao implements PersistentKPIDAO {

	@Override
	public void create(PersistentKPI m) {
		super.create(m);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PersistentKPI> list() {
		return (List<PersistentKPI>) super.list(PersistentKPI.class);
	}

	@Override
	public void delete(long id) {
		super.delete(PersistentKPI.class, id);
	}

	@Override
	public PersistentKPI read(long id) {
		return (PersistentKPI) super.read(PersistentKPI.class, id);
	}

	@Override
	public void update(PersistentKPI m) {
		super.update(m);
	}
	
	@Override
	public List<PersistentKPI> allForGroup(GameGroup group, long toDay) {
		Session s = getSession();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<PersistentKPI> criteriaQuery = builder.createQuery(PersistentKPI.class);
        Root<PersistentKPI> root = criteriaQuery.from(PersistentKPI.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.and(
        					builder.equal(root.get("gameGroup"), group),
        					builder.lessThanOrEqualTo(root.get("logTime"), toDay)
        					));
        criteriaQuery.orderBy(builder.asc(root.get("logTime")));
        return s.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<PersistentKPI> onDayOrdered(long day) {
		Session s = getSession();
		
		long lastDay = day;
		
		//last measurement day
		CriteriaBuilder dayBuilder = s.getCriteriaBuilder();
		CriteriaQuery<PersistentKPI> dayQuery = dayBuilder.createQuery(PersistentKPI.class);
        Root<PersistentKPI> dayRoot = dayQuery.from(PersistentKPI.class);
        dayQuery.select(dayRoot);
        dayQuery.where(dayBuilder.lessThanOrEqualTo(dayRoot.get("logTime"), day));
        dayQuery.orderBy(dayBuilder.desc(dayRoot.get("logTime")));
        try {
        	lastDay = s.createQuery(dayQuery).getResultList().get(0).getTime();
        }catch (Exception e) {}

        //measurements on that day
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<PersistentKPI> criteriaQuery = builder.createQuery(PersistentKPI.class);
        Root<PersistentKPI> root = criteriaQuery.from(PersistentKPI.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("logTime"), lastDay));
        criteriaQuery.orderBy(builder.desc(root.get("totalScore")));
        return s.createQuery(criteriaQuery).getResultList();
	}

}
