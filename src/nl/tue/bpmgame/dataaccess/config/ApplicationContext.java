package nl.tue.bpmgame.dataaccess.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.LogAttribute;
import nl.tue.bpmgame.dataaccess.model.Model;
import nl.tue.bpmgame.dataaccess.model.PersistentKPI;
import nl.tue.bpmgame.dataaccess.model.PersistentLogEvent;
import nl.tue.bpmgame.dataaccess.model.User;
import nl.tue.bpmgame.dataaccess.model.Role;

public class ApplicationContext {
	@SuppressWarnings("rawtypes")
	private static final Class[] ANNOTATED_CLASSES = {GameGroup.class, LogAttribute.class, Model.class, PersistentKPI.class, PersistentLogEvent.class, User.class, Role.class}; //Add all POJO classes here as <classname>.class

	private static ApplicationContext singleton;
	private SessionFactory sessionFactory;
	private boolean createDatabase = false;

	public static ApplicationContext getApplicationContext(){
		if (singleton == null){
			singleton = new ApplicationContext();
		}
		return singleton;
	}
	
	public static void initializeNewDatabase() {
		if (singleton != null) {
			System.err.println("ERROR: Initializing a new database must be done before opening it (using getApplicationContext).");
			System.exit(1);
		}
		singleton = new ApplicationContext();
		singleton.createDatabase = true;
	}

	public SessionFactory getSessionFactory(){
		if (sessionFactory == null){
			try{
				Configuration configuration = new Configuration()
						.setProperty("hibernate.dialect","org.hibernate.dialect.MySQL57Dialect")
						.setProperty("hibernate.connection.driver_class","com.mysql.jdbc.Driver")
						.setProperty("hibernate.connection.url","jdbc:mysql://localhost:3306/bpmgame?useSSL=false")
						.setProperty("hibernate.default_schema","bpmgame")							
						.setProperty("hibernate.connection.username","root")
						.setProperty("hibernate.connection.password","toor")
						.setProperty("hibernate.current_session_context_class","thread");
				if (createDatabase) {
					configuration.setProperty("hibernate.hbm2ddl.auto","create");
				}
				for (@SuppressWarnings("rawtypes") Class aclass: ANNOTATED_CLASSES){
					configuration.addAnnotatedClass(aclass);
				}
				StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
				sessionFactory = configuration.buildSessionFactory(serviceRegistry);
			}catch (Exception e) {
				System.err.println("Could not create sessionFactory. Caused by:\n");
				e.printStackTrace(System.err);
				System.exit(1);
			}
		} 
		return sessionFactory;
	}
		
	public void close(){
		sessionFactory.close();
	}
}
