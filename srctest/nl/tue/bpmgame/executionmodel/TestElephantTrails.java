package nl.tue.bpmgame.executionmodel;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;

import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.assignment.MockAssignment;
import nl.tue.bpmgame.declarative.DeclarativeModel;
import nl.tue.bpmgame.executionmodel.CaseExecution.Fix;
import nl.tue.bpmn.concepts.BPMNModel;
import nl.tue.bpmn.parser.BPMNParseException;
import nl.tue.bpmn.parser.BPMNParser;

public class TestElephantTrails {

	/*
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> A -> B -> end
	 * skip B
	 */
	@Test
	public void testSkip() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/00 sequence start_A_B_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addSkip("B");
		c.setCompletionCondition("[end]");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start]");
		d.addCondition("B", "[A]");
		d.addCondition("end", "[B]");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(d);
		CaseExecution execution = new CaseExecution(m, c);

		List<String> executableNodes = execution.nextExecution();
		List<Fix> executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("A"));
		assertTrue(executionFixes.get(0) == Fix.None);
				
		execution.execute("A");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("end"));
		assertTrue(executionFixes.get(0) == Fix.None);

		execution.execute("end");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 0);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executionFixes.get(0) == Fix.None);
	}

	/*
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> A -> B -> end
	 * redo A
	 */
	@Test
	public void testRedo() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/00 sequence start_A_B_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addRedo("A");
		c.setCompletionCondition("[end]");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start]");
		d.addCondition("B", "[A]");
		d.addCondition("end", "[B]");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(d);
		CaseExecution execution = new CaseExecution(m, c);

		List<String> executableNodes = execution.nextExecution();
		List<Fix> executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("A"));
		assertTrue(executionFixes.get(0) == Fix.None);
				
		execution.execute("A");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("A"));
		assertTrue(executionFixes.get(0) == Fix.ElephantStep);
		
		execution.elephantStep("A");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("B"));
		assertTrue(executionFixes.get(0) == Fix.None);

		execution.execute("B");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("end"));
		assertTrue(executionFixes.get(0) == Fix.None);

		execution.execute("end");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 0);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executionFixes.get(0) == Fix.None);
	}

	/*
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> A -> B -> end
	 * repeat A,B
	 */
	@Test
	public void testRepeat() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/00 sequence start_A_B_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addRepeat("A", "B");
		c.setCompletionCondition("[end]");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start]");
		d.addCondition("B", "[A]");
		d.addCondition("end", "[B]");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(d);
		CaseExecution execution = new CaseExecution(m, c);

		List<String> executableNodes = execution.nextExecution();
		List<Fix> executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("A"));
		assertTrue(executionFixes.get(0) == Fix.None);
				
		execution.execute("A");
				
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("B"));
		assertTrue(executionFixes.get(0) == Fix.None);

		execution.execute("B");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("A"));
		assertTrue(executionFixes.get(0) == Fix.ElephantStep);
		
		execution.elephantStep("A");

		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("B"));
		assertTrue(executionFixes.get(0) == Fix.ElephantStep);
		
		execution.elephantStep("B");

		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("end"));
		assertTrue(executionFixes.get(0) == Fix.None);

		execution.execute("end");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 0);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executionFixes.get(0) == Fix.None);
	}
}
