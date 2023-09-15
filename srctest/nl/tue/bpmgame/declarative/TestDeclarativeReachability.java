package nl.tue.bpmgame.declarative;

import static org.junit.Assert.*;

import org.junit.Test;

import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmgame.executionmodel.DeclarativeStateSpace;

public class TestDeclarativeReachability {

	//NOTE: these tests only make sense if TestDeclarativeStateSpace succeeds
	@Test
	public void testSequenceReachability() throws Exception {
		Case c = new Case();
		c.setCompletionCondition("[A]");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("A","[start]");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);
		
		assertTrue(ss.firstTransitionOnPath(ss.initialState(), ss.finalStates()).equals("start"));
		assertTrue(ss.firstTransitionOnPath(ss.initialState(), ss.initialState()).equals(""));
		assertTrue(ss.firstTransitionOnPath(ss.initialState(), ss.getEnablingStates("start")).equals(""));
		assertTrue(ss.firstTransitionOnPath(ss.initialState(), ss.getEnablingStates("A")).equals("start"));
	}
	
	//NOTE: these tests only make sense if TestDeclarativeStateSpace succeeds
	@Test
	public void testParallellismReachability() throws Exception {
		Case c = new Case();
		c.setCompletionCondition("[A] AND [B]");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("A","[start]");
		testModel.addCondition("B","[start]");
		testModel.addCondition("C","[A] AND [B]");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);

		assertTrue(ss.firstTransitionOnPath(ss.initialState(), ss.finalStates()).equals("start"));
		assertTrue(ss.firstTransitionOnPath(ss.initialState(), ss.getEnablingStates("C")).equals("start"));
	}

	//NOTE: these tests only make sense if TestDeclarativeStateSpace succeeds
	@Test
	public void testChoiceReachability() throws Exception {
		Case c = new Case();
		c.setCompletionCondition("([A] OR [B]) AND [C]");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("A","[start] AND NOT [B]");
		testModel.addCondition("B","[start] AND NOT [A]");
		testModel.addCondition("C","[A] OR [B]");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);

		assertTrue(ss.firstTransitionOnPath(ss.initialState(), ss.finalStates()).equals("start"));
		assertTrue(ss.firstTransitionOnPath(ss.initialState(), ss.getEnablingStates("A")).equals("start"));
		assertTrue(ss.firstTransitionOnPath(ss.getEnablingStates("A").iterator().next(), ss.getEnablingStates("B")).equals(""));
		assertTrue(ss.firstTransitionOnPath(ss.getEnablingStates("start").iterator().next(), ss.getEnablingStates("C")).equals("start"));
	}

	//NOTE: these tests only make sense if TestDeclarativeStateSpace succeeds
	@Test
	public void testIterationReachability() throws Exception {
		Case c = new Case();
		c.setCompletionCondition("I > 1");
		c.addValues("I", "1", "2");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("A","[start] OR (I<2)");
		testModel.addCondition("B", "[A] AND (I=2) AND [start]");
		testModel.addTransitionTouches("A", "I");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);

		assertTrue(ss.firstTransitionOnPath(ss.initialState(), ss.finalStates()).equals("start"));
		assertTrue(ss.firstTransitionOnPath(ss.initialState(), ss.getEnablingStates("A")).equals("start"));
		assertTrue(ss.firstTransitionOnPath(ss.getEnablingStates("A").iterator().next(), ss.getEnablingStates("A")).equals(""));
		assertTrue(ss.firstTransitionOnPath(ss.getEnablingStates("A").iterator().next(), ss.getEnablingStates("B")).equals("A"));
	}

}
