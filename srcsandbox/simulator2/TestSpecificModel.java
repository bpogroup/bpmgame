package simulator2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.assignment.CaseType;
import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmgame.executionmodel.CaseExecution;
import nl.tue.bpmgame.executionmodel.CaseExecution.Fix;
import nl.tue.bpmgame.simulator.IntegrationTest;
import nl.tue.bpmn.concepts.BPMNModel;
import nl.tue.bpmn.parser.BPMNParseException;
import nl.tue.bpmn.parser.BPMNParser;

public class TestSpecificModel {

	public static void main(String[] args) throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, IOException {
		String xml = IntegrationTest.checkModel("./ext/problemmodels/base model  V001- Groep 3 .bpmn");
		BPMNParser parser = new BPMNParser();
		parser.parse(xml);
		BPMNModel m = parser.getParsedModel();
		
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.AssignmentLoan");
		Assignment.getAssignment().getBehavior();
		
		Case c = null;
		for (CaseType ct: Assignment.getAssignment().getCaseTypes()) {
			if (ct.getName().equals("Happy Flow")) {
				c = ct.generateCase(0);
			}
		}		
		CaseExecution execution = new CaseExecution(m, c);

		String result = executeNext(execution);
		while (result != null) {
			System.out.println(result);
			result = executeNext(execution);
		}
	}
	
	private static String executeNext(CaseExecution execution) {
		List<String> executableNodes = execution.nextExecution();
		List<Fix> executionFixes = execution.fixForNextExecution();

		if (!executableNodes.isEmpty()) {
			switch (executionFixes.get(0)) {
			case None:
				execution.execute(executableNodes.get(0));
				return "EXECUTING " + executableNodes.get(0);
			case TaskRecovery:
				execution.recover(executableNodes.get(0));
				return "RECOVERING " + executableNodes.get(0);
			case TaskSkip:
				execution.skip(executableNodes.get(0));
				return "SKIPPING " + executableNodes.get(0);
			case ElephantStep:
				execution.elephantStep(executableNodes.get(0));
				return "ELEPHANTING " + executableNodes.get(0);
			default:
				return "ERROR";
			}
		} else if (!executionFixes.isEmpty() && executionFixes.get(0) == Fix.PrematureCompletion) {
			return "PREMATURE COMPLETION";
		} else {
			return null;
		}
	}

}
