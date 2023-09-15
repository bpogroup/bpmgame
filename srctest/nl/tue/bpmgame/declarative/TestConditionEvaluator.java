package nl.tue.bpmgame.declarative;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmgame.executionmodel.State;
import nl.tue.bpmn.parser.ConditionEvaluator;

public class TestConditionEvaluator {

	@Test
	public void testOccurrenceCondition() {
		State s = new State();
		assertFalse(ConditionEvaluator.getInstance().evaluate("[A]", new Case(), s, "B"));
		
		s.addTransition("A");
		assertTrue(ConditionEvaluator.getInstance().evaluate("[A]", new Case(), s, "B"));
		
		s.addTransition("B");
		assertFalse(ConditionEvaluator.getInstance().evaluate("[A]", new Case(), s, "B"));
		
		s.addTransition("A");
		assertTrue(ConditionEvaluator.getInstance().evaluate("[A]", new Case(), s, "B"));
	}

	@Test
	public void testNominalDataCondition() {
		State s = new State();
		Case c = new Case();
		assertFalse(ConditionEvaluator.getInstance().evaluate("A IN {b,c}", c, s, "B"));

		s = new State();
		c = new Case();
		c.addValues("A", "b", "b");		
		assertFalse(ConditionEvaluator.getInstance().evaluate("A IN {b,c}", c, s, "B"));

		s = new State();
		c = new Case();
		s.addDataItem("A");		
		assertFalse(ConditionEvaluator.getInstance().evaluate("A IN {b,c}", c, s, "B"));
		
		s = new State();
		c = new Case();
		s.addDataItem("A");		
		c.addValues("A", "b", "b");		
		assertTrue(ConditionEvaluator.getInstance().evaluate("A IN {b,c}", c, s, "B"));

		s = new State();
		c = new Case();
		s.addDataItem("A");		
		c.addValues("A", "c", "c");		
		assertTrue(ConditionEvaluator.getInstance().evaluate("A IN {b,c}", c, s, "B"));
	}

	@Test
	public void testNumericDataCondition() {
		State s = new State();
		Case c = new Case();
		s.addDataItem("A");		
		c.addValues("A", "1", "1");		
		assertFalse(ConditionEvaluator.getInstance().evaluate("A > 2", c, s, "B"));		
		assertTrue(ConditionEvaluator.getInstance().evaluate("A < 2", c, s, "B"));		
	}
	
	@Test
	public void testComplexCondition() {
		State s = new State();
		Case c = new Case();
		s.addDataItem("A");		
		c.addValues("A", "c", "c");
		
		s.addDataItem("B");		
		c.addValues("B", "1", "1");
		
		s.addTransition("C");

		assertTrue(ConditionEvaluator.getInstance().evaluate("NOT ((B > 2) AND (NOT [C]) AND (NOT A IN {c}))", c, s, "D"));		
	}

	@Test
	public void testORCondition() {
		State s = new State();
		Case c = new Case();
		s.addDataItem("EVAAssessment");		
		c.addValues("EVAAssessment", "rejected", "rejected");
		
		s.addDataItem("BKRAssessment");		
		c.addValues("BKRAssessment", "rejected", "rejected");
		
		s.addTransition("C");
			
		List<String> errs = ConditionEvaluator.getInstance().validate("(EVAAssessment IN {rejected}) OR (BKRAssessment IN {rejected})");
		for (String err: errs) {
			System.out.println(err);
		}
		assertTrue(ConditionEvaluator.getInstance().evaluate("EVAAssessment IN {rejected} OR BKRAssessment IN {rejected}", c, s, "D"));		
	}
}
