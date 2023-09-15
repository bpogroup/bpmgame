package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.PersistentLogEvent;

@Repository("modelDAO")
@Transactional
public class PersistentLogEventDAOImpl extends AbstractDao implements PersistentLogEventDAO {

	@Override
	public void create(PersistentLogEvent m) {
		super.create(m);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PersistentLogEvent> list() {
		return (List<PersistentLogEvent>) super.list(PersistentLogEvent.class);
	}

	@Override
	public void delete(long id) {
		super.delete(PersistentLogEvent.class, id);
	}

	@Override
	public PersistentLogEvent read(long id) {
		return (PersistentLogEvent) super.read(PersistentLogEvent.class, id);
	}

	@Override
	public void update(PersistentLogEvent m) {
		super.update(m);
	}
	
	@Override
	public List<PersistentLogEvent> listByGroup(GameGroup group) {
		Session s = getSession();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<PersistentLogEvent> criteriaQuery = builder.createQuery(PersistentLogEvent.class);
        Root<PersistentLogEvent> root = criteriaQuery.from(PersistentLogEvent.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("gameGroup"), group));
        return s.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<PersistentLogEvent> listByGroupOnDay(GameGroup group, long day) {
		Session s = getSession();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<PersistentLogEvent> criteriaQuery = builder.createQuery(PersistentLogEvent.class);
        Root<PersistentLogEvent> root = criteriaQuery.from(PersistentLogEvent.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.and(
        		builder.equal(root.get("gameGroup"), group),
        		builder.greaterThan(root.get("completionTime"), day)
        		));
        return s.createQuery(criteriaQuery).getResultList();
	}

}
