package nl.tue.bpmgame.declarative;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmgame.executionmodel.State;
import nl.tue.bpmgame.executionmodel.DeclarativeStateSpace;

public class TestDeclarativeStateSpace {
	
	@Test
	public void testSequence() throws Exception {
		Case c = new Case();
		c.setCompletionCondition("[A]");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("A","[start]");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);
		assertTrue("error in statespace generation: not the expected number of states", ss.getStates().size() == 3);
		assertTrue(ss.initialState() != null);
		assertTrue("the completion condition failed (may also be caused by error in statespace generation)", ss.finalStates().size() == 1);
		
		State expectedFinalState = new State();
		expectedFinalState.addTransition("start");
		expectedFinalState.addTransition("A");
		assertTrue("the final state is not as expeced", ss.finalStates().iterator().next().equals(expectedFinalState));
	}
	
	@Test
	public void testParallellism() throws Exception {
		Case c = new Case();
		c.setCompletionCondition("[A] AND [B]");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("A","[start]");
		testModel.addCondition("B","[start]");
		testModel.addCondition("C","[A] AND [B]");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);
		assertTrue("error in statespace generation: not the expected number of states", ss.getStates().size() == 6);
		assertTrue(ss.initialState() != null);
		assertTrue("the completion condition failed (may also be caused by error in statespace generation)", ss.finalStates().size() == 2);
		Iterator<State> dsi = ss.finalStates().iterator();
		State finalState1 = dsi.next();
		State finalState2 = dsi.next();

		State expectedFinalState1 = new State();
		expectedFinalState1.addTransition("start");
		expectedFinalState1.addTransition("A");
		expectedFinalState1.addTransition("B");
		State expectedFinalState2 = new State();
		expectedFinalState2.addTransition("start");
		expectedFinalState2.addTransition("A");
		expectedFinalState2.addTransition("B");
		expectedFinalState2.addTransition("C");
		assertTrue("the final states are not as expeced", (finalState1.equals(expectedFinalState1) && finalState2.equals(expectedFinalState2)) || 
				                                          (finalState1.equals(expectedFinalState2) && finalState2.equals(expectedFinalState1)));
	}

	@Test
	public void testChoice() throws Exception {
		Case c = new Case();
		c.setCompletionCondition("([A] OR [B]) AND [C]");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("A","[start] AND NOT [B]");
		testModel.addCondition("B","[start] AND NOT [A]");
		testModel.addCondition("C","[A] OR [B]");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);
		assertTrue("error in statespace generation: not the expected number of states", ss.getStates().size() == 6);
		assertTrue(ss.initialState() != null);
		assertTrue("the completion condition failed (may also be caused by error in statespace generation)", ss.finalStates().size() == 2);
		State finalState1 = (State) ss.finalStates().toArray()[0];
		State finalState2 = (State) ss.finalStates().toArray()[1];		

		State expectedFinalState1 = new State();
		expectedFinalState1.addTransition("start");
		expectedFinalState1.addTransition("A");
		expectedFinalState1.addTransition("C");
		State expectedFinalState2 = new State();
		expectedFinalState2.addTransition("start");
		expectedFinalState2.addTransition("B");
		expectedFinalState2.addTransition("C");
		assertTrue("the final states are not as expeced", (finalState1.equals(expectedFinalState1) && finalState2.equals(expectedFinalState2)) || 
				                                          (finalState1.equals(expectedFinalState2) && finalState2.equals(expectedFinalState1)));
	}

	@Test
	public void testIteration() throws Exception {
		Case c = new Case();
		c.setCompletionCondition("I > 1");
		c.addValues("I", "1", "2");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("A","[start] OR (I<2)");
		testModel.addTransitionTouches("A", "I");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);
		assertTrue("error in statespace generation: not the expected number of states", ss.getStates().size() == 4);
		assertTrue(ss.initialState() != null);
		assertTrue("the completion condition failed (may also be caused by error in statespace generation)", ss.finalStates().size() == 1);

		State expectedFinalState = new State();
		expectedFinalState.addTransition("start");
		expectedFinalState.addTransition("A");
		expectedFinalState.addTransition("A");
		expectedFinalState.addDataItem("I");
		expectedFinalState.addDataItem("I");
		assertTrue("the final state is not as expeced", ss.finalStates().iterator().next().equals(expectedFinalState));
	}
}
