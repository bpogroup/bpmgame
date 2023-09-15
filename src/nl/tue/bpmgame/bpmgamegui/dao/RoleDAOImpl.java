package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import nl.tue.bpmgame.dataaccess.model.Role;

@Repository("roleDAO")
@Transactional
public class RoleDAOImpl extends AbstractDao implements RoleDAO{

	@Override
	public void create(Role r) {
		super.create(r);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Role> list() {
		return (List<Role>) super.list(Role.class);
	}

	@Override
	public void delete(long id) {
		super.delete(Role.class, id);
	}

	@Override
	public Role read(long id) {
		return (Role) super.read(Role.class, id);
	}

	@Override
	public void update(Role r) {
		super.update(r);
	}

	@Override
	public Role findRoleByName(String name) {
		Session s = getSession();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<Role> criteriaQuery = builder.createQuery(Role.class);
        Root<Role> root = criteriaQuery.from(Role.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("name"), name));
        Role result = null;
        try {
        	result = s.createQuery(criteriaQuery).getSingleResult();
        }catch (Exception e) {}
        return result;
	}
	
}
