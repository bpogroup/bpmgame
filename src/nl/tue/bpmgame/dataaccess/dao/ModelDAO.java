package nl.tue.bpmgame.dataaccess.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;

import nl.tue.bpmgame.dataaccess.model.Model;

public class ModelDAO extends DAO<Model> {

	public ModelDAO() {
		super(Model.class);
	}

	public List<Model> activeModels(){
		Session s = getSession();
		Transaction t = s.beginTransaction();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<Model> criteriaQuery = builder.createQuery(tClass);
        Root<Model> root = criteriaQuery.from(tClass);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("isActive"), true));
        List<Model> result = s.createQuery(criteriaQuery).getResultList();
		t.commit();
		return result;
	}

	public List<Model> safeModels(){
		Session s = getSession();
		Transaction t = s.beginTransaction();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<Model> criteriaQuery = builder.createQuery(tClass);
        Root<Model> root = criteriaQuery.from(tClass);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("isSafe"), true));
        List<Model> result = s.createQuery(criteriaQuery).getResultList();
		t.commit();
		return result;
	}
}
