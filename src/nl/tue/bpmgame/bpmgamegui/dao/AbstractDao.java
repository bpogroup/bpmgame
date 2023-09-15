package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDao {

	@Autowired
	private SessionFactory annotationSessionFactory;

	protected Session getSession() {
		return annotationSessionFactory.getCurrentSession();
	}

	public void create(Object o) {
		Session s = getSession();
		s.persist(o);
	}

	@SuppressWarnings("unchecked")
	public List<?> list(Class<?> tClass) {
		Session s = getSession();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<?> criteriaQuery = builder.createQuery(tClass);
        @SuppressWarnings("rawtypes")
		Root root = criteriaQuery.from(tClass);
        criteriaQuery.select(root);
        List<?> result = s.createQuery(criteriaQuery).getResultList();
		return result;
	}

	public void delete(Class<?> tClass, long id) {
		Session s = getSession();		
		Object item = s.get(tClass, id);
		if (item != null) {
			s.delete(item);
		}
	}

	public Object read(Class<?> tClass, long id) {
		Session s = getSession();
		Object result = s.get(tClass, id);
		return result;
	}

	public void update(Object o) {
		Session s = getSession();
		s.update(o);
	}
}