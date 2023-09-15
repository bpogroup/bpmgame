package nl.tue.bpmgame.simulator;

import java.util.concurrent.TimeUnit;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

public class EventHolder extends SimProcess{

	String eventLabel;
	long holdTime;
	CaseSimulator caseSimulator;
	SimulatorModel model;
	
	public EventHolder(SimulatorModel model, CaseSimulator caseSimulator, String eventLabel, long holdTime) {
		super(model, "", false);
		
		this.eventLabel = eventLabel;
		this.holdTime = holdTime;
		this.caseSimulator = caseSimulator;
		this.model = model;
	}

	@Override
	public void lifeCycle() throws SuspendExecution {
		this.hold(new TimeSpan(holdTime, TimeUnit.MILLISECONDS));
		caseSimulator.completeNodeExecution(eventLabel, model.getSimulationTime(), model.getSimulationTime(), null, true);
		caseSimulator.activate();
	}
}
