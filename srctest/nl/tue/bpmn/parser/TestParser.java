package nl.tue.bpmn.parser;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import nl.tue.bpmn.concepts.BPMNArc;
import nl.tue.bpmn.concepts.BPMNModel;

public class TestParser {

	@Test
	public void test00UnacceptableElements() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/00 unacceptable elements.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model contains an illegal model element"));
		}
	}

	@Test
	public void test01UnconnectedArc() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/01 unconnected arc.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("'sourceRef' must appear"));
		}
	}

	@Test
	public void test02LabelAppearsTwice() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/02 label appears twice.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("There are two elements in the model that have the name 'Duplicate Task'."));
		}
	}

	@Test
	public void test10NoLane() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/10 no lane.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model has no roles."));
		}
	}

	@Test
	public void test11UnlabeledLane() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/11 unlabeled lane.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model contains a lane that has no name."));
		}
	}

	@Test
	public void test12OutOfLane() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/12 out of lane.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The task 'Task' is not contained in a lane."));
		}
	}

	@Test
	public void test21MultipleStartEvents() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/21 multiple start events.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model contains multiple start events."));
		}
	}

	@Test
	public void test22UnlabeledStartEvent() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/22 unlabeled start event.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model contains a start event that has no name."));
		}
	}

	@Test
	public void test23StartMultipleOutgoing() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/23 start multiple outgoing.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("Start event 'Start' does not have exactly one outgoing arc and zero incoming arcs."));
		}
	}

	@Test
	public void test24NoStart() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/24 no start.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model has no start event."));
		}
	}

	@Test
	public void test25StartNoOutgoing() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/25 start no outgoing.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("Start event 'Start' does not have exactly one outgoing arc and zero incoming arcs."));
		}
	}

	@Test
	public void test31UnlabeledTask() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/31 unlabeled task.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model contains a task that has no name."));
		}
	}

	@Test
	public void test32TaskMultipleIncomingOutgoing() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/32 task multiple incoming outgoing.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("Task 'Problematic Task' does not have exactly one incoming and one outgoig arc."));
		}
	}

	@Test
	public void test33TaskNoIncomingOutgoing() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/33 task no incoming outgoing.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("Task 'Problematic Task' does not have exactly one incoming and one outgoig arc."));
		}
	}

	@Test
	public void test41UnlabeledIntermediate() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/41 unlabeled intermediate.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model contains an intermediate event that has no name."));
		}
	}

	@Test
	public void test42IntermediateMultipleIncomingOutgoing() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/42 intermediate multiple incoming outgoing.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("Intermediate event 'Problematic Event' does not have exactly one incoming and one outgoig arc."));
			//System.out.println(e.getMessage());
		}
	}

	@Test
	public void test43IntermediateNoIncomingOutgoing() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/43 intermediate no incoming outgoing.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("Intermediate event 'Problematic Event' does not have exactly one incoming and one outgoig arc."));
		}
	}

	@Test
	public void test51UnlabeledEnd() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/51 unlabeled end.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model contains an end event that has no name."));
		}
	}

	@Test
	public void test52EndMultipleIncoming() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/52 end multiple incoming.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("End event 'End' does not have exactly one incoming arc and zero outgoing arcs."));
		}
	}

	@Test
	public void test53EndNoIncoming() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/53 end no incoming.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("End event 'Problematic End' does not have exactly one incoming arc and zero outgoing arcs."));
		}
	}

	@Test
	public void test61ChoiceMultipleIncomingOutgoing() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/61 choice multiple incoming outgoing.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model contains an exclusive gateway with multiple incoming and outgoing arcs."));
		}
	}

	@Test
	public void test62ParallelMultipleIncomingOutgoing() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/62 parallel multiple incoming outgoing.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("The model contains a parallel gateway with multiple incoming and outgoing arcs."));
		}
	}

	@Test
	public void test70ErroneousConditionOnArc() {
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/70 erroneous condition on arc.bpmn");
			fail("exception should be thrown");
		} catch (BPMNParseException e) {
			assertTrue(e.getMessage().contains("Error at position 5 in expression 'error': no viable alternative at input 'error'"));
		}
	}

	@Test
	public void test80AllValidElements() {
		String expectedResults[] = {
				"StartEvent(Start) --> Task(Task A)",
				"Task(Task A) --> XOR-Split",
				"XOR-Split -variable < 1-> Task(Task B)",
				"XOR-Split -variable >= 1-> Task(Task C)",
				"Task(Task B) --> XOR-Join",
				"Task(Task C) --> XOR-Join",
				"XOR-Join --> AND-Split",
				"AND-Split --> Task(Task D)",
				"AND-Split --> Task(Task E)",
				"Task(Task D) --> AND-Join",
				"Task(Task E) --> AND-Join",
				"AND-Join --> Event-Split",
				"Event-Split --> IntermediateEvent(Message)",
				"Event-Split --> IntermediateEvent(Timer)",
				"IntermediateEvent(Timer) --> XOR-Join",
				"IntermediateEvent(Message) --> XOR-Join",				
				"XOR-Join --> EndEvent(End)"};
		BPMNParser parser = new BPMNParser();
		try {
			parser.parseFile("./ext/tests bpmn syntax/80 all valid elements.bpmn");
			BPMNModel model = parser.getParsedModel();
			Set<Integer> results = new HashSet<Integer>();
			for (BPMNArc a: model.getArcs()){
				boolean resultFound = false;
				for (Integer i = 0; i < expectedResults.length; i++){
					if (a.toString().equals(expectedResults[i])){
						results.add(i);
						resultFound = true;
						break;
					}
				}
				assertTrue("an unexpected result was produced: " + a.toString(), resultFound);
			}
			assertEquals("not all results were equal", expectedResults.length, results.size());
		} catch (BPMNParseException e) {
			fail("an exception should not be thrown");
		}
	}

}
