package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.Model;

@Repository("modelDAO")
@Transactional
public class ModelDAOImpl extends AbstractDao implements ModelDAO {

	@Override
	public void create(Model m) {
		super.create(m);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Model> list() {
		return (List<Model>) super.list(Model.class);
	}

	@Override
	public void delete(long id) {
		super.delete(Model.class, id);
	}

	@Override
	public Model read(long id) {
		return (Model) super.read(Model.class, id);
	}

	@Override
	public void update(Model m) {
		super.update(m);
	}
	
	@Override
	public List<Model> listByGroup(GameGroup group) {
		Session s = getSession();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<Model> criteriaQuery = builder.createQuery(Model.class);
        Root<Model> root = criteriaQuery.from(Model.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("gameGroup"), group));
        return s.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public Model findActiveByGroup(GameGroup group) {
		Session s = getSession();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<Model> criteriaQuery = builder.createQuery(Model.class);
        Root<Model> root = criteriaQuery.from(Model.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.and(
        		builder.equal(root.get("gameGroup"), group),
        		builder.equal(root.get("isActive"), true)
        		));
        Model result = null;
        try {
        	result = s.createQuery(criteriaQuery).getSingleResult();
        } catch (Exception e) {}
        return result;        
	}

}
