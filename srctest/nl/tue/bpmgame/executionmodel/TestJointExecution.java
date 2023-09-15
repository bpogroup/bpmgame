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

/**
 *  Test a BPMN model against a declarative ideal model. 
 *
 */
public class TestJointExecution {

	/**
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> A -> B -> end
	 */
	@Test
	public void test00SequenceCorrect() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/00 sequence start_A_B_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
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
		assertTrue(executableNodes.get(0).equals("end"));
		assertTrue(executionFixes.get(0) == Fix.None);

		execution.execute("end");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 0);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executionFixes.get(0) == Fix.None);
	}

	/**
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> A -> end
	 */
	@Test
	public void test01SequenceSkip() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/01 sequence start_A_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
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
		assertTrue(executionFixes.get(0) == Fix.TaskRecovery);

		execution.recover("B");
		
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

	/**
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> end
	 */
	@Test
	public void test02SequenceSkipMultiple() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/02 sequence start_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
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
		assertTrue(executionFixes.get(0) == Fix.TaskRecovery);
				
		execution.recover("A");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("B"));
		assertTrue(executionFixes.get(0) == Fix.TaskRecovery);

		execution.recover("B");
		
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

	/**
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> A -> C -> B -> end
	 */
	@Test
	public void test03SequenceInsert() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/03 sequence start_A_C_B_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
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
		assertTrue(executableNodes.get(0).equals("C"));
		assertTrue(executionFixes.get(0) == Fix.TaskSkip);

		execution.skip("C");
		
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

	/**
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> A -> B -> C -> D -> end
	 */
	@Test
	public void test04SequenceInsertMultiple() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/04 sequence start_A_B_C_D_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
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
		assertTrue(executableNodes.get(0).equals("C"));
		assertTrue(executionFixes.get(0) == Fix.TaskSkip);

		execution.skip("C");

		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("D"));
		assertTrue(executionFixes.get(0) == Fix.TaskSkip);

		execution.skip("D");

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

	/**
	 * Declarative ideal is start -> A -> B -> end
	 * BPMN model is start -> B -> A -> end
	 */
	@Test
	public void test05SequenceSwap() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/05 sequence start_B_A_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
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
		assertTrue(executionFixes.get(0) == Fix.TaskRecovery);
				
		execution.recover("A");
		
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
		assertTrue(executionFixes.get(0) == Fix.TaskSkip);
				
		execution.skip("A");
		
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
	
	/**
	 * Declarative ideal is start -> (A AND B) -> end
	 * BPMN model is start -> (A AND B) -> end
	 */
	@Test
	public void test10ParallelCorrect() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/10 parallellism start_AB_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.setCompletionCondition("[end]");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start]");
		d.addCondition("B", "[start]");
		d.addCondition("end", "[A] AND [B]");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(d);
		CaseExecution execution = new CaseExecution(m, c);

		List<String> executableNodes = execution.nextExecution();
		List<Fix> executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 2);
		assertTrue(executionFixes.size() == 2);
		assertTrue((executableNodes.get(0).equals("A") && executableNodes.get(1).equals("B")) || (executableNodes.get(0).equals("B") && executableNodes.get(1).equals("A")));
		assertTrue((executionFixes.get(0) == Fix.None) && (executionFixes.get(1) == Fix.None));
				
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
		assertTrue(executableNodes.get(0).equals("end"));
		assertTrue(executionFixes.get(0) == Fix.None);

		execution.execute("end");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 0);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executionFixes.get(0) == Fix.None);
	}

	/**
	 * Declarative ideal is start -> (A AND B) -> end
	 * BPMN model is start -> A -> B -> end
	 */
	@Test
	public void test11ParallelSequentialCompletion() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/11 parallel sequence start_A_B_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.setCompletionCondition("[end]");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start]");
		d.addCondition("B", "[start]");
		d.addCondition("end", "[A] AND [B]");
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
		assertTrue(executableNodes.get(0).equals("end"));
		assertTrue(executionFixes.get(0) == Fix.None);

		execution.execute("end");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 0);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executionFixes.get(0) == Fix.None);
	}

	/**
	 * Declarative ideal is start -> (A AND B) -> end
	 * BPMN model is start -> A -> end
	 */
	@Test
	public void test12ParallelDelete() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/12 parallel delete start_A_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.setCompletionCondition("[end]");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start]");
		d.addCondition("B", "[start]");
		d.addCondition("end", "[A] AND [B]");
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
		assertTrue(executionFixes.get(0) == Fix.TaskRecovery);

		execution.recover("B");
		
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
	
	/**
	 * Declarative ideal is start -> (A AND B) -> end
	 * BPMN model is start -> (A AND C) -> end
	 */
	@Test
	public void test13ParallelReplace() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/13 parallel replace start_AC_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.setCompletionCondition("[end]");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start]");
		d.addCondition("B", "[start]");
		d.addCondition("end", "[A] AND [B]");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(d);
		CaseExecution execution = new CaseExecution(m, c);

		List<String> executableNodes = execution.nextExecution();
		List<Fix> executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 2);
		assertTrue(executionFixes.size() == 2);
		if (executableNodes.get(0).equals("A")) {
			assertTrue(executableNodes.get(0).equals("A"));
			assertTrue(executionFixes.get(0) == Fix.None);
			assertTrue(executableNodes.get(1).equals("C"));
			assertTrue(executionFixes.get(1) == Fix.TaskSkip);
		} else {
			assertTrue(executableNodes.get(0).equals("C"));
			assertTrue(executionFixes.get(0) == Fix.TaskSkip);			
			assertTrue(executableNodes.get(1).equals("A"));
			assertTrue(executionFixes.get(1) == Fix.None);
		}
				
		execution.execute("A");
		
		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("C"));
		assertTrue(executionFixes.get(0) == Fix.TaskSkip);

		execution.skip("C");

		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("B"));
		assertTrue(executionFixes.get(0) == Fix.TaskRecovery);

		execution.recover("B");

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

	/**
	 * Declarative ideal is start -> (A AND B) -> end
	 * BPMN model is start -> (A AND (B -> C)) -> end
	 */
	@Test
	public void test14ParallelInsert() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/14 parallel insert start_AB_C_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.setCompletionCondition("[end]");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start]");
		d.addCondition("B", "[start]");
		d.addCondition("end", "[A] AND [B]");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(d);
		CaseExecution execution = new CaseExecution(m, c);

		List<String> executableNodes = execution.nextExecution();
		List<Fix> executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 2);
		assertTrue(executionFixes.size() == 2);
		if (executableNodes.get(0).equals("A")) {
			assertTrue(executableNodes.get(0).equals("A"));
			assertTrue(executionFixes.get(0) == Fix.None);
			assertTrue(executableNodes.get(1).equals("B"));
			assertTrue(executionFixes.get(1) == Fix.None);
		} else {
			assertTrue(executableNodes.get(0).equals("B"));
			assertTrue(executionFixes.get(0) == Fix.None);			
			assertTrue(executableNodes.get(1).equals("A"));
			assertTrue(executionFixes.get(1) == Fix.None);
		}
				
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
		assertTrue(executableNodes.get(0).equals("C"));
		assertTrue(executionFixes.get(0) == Fix.TaskSkip);

		execution.skip("C");

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

	/**
	 * Declarative ideal is start -> x=1 A, x=2 B, end
	 * BPMN model is start -> x=1 A, x=2 B, end
	 * case has x=1
	 */
	@Test
	public void test20Choice() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/20 choice start_AB_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.setCompletionCondition("[end]");
		c.addValues("x", "1", "1");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start] AND (x=1)");
		d.addCondition("B", "[start] AND (x=2)");
		d.addCondition("end", "[A] OR [B]");
		d.addTransitionTouches("start", "x");
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

	/**
	 * Declarative ideal is start -> x=1 A, x=2 B, end
	 * BPMN model is start -> x=1 B, x=2 A, end
	 * case has x=1
	 */
	@Test
	public void test21ChoiceSwap() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/21 choice swap start_AB_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.setCompletionCondition("[end]");
		c.addValues("x", "1", "1");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start] AND (x=1)");
		d.addCondition("B", "[start] AND (x=2)");
		d.addCondition("end", "[A] OR [B]");
		d.addTransitionTouches("start", "x");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(d);
		CaseExecution execution = new CaseExecution(m, c);

		List<String> executableNodes = execution.nextExecution();
		List<Fix> executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("B"));
		assertTrue(executionFixes.get(0) == Fix.TaskSkip);
				
		execution.skip("B");

		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("A"));
		assertTrue(executionFixes.get(0) == Fix.TaskRecovery);
				
		execution.recover("A");

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

	/**
	 * Declarative ideal is start -> x=1 A, x=2 B, end
	 * BPMN model is start -> x=1 A -> C, x=2 B, end
	 * case has x=1
	 */
	@Test
	public void test22ChoiceInsert() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/22 choice insert start_A_CB_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.setCompletionCondition("[end]");
		c.addValues("x", "1", "1");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start] AND (x=1)");
		d.addCondition("B", "[start] AND (x=2)");
		d.addCondition("end", "[A] OR [B]");
		d.addTransitionTouches("start", "x");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(d);
		CaseExecution execution = new CaseExecution(m, c);

		List<String> executableNodes = execution.nextExecution();
		List<Fix> executionFixes = execution.fixForNextExecution();
		assertTrue(executableNodes.size() == 1);
		assertTrue(executionFixes.size() == 1);
		assertTrue(executableNodes.get(0).equals("C"));
		assertTrue(executionFixes.get(0) == Fix.TaskSkip);
				
		execution.skip("C");

		executableNodes = execution.nextExecution();
		executionFixes = execution.fixForNextExecution();
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

	/**
	 * Declarative ideal is start -> A -> (x<2 loop), (a>=2 end)
	 * BPMN model is start -> A -> (x<2 loop), (a>=2 end)
	 * case has counts x=#times A executed
	 */
	@Test
	public void test30Iteration() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/30 iteration start_A_loop_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.setCompletionCondition("[end]");
		c.addValues("x", "1", "2");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start] OR (x<2)");
		d.addCondition("end", "[A] AND (x>=2) AND [start]");
		d.addTransitionTouches("A", "x");
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

	/**
	 * Declarative ideal is start -> A -> (x<2 loop), (a>=2 end)
	 * BPMN model is start -> A -> (x<1 loop), (a>=1 end)
	 * case has counts x=#times A executed
	 */
	@Test
	public void test31IterationDelete() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests joint execution/31 iteration start_A_loop_end.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.setCompletionCondition("[end]");
		c.addValues("x", "1", "2");
		DeclarativeModel d = new DeclarativeModel();
		d.addCondition("start", "");
		d.addCondition("A", "[start] OR (x<2)");
		d.addCondition("end", "[A] AND (x>=2) AND [start]");
		d.addTransitionTouches("A", "x");
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
		assertTrue(executionFixes.get(0) == Fix.TaskRecovery);
				
		execution.recover("A");

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
