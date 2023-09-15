package simulator2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.assignment.CaseType;
import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmgame.simulator.LogEvent;
import nl.tue.bpmgame.simulator.Simulator;
import nl.tue.bpmgame.simulator.SimulatorModel;
import nl.tue.bpmn.parser.BPMNParseException;
import nl.tue.util.TimeUtils;

public class SimulateSpecificModel {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, IOException, BPMNParseException {
		Properties props = new Properties();
		Class<?> cls = Class.forName("nl.tue.bpmgame.simulator.Simulator");
        ClassLoader cLoader = cls.getClassLoader();
		props.load(cLoader.getResourceAsStream("nl/tue/bpmgame/bpmgamegui/resources/log4j.test.properties"));
		PropertyConfigurator.configure(props);
		
		Assignment.initializeAssignment(Assignment.ACTIVE_ASSIGNMENT);
		
		long startWorkdayTime = TimeUtils.today() + Assignment.getAssignment().getWorkdayStart(); //in milliseconds since 1 jan 1970
		long stopWorkdayTime = startWorkdayTime + Assignment.getAssignment().getWorkdayDuration(); //in milliseconds since 1 jan 1970

		Case c = null;
		for (CaseType ct: Assignment.getAssignment().getCaseTypes()) {
			if (ct.getName().equals("Happy Flow")) {
				c = ct.generateCase(startWorkdayTime);
			}
		}		
		List<Case> cases = new ArrayList<Case>();
		cases.add(c);
		
		byte[] encoded = Files.readAllBytes(Paths.get("./ext/problemmodels/13122018.bpmn"));
		String xml = new String(encoded);
		SimulatorModel result = Simulator.simulateModel(xml, cases, startWorkdayTime, stopWorkdayTime);
		
	    List<String> dataItems = new ArrayList<String>(Assignment.getAssignment().getDataItems());
	    
	    String header = "Case ID,Activity,Resource,Start Timestamp,Complete Timestamp";
	    for (String dataItem: dataItems) {
	    	header += "," + dataItem;
	    }
	    System.out.println(header);
	    
	    for (LogEvent le: result.getLog()) {
		    	String startTime = TimeUtils.formatTime(le.getStartTime());
		    	String completionTime = TimeUtils.formatTime(le.getCompletionTime());
		    	String line = le.getCaseID() + "," + le.getNodeLabel() + "," + (le.getResourceName()==null?"":le.getResourceName()) + "," + startTime + "," + completionTime;
		    	List<String> attributes = new ArrayList<String>(le.getAttributes());
			    for (String dataItem: dataItems) {
			    	String value = "";
			    	int i = 0;
			    	boolean found = false;
			    	while ((i < attributes.size()) && !found) {
			    		if (attributes.get(i).equals(dataItem)) {
			    			value = le.getAttributeValue(attributes.get(i));
			    			attributes.remove(i);
			    			found = true;
			    		}
			    		i++;
			    	}
		    		line += "," + value;
			    }
			    System.out.println(line);
	    }
		
		System.exit(0);
	}

}
