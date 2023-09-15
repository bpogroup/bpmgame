package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import nl.tue.bpmgame.dataaccess.model.GameGroup;

@Repository("gameGroupDAO")
@Transactional
public class GameGroupDAOImpl extends AbstractDao implements GameGroupDAO {

	@Override
	public void create(GameGroup g) {
		super.create(g);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GameGroup> list() {
		return (List<GameGroup>) super.list(GameGroup.class);
	}

	@Override
	public void delete(long id) {
		super.delete(GameGroup.class, id);
	}

	@Override
	public GameGroup read(long id) {
		return (GameGroup) super.read(GameGroup.class, id);
	}

	@Override
	public void update(GameGroup g) {
		super.update(g);
	}
	
	@Override
	public GameGroup findByName(String name) {
		Session s = getSession();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<GameGroup> criteriaQuery = builder.createQuery(GameGroup.class);
        Root<GameGroup> root = criteriaQuery.from(GameGroup.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("name"), name));
        GameGroup result = null;
        try {
        	result = s.createQuery(criteriaQuery).getSingleResult();
        } catch (Exception e) {}
        return result;        
	}
	
}
