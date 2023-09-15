package nl.tue.bpmgame.bpmgamegui.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import nl.tue.bpmgame.dataaccess.model.User;

@Repository("userDAO")
@Transactional
public class UserDAOImpl extends AbstractDao implements UserDAO {

	@Override
	public void create(User u) {
		super.create(u);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> list() {
		return (List<User>) super.list(User.class);
	}

	@Override
	public void delete(long id) {
		super.delete(User.class, id);
	}

	@Override
	public User read(long id) {
		return (User) super.read(User.class, id);
	}

	@Override
	public void update(User r) {
		super.update(r);
	}

	@Override
	public User findByEmail(String email) {
		Session s = getSession();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("email"), email));
        User result = null;
        try {
        	result = s.createQuery(criteriaQuery).getSingleResult();
        } catch (Exception e) {}
        return result;
	}	
	
	@Override
	public User findByUid(String uid) {
		Session s = getSession();
		CriteriaBuilder builder = s.getCriteriaBuilder();
		CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("uid"), uid));
        User result = null;
        try {
        	result = s.createQuery(criteriaQuery).getSingleResult();
        } catch (Exception e) {}
        return result;        
	}
	
}
