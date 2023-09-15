package nl.tue.bpmgame.simulator;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.SingleUnitTimeFormatter;
import desmoj.core.simulator.TimeInstant;
import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmn.concepts.BPMNModel;
import nl.tue.bpmn.parser.BPMNParseException;
import nl.tue.bpmn.parser.BPMNParser;

public class TestSimulatorModel {

	public static boolean executeSkip = false;
	public static boolean executeUnskilled = false;
	public static long maximumActiveTime = 1000000;

	/*
	 * A basic test: one task (takes 60000), one skill, one resource, interarrival time 30000
	 * Model: Start->TaskA->End
	 * Ideal: Start->TaskA->End
	 */
	@Test
	public void test00Basic() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment00", "./ext/tests simulator/00 TaskA.bpmn", 70000);

		assertTrue("Two cases must be executed", simulatorModel.getLog().size() == 4);
		assertTrue("TaskA first case starts on case arrival", simulatorModel.getLog().get(0).getStartTime() == 30000);
		assertTrue("TaskA second case starts on first case done", simulatorModel.getLog().get(2).getStartTime() == 90000);
		assertTrue(simulatorModel.getLog().get(0).getNodeLabel().equals("TaskA"));
		assertTrue(simulatorModel.getLog().get(2).getNodeLabel().equals("TaskA"));
		assertTrue(simulatorModel.getLog().get(0).getResourceName().equals("ResourceA"));
		assertTrue(simulatorModel.getLog().get(2).getResourceName().equals("ResourceA"));
	}

	/*
	 * A recovered TaskB is executed.
	 * TaskB requires SkillB not provided by ResourceA, so TaskB is executed by General Manager.
	 * TaskB incurs two penalties: one because it was not properly performed, one because it was executed by the wrong resource.
	 * The task is executed at a penalty. 
	 * The process continues with the next node.
	 * Model: Start->TaskA->End
	 * Ideal: Start->TaskA->TaskB-> End
	 */
	@Test
	public void test10Recover() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment10", "./ext/tests simulator/00 TaskA.bpmn", 70000);
		//System.out.println(PrettyPrinter.prettyLog(simulatorModel.getLog()));
		assertTrue("Two cases must be executed", simulatorModel.getLog().size() == 6);
		assertTrue(simulatorModel.getLog().get(2).getNodeLabel().equals("TaskB"));
		assertTrue("TaskB starts on TaskA done", simulatorModel.getLog().get(2).getStartTime() == 90000);
		assertTrue("TaskB is executed at a penalty", simulatorModel.getLog().get(2).getCompletionTime() == 210000);
		assertTrue("TaskB is excecuted by the General Manager", simulatorModel.getLog().get(2).getResourceName().equals(Assignment.GENERAL_MANAGER));		
	}

	/*
	 * A recovered EventB is executed.
	 * The event is executed at a penalty. 
	 * The process continues with the next node.
	 * Model: Start->EventA->End
	 * Ideal: Start->EventA->EventB->TaskA->End
	 */
	@Test
	public void test11Recover() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment11", "./ext/tests simulator/00 EventA.bpmn", 70000);
		assertTrue("Two cases must be executed", simulatorModel.getLog().size() == 8);
		assertTrue(simulatorModel.getLog().get(2).getNodeLabel().equals("EventB"));
		assertTrue("EventB is executed at a penalty", simulatorModel.getLog().get(2).getCompletionTime() == 180000);
	}

	/*
	 * A recovered TaskA is executed.
	 * The task can both be executed by ResourceA and by the General Manager. 
	 * Depending on who executes the first task, the second task is executed by the other one (because that one is the only one available at the time) 
	 * The process continues with the next node.
	 * Model: Start->EventA->End
	 * Ideal: Start->EventA->EventB->TaskA->End
	 */
	@Test
	public void test12Recover() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment11", "./ext/tests simulator/00 EventA.bpmn", 70000);
		assertTrue(simulatorModel.getLog().get(4).getNodeLabel().equals("TaskA"));
		assertTrue(simulatorModel.getLog().get(6).getNodeLabel().equals("TaskA"));
		if (simulatorModel.getLog().get(4).getResourceName().equals(Assignment.GENERAL_MANAGER)) {
			assertTrue("The other TaskA is executed by ResourceA", simulatorModel.getLog().get(6).getResourceName().equals("ResourceA"));
		} else {
			assertTrue("The other TaskA is executed by the General Manager", simulatorModel.getLog().get(6).getResourceName().equals(Assignment.GENERAL_MANAGER));
		}
	}
	
	/*
	 * A skipped task B is executed anyway
	 * No penalties are incurred (other than an unnecessary task being performed)
	 * Model: Start->TaskA->TaskB->End
	 * Ideal: Start->TaskA->End 
	 */
	@Test
	public void test21Skip() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		executeSkip = true;
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment21", "./ext/tests simulator/00 TaskA TaskB.bpmn", 70000);
		assertTrue(simulatorModel.getLog().size() == 6);
		assertTrue(simulatorModel.getLog().get(0).getNodeLabel().equals("TaskA"));
		assertTrue(simulatorModel.getLog().get(0).getResourceName().equals("ResourceA"));
		assertTrue(simulatorModel.getLog().get(0).getStartTime() == 30000);
		assertTrue(simulatorModel.getLog().get(0).getCompletionTime() == 90000);
		assertTrue(simulatorModel.getLog().get(1).getNodeLabel().equals("TaskB"));
		assertTrue(simulatorModel.getLog().get(1).getResourceName().equals("ResourceB"));
		assertTrue(simulatorModel.getLog().get(1).getStartTime() == 90000);
		assertTrue(simulatorModel.getLog().get(1).getCompletionTime() == 120000);
	}

	/*
	 * A skipped task B is not executed
	 * No penalties are incurred
	 * Model: Start->TaskA->TaskB->End
	 * Ideal: Start->TaskA->End 
	 */
	@Test
	public void test22Skip() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		executeSkip = false;		
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment21", "./ext/tests simulator/00 TaskA TaskB.bpmn", 70000);
		
		assertTrue(simulatorModel.getLog().size() == 4);
		assertTrue(simulatorModel.getLog().get(0).getNodeLabel().equals("TaskA"));
		assertTrue(simulatorModel.getLog().get(0).getResourceName().equals("ResourceA"));
		assertTrue(simulatorModel.getLog().get(0).getStartTime() == 30000);
		assertTrue(simulatorModel.getLog().get(0).getCompletionTime() == 90000);
	}
	
	/*
	 * A skipped event B is executed anyway
	 * No penalties are incurred
	 * Model: Start->EventA->EventB->End
	 * Ideal: Start->EventA->End 
	 */
	@Test
	public void test23Skip() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		executeSkip = true;
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment23", "./ext/tests simulator/00 EventA EventB.bpmn", 70000);
		assertTrue(simulatorModel.getLog().size() == 6);
		assertTrue(simulatorModel.getLog().get(0).getNodeLabel().equals("EventA"));
		assertTrue(simulatorModel.getLog().get(0).getStartTime() == 90000);
		assertTrue(simulatorModel.getLog().get(0).getCompletionTime() == 90000);
		assertTrue(simulatorModel.getLog().get(2).getNodeLabel().equals("EventB"));
		assertTrue(simulatorModel.getLog().get(2).getStartTime() == 120000);
		assertTrue(simulatorModel.getLog().get(2).getCompletionTime() == 120000);
	}
	
	/*
	 * A skipped event B is not executed
	 * No penalties are incurred
	 * Model: Start->EventA->EventB->End
	 * Ideal: Start->EventA->End 
	 */
	@Test
	public void test24Skip() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		executeSkip = false;
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment23", "./ext/tests simulator/00 EventA EventB.bpmn", 70000);

		assertTrue(simulatorModel.getLog().size() == 4);
		assertTrue(simulatorModel.getLog().get(0).getNodeLabel().equals("EventA"));
		assertTrue(simulatorModel.getLog().get(0).getStartTime() == 90000);
		assertTrue(simulatorModel.getLog().get(0).getCompletionTime() == 90000);
	}
	
	/*
	 * Data is generated by task A and then adapted by task B
	 * Model: Start->TaskA->TaskB->End
	 * Ideal: Start->TaskA->TaskB->End 
	 */
	@Test
	public void test31Data() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment31", "./ext/tests simulator/00 TaskA TaskB.bpmn", 70000);
		
		assertTrue(simulatorModel.getLog().size() == 6);
		assertTrue(simulatorModel.getLog().get(0).getAttributeValue("AttributeA").contentEquals("afirst"));
		assertTrue(simulatorModel.getLog().get(0).getAttributeValue("AttributeB").contentEquals("bfirst"));
		assertTrue(simulatorModel.getLog().get(1).getAttributeValue("AttributeA").contentEquals("asecond"));
		assertTrue(simulatorModel.getLog().get(1).getAttributeValue("AttributeB").contentEquals("bsecond"));
	}
	
	/*
	 * Data is generated by event A and then adapted by event B
	 * Model: Start->EventA->EventB->End
	 * Ideal: Start->EventA->EventB->End 
	 */
	@Test
	public void test32Data() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment32", "./ext/tests simulator/00 EventA EventB.bpmn", 70000);
		
		assertTrue(simulatorModel.getLog().size() == 6);
		assertTrue(simulatorModel.getLog().get(0).getAttributeValue("AttributeA").contentEquals("afirst"));
		assertTrue(simulatorModel.getLog().get(0).getAttributeValue("AttributeB").contentEquals("bfirst"));
		assertTrue(simulatorModel.getLog().get(2).getAttributeValue("AttributeA").contentEquals("asecond"));
		assertTrue(simulatorModel.getLog().get(2).getAttributeValue("AttributeB").contentEquals("bsecond"));
	}

	/*
	 * Assigned Resource A is not skilled to perform Task A.
	 * Resource will execute Task A anyway at a penalty.
	 * Model: Start->TaskA->End
	 * Ideal: Start->TaskA->End
	 */
	@Test
	public void test41ExecuteUnskilled() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		executeUnskilled = true;
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment41", "./ext/tests simulator/00 TaskA.bpmn", 70000);
		//System.out.println(PrettyPrinter.prettyLog(simulatorModel.getLog()));

		assertTrue(simulatorModel.getLog().size() == 4);
		assertTrue(simulatorModel.getLog().get(0).getNodeLabel().equals("TaskA"));
		assertTrue(simulatorModel.getLog().get(0).getResourceName().equals("ResourceA"));
		assertTrue(simulatorModel.getLog().get(0).getStartTime() == 30000);
		assertTrue(simulatorModel.getLog().get(0).getCompletionTime() == 120000);
	}
	
	/*
	 * Assigned Resource A is not skilled to perform Task A.
	 * Another resource will execute Task A at a penalty.
	 * Model: Start->TaskA->End
	 * Ideal: Start->TaskA->End
	 */
	@Test
	public void test42ExecuteUnskilled() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		executeUnskilled = false;
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment41", "./ext/tests simulator/00 TaskA.bpmn", 70000);

		assertTrue(simulatorModel.getLog().size() == 4);
		assertTrue(simulatorModel.getLog().get(0).getNodeLabel().equals("TaskA"));
		assertTrue(simulatorModel.getLog().get(0).getResourceName().equals(Assignment.GENERAL_MANAGER));
		assertTrue(simulatorModel.getLog().get(0).getStartTime() == 30000);
		assertTrue(simulatorModel.getLog().get(0).getCompletionTime() == 120000);
	}
	
	@Test
	public void test51KPIs() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		maximumActiveTime = 1000000;
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment50", "./ext/tests simulator/00 A Parallel BC.bpmn", 250000);

		assertTrue(simulatorModel.avgServiceTime() == 120000);
		assertTrue(simulatorModel.avgThroughputTime() == 110000.0);
		assertTrue((simulatorModel.avgWaitingTime() == 10000.0) || (simulatorModel.avgWaitingTime() == 20000.0));
		assertTrue(simulatorModel.avgCustomerSatisfaction() == 10.0); // no customer dissatisfaction
		assertTrue(simulatorModel.totalCost() == 8.0 + (5.0*40000.0/3600000.0));
	}
	
	@Test
	public void test52KPIsCustomerDissatisfaction() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		executeUnskilled = true;
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment41", "./ext/tests simulator/00 TaskA.bpmn", 70000);

		assertTrue(simulatorModel.avgCustomerSatisfaction() == 8.0); // one reduced due to unskilled, one due to taskA -1 effect on satisfation		
	}

	/*
	 * Execution time is sufficient to execute TaskA, but not to execute TaskB and TaskC.
	 * Note that tasks that are already being executed will continue to be executed.
	 * Tasks that are queued will be unqueued.
	 * Model: Start->TaskA->Parallel(TaskB, TaskC)->End
	 * Ideal: Start->TaskA->Parallel(TaskB, TaskC)->End
	 */
	@Test
	public void test61MaximumActiveTime() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		maximumActiveTime = 30000;
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment50", "./ext/tests simulator/00 A Parallel BC.bpmn", 250000);
		
		assertTrue(simulatorModel.getLog().size() == 4);
	}

	/*
	 * Execution time is sufficient to execute TaskA and sometimes TaskB and/or TaskC.
	 * Note that tasks that are already being executed will continue to be executed.
	 * Tasks that are queued will be unqueued.
	 * Model: Start->TaskA->Parallel(TaskB, TaskC)->End
	 * Ideal: Start->TaskA->Parallel(TaskB, TaskC)->End
	 */
	@Test
	public void test62MaximumActiveTime() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		maximumActiveTime = 50000;
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment50", "./ext/tests simulator/00 A Parallel BC.bpmn", 250000);
		
		assertTrue(simulatorModel.getLog().size() == 8);
	}

	/*
	 * Model: Start->EventA->EventB->End
	 * Ideal: Start->OR(EventA,EventB)->End 
	 *        with only EventA occurring
	 * Expect 3 cases Start->EventA->End
	 */
	@Test
	public void test71NonOccurringEvents() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment71", "./ext/tests simulator/00 EventA EventB.bpmn", 100000);
		
		assertTrue(simulatorModel.getLog().size() == 6);
	}

	/*
	 * Model: Start -> Deferred(EventA->TaskA, EventB->TaskB) -> End
	 * Ideal: Start -> OR(EventA->TaskA, EventB->TaskB) -> End 
	 *        one case with eventA occurring and one case with eventB occurring
	 * Expect 1 case Start->EventA->TaskA->End
	 * Expect 1 case Start->EventB->TaskB->End
	 */
	@Test
	public void test72DeferredChoice() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment72", "./ext/tests simulator/00 Deferred Choice.bpmn", 100000);

		assertTrue(simulatorModel.getLog().size() == 6);
	}

	/*
	 * Model: Start->EventA->TaskA->End
	 * Ideal: Start -> OR(EventA->TaskA, EventB->TaskB) -> End 
	 *        one case with eventA occurring and one case with eventB occurring
	 * Expect 1 case Start->EventA->TaskA->End
	 * Expect 1 case Start->EventB->TaskB->End at a penalty
	 */
	@Test
	public void test73IncorrectDeferredChoice() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimulatorModel simulatorModel = doSimulation("nl.tue.bpmgame.assignment.Assignment72", "./ext/tests simulator/00 Incorrect deferred choice.bpmn", 100000);
		//System.out.println(PrettyPrinter.prettyLog(simulatorModel.getLog()));		

		assertTrue(simulatorModel.getLog().size() == 6);
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
}
