package nl.tue.bpmgame.dataaccess.dao;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;

import nl.tue.bpmgame.dataaccess.config.ApplicationContext;

public class DAO<T> {

	final Class<T> tClass; 
	
	public DAO(Class<T> tClass){
		this.tClass=tClass;
	}
	
	public void create(T e){
		Session s = getSession();
		Transaction t = s.beginTransaction();
		s.persist(e);
		t.commit();		
	}

	public List<T> list(){
		Session s = getSession();
		Transaction t = s.beginTransaction();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(tClass);
        Root<T> root = criteriaQuery.from(tClass);
        criteriaQuery.select(root);
        List<T> result = s.createQuery(criteriaQuery).getResultList();
		t.commit();
		return result;
	}
	
	public void delete(long id){
		Session s = getSession();		
		Transaction t = s.beginTransaction();
		T item = s.get(tClass, id);
		if (item != null) {
			s.delete(item);
		}
		t.commit();
	}
	
	public void clear() {
		Session s = getSession();		
		Transaction t = s.beginTransaction();
		Query query = s.createQuery("DELETE FROM " + tClass.getSimpleName());
		query.executeUpdate();
		t.commit();		
	}
	
	public T read(long id){
		Session s = getSession();
		Transaction t = s.beginTransaction();
		T result = s.get(tClass, id);
		t.commit();
		return result;
	}
	
	public T findByAttribute(String attribute, Object value){
		Session s = getSession();
		Transaction t = s.beginTransaction();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(tClass);
        Root<T> root = criteriaQuery.from(tClass);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get(attribute), value));        
		T result = null;
		try {
			result = s.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {}
		t.commit();
		return result;
	}

	public List<T> listByAttribute(String attribute, Object value){
		Session s = getSession();
		Transaction t = s.beginTransaction();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(tClass);
        Root<T> root = criteriaQuery.from(tClass);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get(attribute), value));
		List<T> result = s.createQuery(criteriaQuery).getResultList();
		t.commit();
		return result;
	}

	public void update(T e){
		Session s = getSession();
		Transaction t = s.beginTransaction();
		s.update(e);
		t.commit();
	}
	
	protected Session getSession(){
		return ApplicationContext.getApplicationContext().getSessionFactory().getCurrentSession();
	}
}