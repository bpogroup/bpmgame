package nl.tue.bpmgame.dataaccess.service;

import nl.tue.bpmgame.dataaccess.dao.DAO;
import nl.tue.bpmgame.dataaccess.dao.ModelDAO;
import nl.tue.bpmgame.dataaccess.dao.PersistentKPIDAO;
import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.LogAttribute;
import nl.tue.bpmgame.dataaccess.model.PersistentLogEvent;
import nl.tue.bpmgame.dataaccess.model.Role;
import nl.tue.bpmgame.dataaccess.model.User;

public class DAOService {

	private DAO<User> userDAO;
	private DAO<Role> roleDAO;
	private DAO<GameGroup> gameGroupDAO;
	private ModelDAO modelDAO;
	private DAO<PersistentLogEvent> persistentLogEventDAO;
	private PersistentKPIDAO persistentKPIDAO;
	private DAO<LogAttribute> logAttributeDAO;
	
	private static DAOService singleton;
	
	private DAOService(){
		gameGroupDAO = new DAO<GameGroup>(GameGroup.class);
		modelDAO = new ModelDAO();
		persistentLogEventDAO = new DAO<PersistentLogEvent>(PersistentLogEvent.class);
		persistentKPIDAO = new PersistentKPIDAO();
		logAttributeDAO = new DAO<LogAttribute>(LogAttribute.class);
		userDAO = new DAO<User>(User.class);
		roleDAO = new DAO<Role>(Role.class);
	}
	
	private static DAOService getInstance(){
		if (singleton == null){
			singleton = new DAOService();
		}
		return singleton;
	}
	
	public static DAO<GameGroup> gameGroupDAO(){
		return getInstance().gameGroupDAO;
	}
	
	public static ModelDAO modelDAO(){
		return getInstance().modelDAO;
	}

	public static DAO<PersistentLogEvent> persistentLogEventDAO(){
		return getInstance().persistentLogEventDAO;
	}

	public static PersistentKPIDAO persistentKPIDAO(){
		return getInstance().persistentKPIDAO;
	}

	public static DAO<LogAttribute> logAttributeDAO(){
		return getInstance().logAttributeDAO;
	}

	public static DAO<User> userDAO(){
		return getInstance().userDAO;
	}

	public static DAO<Role> roleDAO(){
		return getInstance().roleDAO;
	}
}
