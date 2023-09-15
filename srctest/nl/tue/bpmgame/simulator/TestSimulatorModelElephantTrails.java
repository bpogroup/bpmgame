package nl.tue.bpmgame.simulator;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.SingleUnitTimeFormatter;
import desmoj.core.simulator.TimeInstant;
import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmn.concepts.BPMNModel;
import nl.tue.bpmn.parser.BPMNParseException;
import nl.tue.bpmn.parser.BPMNParser;

public class TestSimulatorModelElephantTrails {

	/*
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> A -> B -> end
	 * skip B
	 */
	@Test
	public void test00Skip() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.AssignmentElephantTrail00", "./ext/tests simulator/00 TaskA TaskB.bpmn", 70000);

		assertTrue("Two cases must be executed", simulatorModel.getLog().size() == 4);
		assertTrue("TaskA first case starts on case arrival", simulatorModel.getLog().get(0).getStartTime() == 30000);
		assertTrue("TaskA first case starts on case arrival", simulatorModel.getLog().get(0).getNodeLabel().equals("TaskA"));
		assertTrue("First case completes after TaskA, TaskB is elephant-trail skipped", simulatorModel.getLog().get(1).getNodeLabel().equals("End"));
		assertTrue("First case completes after TaskA, TaskB is elephant-trail skipped", simulatorModel.getLog().get(1).getStartTime() == 90000);
		assertTrue(simulatorModel.getLog().get(2).getNodeLabel().equals("TaskA"));
		assertTrue(simulatorModel.getLog().get(2).getStartTime() == 90000);
	}

	/*
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> A -> B -> end
	 * redo B
	 */
	@Test
	public void test10Redo() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.AssignmentElephantTrail10", "./ext/tests simulator/00 TaskA TaskB.bpmn", 70000);

		assertTrue("Two cases must be executed, each has 4 events", simulatorModel.getLog().size() == 8);
		List<LogEvent> filteredLog = filterLog(simulatorModel.getLog(), "0");
		assertTrue(filteredLog.get(0).getNodeLabel().equals("TaskA"));
		assertTrue(filteredLog.get(1).getNodeLabel().equals("TaskB"));
		assertTrue(filteredLog.get(2).getNodeLabel().equals("TaskB"));
		assertTrue(filteredLog.get(3).getNodeLabel().equals("End"));
	}

	/*
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> A -> B -> end
	 * repeat A,B
	 */
	@Test
	public void test20Repeat() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.AssignmentElephantTrail20", "./ext/tests simulator/00 TaskA TaskB.bpmn", 70000);

		assertTrue("Two cases must be executed, each has 4 events", simulatorModel.getLog().size() == 10);
		List<LogEvent> filteredLog = filterLog(simulatorModel.getLog(), "0");
		assertTrue(filteredLog.get(0).getNodeLabel().equals("TaskA"));
		assertTrue(filteredLog.get(1).getNodeLabel().equals("TaskB"));
		assertTrue(filteredLog.get(2).getNodeLabel().equals("TaskA"));
		assertTrue(filteredLog.get(3).getNodeLabel().equals("TaskB"));
		assertTrue(filteredLog.get(4).getNodeLabel().equals("End"));
	}
	
	private static SimulatorModel doSimulation(String assignment, String model, long endGenerationTime) throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		//Initialize the assignment
		Assignment.initializeAssignment(assignment);
		
		//Load the model
		BPMNParser parser = new BPMNParser();
		parser.parseFile(model);
		BPMNModel bpmnModel = parser.getParsedModel();
		
		//Instantiate the cases
		CaseGenerator cg = new CaseGenerator();
		cg.generateCases(0, endGenerationTime);
		
		//Instantiate the simulation model
		SimulatorModel simulatorModel = new SimulatorModel(bpmnModel, cg.getCases(), 0);
		
		//run the simulation model
		Experiment.setTimeFormatter(new SingleUnitTimeFormatter(TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS, 0, true));
		Experiment experiment = new Experiment("Experiment", false);		
		simulatorModel.connectToExperiment(experiment);

		experiment.setShowProgressBar(false);
		experiment.stop(new TimeInstant(100*30000, TimeUnit.MILLISECONDS));
		experiment.tracePeriod(new TimeInstant(0), new TimeInstant(0));
		experiment.debugPeriod(new TimeInstant(0), new TimeInstant(0));
		experiment.setSilent(true);

		experiment.start();
				
		return simulatorModel;
	}
	
	private List<LogEvent> filterLog(List<LogEvent> log, String byCaseID){
		List<LogEvent> filteredLog = new ArrayList<LogEvent>();
		for (LogEvent e: log) {
			if (e.getCaseID().equals("0")) {
				filteredLog.add(e);
			}
		}
		return filteredLog;
	}
}
