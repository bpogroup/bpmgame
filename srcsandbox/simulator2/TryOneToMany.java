package simulator2;

import nl.tue.bpmgame.dataaccess.config.ApplicationContext;
import nl.tue.bpmgame.dataaccess.model.LogAttribute;
import nl.tue.bpmgame.dataaccess.model.PersistentLogEvent;
import nl.tue.bpmgame.dataaccess.service.DAOService;

public class TryOneToMany {

	public static void main(String[] args) {
		ApplicationContext.initializeNewDatabase();
		
		PersistentLogEvent ple = new PersistentLogEvent();
		ple.setEvent("test ple");
		DAOService.persistentLogEventDAO().create(ple);
				
		LogAttribute la = new LogAttribute();
		la.setAttributeName("test la");
		DAOService.logAttributeDAO().create(la);		

		la.setEvent(ple);		
		DAOService.logAttributeDAO().update(la);
		ple.addAttribute(la);
		DAOService.persistentLogEventDAO().update(ple);

		ple.getAttributes().remove(0);
		DAOService.persistentLogEventDAO().update(ple);
	}

}
