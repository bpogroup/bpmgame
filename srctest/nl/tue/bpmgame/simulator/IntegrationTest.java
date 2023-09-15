package nl.tue.bpmgame.simulator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.dataaccess.config.ApplicationContext;
import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.Model;
import nl.tue.bpmgame.dataaccess.model.PersistentLogEvent;
import nl.tue.bpmgame.dataaccess.service.DAOService;
import nl.tue.bpmn.concepts.BPMNModel;
import nl.tue.bpmn.concepts.BPMNNode;
import nl.tue.bpmn.concepts.BPMNRole;
import nl.tue.bpmn.concepts.BPMNNode.NodeType;
import nl.tue.bpmn.parser.BPMNParseException;
import nl.tue.bpmn.parser.BPMNParser;
import nl.tue.util.TimeUtils;

public class IntegrationTest {

	public static String checkModel(String modelPath) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, BPMNParseException {
		byte[] encoded = Files.readAllBytes(Paths.get(modelPath));
		String xml = new String(encoded);
		xml = xml.replaceAll("start event", "start");
		xml = xml.replaceAll("end event", "end");
		xml = xml.replaceAll("perform automated credit check", "perform credit check");
		xml = xml.replaceAll("perform automated BKR check", "perform BKR check");
		xml = xml.replaceAll("perform automated EVA check", "perform EVA check");
		xml = xml.replaceAll("perform automated credibility check", "perform credibility check");
		xml = xml.replaceAll("perform manual credit check", "perform credit check");
		xml = xml.replaceAll("perform manual BKR check", "perform BKR check");
		xml = xml.replaceAll("perform manual EVA check", "perform EVA check");
		xml = xml.replaceAll("perform manual credibility check", "perform credibility check");
		xml = xml.replaceAll("ClientPickedUp", "PickedUp");
		xml = xml.replaceAll("bkrAccepted", "accepted");
		xml = xml.replaceAll("bkrRejected", "rejected");
		xml = xml.replaceAll("evaAccepted", "accepted");
		xml = xml.replaceAll("evaRejected", "rejected");
		xml = xml.replaceAll("\\{true\\}", "{pickedup}");
		xml = xml.replaceAll("\\{false\\}", "{notpickedup}");
		
		//Add general manager as resource to each lane
        String textToInsert = "\n<documentation>" + Assignment.GENERAL_MANAGER + ",Mose,Jerome</documentation>";
		Pattern pattern = Pattern.compile("<lane [^>]*>");
        Matcher matcher = pattern.matcher(xml);
        int shift = 0;
        while(matcher.find()) {
        		xml = xml.substring(0, matcher.end() + shift) +  
        			  textToInsert + 
        			  xml.substring(matcher.end() + shift);
        		shift += textToInsert.length();
        }
        
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.AssignmentLoan");
		
		BPMNParser parser = new BPMNParser();
		parser.parse(xml);
		BPMNModel bpmnModel = parser.getParsedModel();

		//Check that the nodes are there
		for (BPMNNode node: bpmnModel.getNodes()) {
			String label = node.getName();
			if (node.getType() == NodeType.Task) {
				if (Assignment.getAssignment().getTask(label) == null) {
					System.out.println("Task " + label + " not found.");
				}
			}else if ((node.getType() == NodeType.StartEvent) || (node.getType() == NodeType.EndEvent) || (node.getType() == NodeType.IntermediateEvent)) {
				if (Assignment.getAssignment().getEvent(label) == null) {
					System.out.println("Event " + label + " not found.");
				}				
			}
		}
		
		//Check that the resources in the roles are there
		for (BPMNRole role: bpmnModel.getRoles()) {
			for (String r: role.getResources()) {
				if (Assignment.getAssignment().getResource(r) == null) {
					System.out.println("Role " + role.getName() + " contains undefined resource: " + r);					
				}
			}
		}
		
		return xml;
	}

	public static void addModel(String groupName, String xml)  throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, BPMNParseException {						
		GameGroup group = new GameGroup();
		group.setName(groupName);
		DAOService.gameGroupDAO().create(group);
		
		Model model = new Model();
		model.setActive(true);
		model.setSafe(false);
		model.setGroup(group);
		model.setXml(xml);
		DAOService.modelDAO().create(model);
	}
	
	public static void printResults(String groupName) {
		//PersistentKPI pkpi = DAOService.persistentKPIDAO().list().get(0);
		List<PersistentLogEvent> log = DAOService.persistentLogEventDAO().listByAttribute("gameGroup", DAOService.gameGroupDAO().findByAttribute("name", groupName));		
		
		for (PersistentLogEvent ple: log) {
			System.out.print(ple.getCaseID());
			System.out.print(",");
			System.out.print(ple.getEvent());
			System.out.print(",");
			System.out.print(TimeUtils.formatTime(ple.getStartTime()));
			System.out.print(",");
			System.out.print(TimeUtils.formatTime(ple.getCompletionTime()));
			System.out.print(",");
			System.out.print((ple.getResource()==null)?"":ple.getResource());
			
			System.out.println();
		}
	}

	public static void main(String args[]) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, BPMNParseException {		
		ApplicationContext.initializeNewDatabase();
		addModel("Group 1", checkModel("./ext/studentmodels/model 01.bpmn"));
		addModel("Group 2", checkModel("./ext/studentmodels/model 02.bpmn"));
		addModel("Group 3", checkModel("./ext/studentmodels/model 03.bpmn"));
		addModel("Group 4", checkModel("./ext/studentmodels/model 04.bpmn"));
		Simulator.main(new String[]{"-runActive"});
		printResults("Group 1");
		ApplicationContext.getApplicationContext().close();
		System.exit(0);
	}

}
