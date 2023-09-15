package nl.tue.bpmgame.simulator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;
import nl.tue.bpmgame.executionmodel.Case;

/**
 * Generates cases for a particular simulation model, based on a pre-generated set of general cases.
 * The generation of cases will start from the specified startTime (milliseconds after 1970).
 * 
 */
public class SimulatorModelCaseGenerator extends SimProcess{

	List<Case> cases;
	long startTime; 
	
	public SimulatorModelCaseGenerator(Model owner, List<Case> cases, long startTime) {
		super(owner, "", false);
		this.cases = cases;
		this.startTime = startTime;
	}

	public void lifeCycle() throws SuspendExecution {
		SimulatorModel model = (SimulatorModel) getModel();
		long previousStart = startTime;

		for (Case c: cases) {
			hold(new TimeSpan(c.getStartTime() - previousStart, TimeUnit.MILLISECONDS));

			CaseSimulator container = new CaseSimulator(model, c);
			container.activate();
			
			previousStart = c.getStartTime();
		}
	}
}
