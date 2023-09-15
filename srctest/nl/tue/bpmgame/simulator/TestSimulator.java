package nl.tue.bpmgame.simulator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.Model;
import nl.tue.bpmgame.dataaccess.model.PersistentKPI;
import nl.tue.bpmgame.dataaccess.model.PersistentLogEvent;
import nl.tue.bpmgame.dataaccess.service.DAOService;
import nl.tue.bpmn.parser.BPMNParseException;
import nl.tue.util.TimeUtils;

public class TestSimulator {
	
	@Before
	public void beforeTest() {
		//Clean the database
		DAOService.modelDAO().clear();
		DAOService.persistentKPIDAO().clear();
		DAOService.persistentLogEventDAO().clear();
		DAOService.logAttributeDAO().clear();
		DAOService.gameGroupDAO().clear();		
	}	

	@Test
	public void test() throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, BPMNParseException {
		byte[] encoded = Files.readAllBytes(Paths.get("./ext/tests simulator/00 TaskA TaskB.bpmn"));
		String xml = new String(encoded); 
				
		GameGroup group1 = new GameGroup();
		group1.setName("Group 1");
		DAOService.gameGroupDAO().create(group1);
		
		Model model1 = new Model();
		model1.setActive(true);
		model1.setGroup(group1);
		model1.setXml(xml);
		DAOService.modelDAO().create(model1);
		
		GameGroup group2 = new GameGroup();
		group2.setName("Group 2");
		DAOService.gameGroupDAO().create(group2);
		
		Model model2 = new Model();
		model2.setActive(true);
		model2.setGroup(group2);
		model2.setXml(xml);
		DAOService.modelDAO().create(model2);
		
		Simulator.simulate("nl.tue.bpmgame.assignment.IntegrationTestAssignment", false, false, TimeUtils.tomorrow());
		
		PersistentKPI pkpi = DAOService.persistentKPIDAO().list().get(0);
		List<PersistentLogEvent> log = DAOService.persistentLogEventDAO().listByAttribute("gameGroup", group1);		
				
		//Check nr cases produced
		double nrCases = log.size()/3; //Since there are exactly 3 events per case, this should be the number of cases
		assertTrue("Nr. of cases produced for 7 hours approx. every 45 minutes should be around 9", Math.abs(nrCases - 9) <= 1);
		
		//Check total cost
		double avgCostEstimate = pkpi.getAvgServiceTime() / 3600000.0; //since cost is 1 per hour, this is the service time per hour per case			
		assertTrue("Cost estimate should be equal to actual costs", Math.abs(avgCostEstimate * nrCases - pkpi.getTotalCost()) < 0.2);
		
		//Check average service time
		double ast = pkpi.getAvgServiceTime();
		double st = 0;
		for (PersistentLogEvent ple: log) {
			st += ple.getCompletionTime() - ple.getStartTime();
		}
		assertTrue("Service time KPI = service time of events in log", Math.abs(st/nrCases - ast) < 10);
		assertTrue("Service time should be around the expected service time, which is 1 h or 3600000 ms. Actual value is " + ast + " this mismatch may be due to randomness", Math.abs(ast-3600000) < 600000);
		
		//Check waiting time and throughput time
		double awt = pkpi.getAvgWaitingTime();
		double att = pkpi.getAvgThroughputTime();
		assertTrue("Throughput time should be service time + waiting time", Math.abs(awt + ast - att) < 10);
		
		System.out.println("Case ID,Event,Start Time,Completion Time,Resource");
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

}
