package nl.tue.bpmgame.dataaccess.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;

import nl.tue.bpmgame.dataaccess.model.PersistentKPI;

public class PersistentKPIDAO extends DAO<PersistentKPI> {

	public PersistentKPIDAO() {
		super(PersistentKPI.class);
	}
	
	public List<PersistentKPI> kpisAtTime(Long time){
		Session s = getSession();
		Transaction t = s.beginTransaction();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<PersistentKPI> criteriaQuery = builder.createQuery(tClass);
        Root<PersistentKPI> root = criteriaQuery.from(tClass);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("logTime"), time));
        List<PersistentKPI> result = s.createQuery(criteriaQuery).getResultList();
		t.commit();
		return result;
	}

}
