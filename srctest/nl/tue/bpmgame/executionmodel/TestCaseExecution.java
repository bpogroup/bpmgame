package nl.tue.bpmgame.executionmodel;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.assignment.MockAssignment;
import nl.tue.bpmgame.declarative.DeclarativeModel;
import nl.tue.bpmn.concepts.BPMNModel;
import nl.tue.bpmn.concepts.BPMNNode;
import nl.tue.bpmn.parser.BPMNParseException;
import nl.tue.bpmn.parser.BPMNParser;

public class TestCaseExecution {

	@Test
	public void test00BasicExecution() throws BPMNParseException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/00 basic model.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		BPMNNode enabledNode = enabledNodes.iterator().next(); 
		assertTrue(enabledNode.getName().equals("Task"));
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode = enabledNodes.iterator().next(); 
		assertTrue(enabledNode.getName().equals("End"));		
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 0);
	}

	@Test
	public void test11ChoiceA() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/11 choice.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addValues("variable", "1", "1");
		DeclarativeModel dm = new DeclarativeModel();
		dm.addTransitionTouches("Start", "variable");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		BPMNNode enabledNode = enabledNodes.iterator().next(); 
		assertTrue(enabledNode.getName().equals("Task A"));
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode = enabledNodes.iterator().next(); 
		assertTrue(enabledNode.getName().equals("End"));		
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 0);
	}

	@Test
	public void test11ChoiceB() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/11 choice.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addValues("variable", "2", "2");
		DeclarativeModel dm = new DeclarativeModel();
		dm.addTransitionTouches("Start", "variable");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		BPMNNode enabledNode = enabledNodes.iterator().next(); 
		assertTrue(enabledNode.getName().equals("Task B"));
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode = enabledNodes.iterator().next(); 
		assertTrue(enabledNode.getName().equals("End"));		
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 0);
	}
	
	@Test
	public void test12ChoiceNoData() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/11 choice.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		DeclarativeModel dm = new DeclarativeModel();
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		BPMNNode enabledNode = enabledNodes.iterator().next(); 
		assertTrue(enabledNode.getName().equals("Task B"));
	}
	
	@Test
	public void test13ChoiceNoCriteria() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/13 choice no criteria.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		DeclarativeModel dm = new DeclarativeModel();
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		boolean aEnabled = false;
		boolean bEnabled = false;
		for (int i = 0; i < 10; i++){
			BPMNState state = execution.determineInitialState();
			Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
			assertTrue(enabledNodes.size() == 1);
			BPMNNode enabledNode = enabledNodes.iterator().next();
			if (enabledNode.getName().equals("Task A")){
				aEnabled = true;
			}else if (enabledNode.getName().equals("Task B")){
				bEnabled = true;
			}
		}
		assertTrue("Task A is never enabled. (Note: this could be a rare random occurrence. Validate by executing again.)", aEnabled);
		assertTrue("Task B is never enabled. (Note: this could be a rare random occurrence. Validate by executing again.)", bEnabled);
	}

	@Test
	public void test14Loop() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/14 loop.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addValues("variable", "1", "2");
		DeclarativeModel dm = new DeclarativeModel();
		dm.addTransitionTouches("Task A", "variable");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		BPMNNode enabledNode = enabledNodes.iterator().next(); 
		assertTrue(enabledNode.getName().equals("Task A"));
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode = enabledNodes.iterator().next();
		assertTrue(enabledNode.getName().equals("Task A"));
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode = enabledNodes.iterator().next();
		assertTrue(enabledNode.getName().equals("End"));
	}

	@Test
	public void test15InfiniteLoop() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/15 infinite loop.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addValues("variable", "1", "1");
		DeclarativeModel dm = new DeclarativeModel();
		dm.addTransitionTouches("Start", "variable");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 0);
	}

	@Test
	public void test21CorrectParallellism() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/21 parallellism.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		DeclarativeModel dm = new DeclarativeModel();
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 2);
		Iterator<BPMNNode> nodeIterator = enabledNodes.iterator(); 
		BPMNNode enabledNode1 = nodeIterator.next();
		BPMNNode enabledNode2 = nodeIterator.next();		
		assertTrue((enabledNode1.getName().equals("Task A") && enabledNode2.getName().equals("Task B"))||
				   (enabledNode1.getName().equals("Task B") && enabledNode2.getName().equals("Task A")));
		if (enabledNode1.getName().equals("Task A")){
			state = execution.fireTransition(state, enabledNode1);
		}else{
			state = execution.fireTransition(state, enabledNode2);			
		}
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode1 = enabledNodes.iterator().next();
		assertTrue(enabledNode1.getName().equals("Task B"));
		state = execution.fireTransition(state, enabledNode1);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode1 = enabledNodes.iterator().next();
		assertTrue(enabledNode1.getName().equals("End"));		
	}

	@Test
	public void test31aCorrectDeferredChoice() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/31 correct deferred choice.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addOccurringEvent("Timer");
		DeclarativeModel dm = new DeclarativeModel();
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		Iterator<BPMNNode> nodeIterator = enabledNodes.iterator(); 
		BPMNNode enabledNode = nodeIterator.next();
		assertTrue(enabledNode.getName().equals("Timer"));
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode = enabledNodes.iterator().next();
		assertTrue(enabledNode.getName().equals("Task B"));
	}

	@Test
	public void test31bCorrectDeferredChoice() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/31 correct deferred choice.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addOccurringEvent("Message");
		DeclarativeModel dm = new DeclarativeModel();
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		Iterator<BPMNNode> nodeIterator = enabledNodes.iterator(); 
		BPMNNode enabledNode = nodeIterator.next();
		assertTrue(enabledNode.getName().equals("Message"));
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode = enabledNodes.iterator().next();
		assertTrue(enabledNode.getName().equals("Task A"));
	}

	@Test
	public void test31cNonDeterministicDeferredChoice() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/31 correct deferred choice.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addOccurringEvent("Message");
		c.addOccurringEvent("Timer");
		DeclarativeModel dm = new DeclarativeModel();
		boolean messageEnabled = false;
		boolean timerEnabled = false;
		for (int i = 0; i < 10; i++){
			Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
			MockAssignment a = (MockAssignment) Assignment.getAssignment();
			a.setBehavior(dm);
			CaseExecution execution = new CaseExecution(m, c);
			BPMNState state = execution.determineInitialState();
			Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
			assertTrue(enabledNodes.size() == 1);
			Iterator<BPMNNode> nodeIterator = enabledNodes.iterator(); 
			BPMNNode enabledNode = nodeIterator.next();
			if (enabledNode.getName().equals("Message")){
				messageEnabled = true;
			}else if (enabledNode.getName().equals("Timer")){
				timerEnabled = true;				
			}
		}
		assertTrue("Message event was never enabled. (Note: this could be a rare random occurrence. Validate by executing again.)", messageEnabled);
		assertTrue("Timer event was never enabled. (Note: this could be a rare random occurrence. Validate by executing again.)", timerEnabled);
	}

	@Test
	public void test32IncorrectDeferredChoice() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/32 incorrect deferred choice.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addOccurringEvent("Timer");
		DeclarativeModel dm = new DeclarativeModel();
		for (int i = 0; i < 10; i++){
			Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
			MockAssignment a = (MockAssignment) Assignment.getAssignment();
			a.setBehavior(dm);
			CaseExecution execution = new CaseExecution(m, c);
			BPMNState state = execution.determineInitialState();
			Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
			assertTrue(enabledNodes.size() == 1);
			Iterator<BPMNNode> nodeIterator = enabledNodes.iterator(); 
			BPMNNode enabledNode = nodeIterator.next();
			assertTrue(enabledNode.getName().equals("Timer"));
		}
	}

	@Test
	public void test41MultipleControlNodes1() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/41 multiple control nodes 1.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addValues("variable", "1", "1");
		DeclarativeModel dm = new DeclarativeModel();
		dm.addTransitionTouches("Start", "variable");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		BPMNNode enabledNode = enabledNodes.iterator().next();
		assertTrue(enabledNode.getName().equals("Task A"));
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 2);
		enabledNode = enabledNodes.iterator().next();
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode = enabledNodes.iterator().next();
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode = enabledNodes.iterator().next();
		assertTrue(enabledNode.getName().equals("End"));		
	}

	@Test
	public void test42MultipleControlNodes2() throws BPMNParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		BPMNParser parser = new BPMNParser();
		parser.parseFile("./ext/tests bpmn semantics/42 multiple control nodes 2.bpmn");
		BPMNModel m = parser.getParsedModel();
		Case c = new Case();
		c.addValues("variable", "1", "1");
		DeclarativeModel dm = new DeclarativeModel();
		dm.addTransitionTouches("Start", "variable");
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MockAssignment");
		MockAssignment a = (MockAssignment) Assignment.getAssignment();
		a.setBehavior(dm);
		CaseExecution execution = new CaseExecution(m, c);
		BPMNState state = execution.determineInitialState();
		Set<BPMNNode> enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 2);		
		Iterator<BPMNNode> nodeIterator = enabledNodes.iterator(); 
		BPMNNode enabledNode1 = nodeIterator.next();
		BPMNNode enabledNode2 = nodeIterator.next();		
		assertTrue((enabledNode1.getName().equals("Task C") && enabledNode2.getName().equals("Task D"))||
				   (enabledNode1.getName().equals("Task D") && enabledNode2.getName().equals("Task C")));
		if (enabledNode1.getName().equals("Task C")){
			state = execution.fireTransition(state, enabledNode1);
		}else{
			state = execution.fireTransition(state, enabledNode2);			
		}
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		BPMNNode enabledNode = enabledNodes.iterator().next();
		assertTrue(enabledNode.getName().equals("Task D"));
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 1);
		enabledNode = enabledNodes.iterator().next();
		assertTrue(enabledNode.getName().equals("End"));
		state = execution.fireTransition(state, enabledNode);
		enabledNodes = execution.determineEnabledNodes(state);
		assertTrue(enabledNodes.size() == 0);
	}
}
