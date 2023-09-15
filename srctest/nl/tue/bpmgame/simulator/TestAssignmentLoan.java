package nl.tue.bpmgame.simulator;

import static org.junit.Assert.*;

import org.junit.Test;

import nl.tue.bpmgame.declarative.DeclarativeModel;
import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmgame.executionmodel.DeclarativeStateSpace;
import nl.tue.bpmgame.executionmodel.State;

public class TestAssignmentLoan {

	@Test
	public void testFixUndecided() throws Exception {
		State.resetNextId();
		
		Case c = new Case();
		c.setCompletionCondition("CredibilityAssessment IN {accepted}");
		c.addValues("CredibilityAssessment", "undecided", "accepted");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("perform manual credit check","[start] OR (CredibilityAssessment IN {undecided})");
		testModel.addTransitionTouches("perform manual credit check", "CredibilityAssessment");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);

		assertTrue(ss.getStates().size() == 4);
		for (State s: ss.getStates()) {
			if (s.getId() == 0) {
				assertTrue((s.transitionOccurrences("start") == 0) && (s.transitionOccurrences("perform manual credit check") == 0));
			}
			if (s.getId() == 1) {
				assertTrue((s.transitionOccurrences("start") == 1) && (s.transitionOccurrences("perform manual credit check") == 0));
			}
			if (s.getId() == 2) {
				assertTrue((s.transitionOccurrences("start") == 1) && (s.transitionOccurrences("perform manual credit check") == 1));
			}
			if (s.getId() == 3) {
				assertTrue((s.transitionOccurrences("start") == 1) && (s.transitionOccurrences("perform manual credit check") == 2));
			}
		}
	}

	@Test
	public void testAlternatives() throws Exception {
		State.resetNextId();
		
		Case c = new Case();
		c.setCompletionCondition("(CredibilityAssessment IN {accepted,rejected}) AND (BKRAssessment IN {accepted,rejected}) AND (EVAAssessment IN {accepted,rejected})");
		c.addValues("CredibilityAssessment", "undecided", "accepted");
		c.addValues("BKRAssessment", "accepted", "accepted");
		c.addValues("EVAAssessment", "accepted", "accepted");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("perform manual credit check","[start] OR (CredibilityAssessment IN {undecided})");
		testModel.addCondition("perform manual BKR check","[start]");
		testModel.addCondition("perform manual EVA check","[start]");
		testModel.addCondition("perform manual credibility check","([start] OR (CredibilityAssessment IN {undecided})) AND (BKRAssessment IN {accepted,rejected}) AND (EVAAssessment IN {accepted,rejected})");
		testModel.addTransitionTouches("perform manual credit check", "CredibilityAssessment");
		testModel.addTransitionTouches("perform manual credit check", "BKRAssessment");
		testModel.addTransitionTouches("perform manual credit check", "EVAAssessment");
		testModel.addTransitionTouches("perform manual BKR check", "BKRAssessment");
		testModel.addTransitionTouches("perform manual EVA check", "EVAAssessment");
		testModel.addTransitionTouches("perform manual credibility check", "CredibilityAssessment");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);

		assertTrue(ss.finalStates().size() == 14);
		for (State s: ss.finalStates()) {
			assertTrue(Integer.toString(s.dataItemTouches("BKRAssessment")), s.dataItemTouches("BKRAssessment") > 0);
			assertTrue(Integer.toString(s.dataItemTouches("EVAAssessment")), s.dataItemTouches("EVAAssessment") > 0);
			assertTrue(Integer.toString(s.dataItemTouches("CredibilityAssessment")), s.dataItemTouches("CredibilityAssessment") > 1);
		}		
	}

	@Test
	public void testManualChecks() throws Exception {
		State.resetNextId();
		
		Case c = new Case();
		c.setCompletionCondition("CheckedCredibilityAssessment IN {accepted,rejected}");
		c.addValues("CredibilityAssessment", "undecided", "accepted");
		c.addValues("BKRAssessment", "accepted", "accepted");
		c.addValues("EVAAssessment", "accepted", "accepted");
		c.addValues("CheckedCredibilityAssessment", "accepted", "accepted");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("perform manual credit check","([start] OR (CredibilityAssessment IN {undecided})) AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		testModel.addCondition("perform manual BKR check","[start] AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		testModel.addCondition("perform manual EVA check","[start] AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		testModel.addCondition("perform manual credibility check","([start] OR (CredibilityAssessment IN {undecided})) AND (BKRAssessment IN {accepted,rejected}) AND (EVAAssessment IN {accepted,rejected}) AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		testModel.addCondition("confirm credibility check", "[start] AND CredibilityAssessment IN {accepted,rejected}");
		testModel.addCondition("end", "[start] AND CheckedCredibilityAssessment IN {accepted,rejected}");		
		testModel.addTransitionTouches("perform manual credit check", "CredibilityAssessment");
		testModel.addTransitionTouches("perform manual credit check", "BKRAssessment");
		testModel.addTransitionTouches("perform manual credit check", "EVAAssessment");
		testModel.addTransitionTouches("perform manual BKR check", "BKRAssessment");
		testModel.addTransitionTouches("perform manual EVA check", "EVAAssessment");
		testModel.addTransitionTouches("perform manual credibility check", "CredibilityAssessment");
		testModel.addTransitionTouches("confirm credibility check", "CheckedCredibilityAssessment");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);

		assertTrue(ss.finalStates().size() > 0);
		for (State s: ss.finalStates()) {
			assertTrue(s.dataItemTouches("BKRAssessment") > 0);
			assertTrue(s.dataItemTouches("EVAAssessment") > 0);
			assertTrue(s.dataItemTouches("CredibilityAssessment") > 1);
			assertTrue(s.dataItemTouches("CheckedCredibilityAssessment") > 0);
		}		
	}
	
	@Test
	public void testDeferredChoice() throws Exception {
		State.resetNextId();
		
		Case c = new Case();
		c.setCompletionCondition("['receive accepted offer']");
		c.addValues("Responded", "notresponded", "responded");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");		
		testModel.addCondition("send loan offer", "[start]");		
		testModel.addCondition("after one hour 1", "[start]");		
		testModel.addCondition("after one hour 2", "[start]");		
		testModel.addCondition("remind client", "['after one hour 1'] OR ['after one hour 2']");		
		testModel.addCondition("receive accepted offer", "['send loan offer'] AND Responded IN {responded}");		
		testModel.addTransitionTouches("send loan offer", "Responded");
		testModel.addTransitionTouches("remind client", "Responded");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);
		
		assertTrue(ss.finalStates().size() == 3);
		for (State s: ss.finalStates()) {
			assertTrue(s.dataItemTouches("Responded") > 1);
			assertTrue(s.transitionOccurrences("receive accepted offer") > 0);
			assertTrue((s.transitionOccurrences("after one hour 1") > 0) || (s.transitionOccurrences("after one hour 2") > 0));
		}		
	}
	
	@Test
	public void testCallBack() throws Exception {
		State.resetNextId();
		
		Case c = new Case();
		c.setCompletionCondition("PickedUp IN {pickedup}");
		c.addValues("PickedUp", "notpickedup", "pickedup");
		c.addValues("_Waited", "1", "2");
		c.addValues("CheckedCredibilityAssessment", "accepted", "accepted");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("confirm credibility check", "[start]");
		testModel.addCondition("after one hour 1", "[start] AND CheckedCredibilityAssessment IN {accepted}"); //Only enable from checked to prevent too large statespace		
		testModel.addCondition("after one hour 2", "[start] AND CheckedCredibilityAssessment IN {accepted}"); //Only enable from checked to prevent too large statespace		
		testModel.addCondition("call client to complement information", "[start] AND CheckedCredibilityAssessment IN {accepted}"
				                                                      + " OR "
				                                                      + "PickedUp IN {notpickedup} AND _Waited IN {1,2}");		
		testModel.addTransitionTouches("confirm credibility check", "CheckedCredibilityAssessment");
		testModel.addTransitionTouches("call client to complement information", "PickedUp");
		testModel.addTransitionTouches("after one hour 1", "_Waited");
		testModel.addTransitionTouches("after one hour 2", "_Waited");
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);

		assertTrue(ss.finalStates().size() > 0);
		for (State s: ss.finalStates()) {
			assertTrue(s.dataItemTouches("PickedUp") > 1);
			assertTrue(s.transitionOccurrences("call client to complement information") > 1);
		}				
	}

	@Test
	public void testCheckLoanOffer() throws Exception {
		State.resetNextId();
		
		Case c = new Case();
		c.setCompletionCondition("['send loan offer']");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("create loan offer", "[start]");
		testModel.addCondition("check loan offer", "['create loan offer'] AND NOT ['send loan offer']");
		testModel.addCondition("send loan offer", "['create loan offer']");
		
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);
		assertTrue(ss.finalStates().size() == 2);
		State[] fs = ss.finalStates().toArray(new State[]{});
		assertTrue(((fs[0].transitionOccurrences("check loan offer") == 1) && (fs[1].transitionOccurrences("check loan offer") == 0)) ||
				((fs[0].transitionOccurrences("check loan offer") == 0) && (fs[1].transitionOccurrences("check loan offer") == 1)));
	}

	@Test
	public void testValidateLoanActivation() throws Exception {
		State.resetNextId();
		
		Case c = new Case();
		c.setCompletionCondition("['return documents to client']");
		DeclarativeModel testModel = new DeclarativeModel();
		testModel.addCondition("start", "");
		testModel.addCondition("activate loan application", "[start]");
		testModel.addCondition("execute initial payment", "['activate loan application']");
		testModel.addCondition("validate loan activation", "['activate loan application']");
		testModel.addCondition("return documents to client", "['execute initial payment']");
		
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);
		System.out.println(ss);
		assertTrue(ss.finalStates().size() == 2);
		
		State[] fs = ss.finalStates().toArray(new State[]{});
		assertTrue(((fs[0].transitionOccurrences("validate loan activation") == 1) && (fs[1].transitionOccurrences("validate loan activation") == 0)) ||
				((fs[0].transitionOccurrences("validate loan activation") == 0) && (fs[1].transitionOccurrences("validate loan activation") == 1)));
	}

	@Test
	public void testFullAssignment() throws Exception {
		State.resetNextId();
		
		Case c = new Case();
		c.setCompletionCondition("[end]");
		c.addValues("LoanDetails", "received", "received");
		c.addValues("BKRAssessment", "accepted", "accepted");
		c.addValues("EVAAssessment", "accepted", "accepted");
		c.addValues("CredibilityAssessment", "undecided", "accepted");
		c.addValues("CheckedCredibilityAssessment", "accepted", "accepted");
		c.addValues("RejectionLetter", "sent", "sent");
		c.addValues("PickedUp", "notpickedup", "pickedup");
		c.addValues("LoanOffer", "created", "created");
		c.addValues("AcceptedOffer", "received", "received");
		c.addValues("LoanActivation", "completed", "completed");
		c.addValues("InitialPayment", "completed", "completed");
		
		c.addValues("_Waited", "1", "2");
		c.addValues("_Responded", "notresponded", "responded");

		//Initialize declarative model
		DeclarativeModel testModel = new DeclarativeModel();
		
		testModel.addCondition("start", "");
		testModel.addTransitionTouches("start", "LoanDetails");
		
		//Do all checks
		testModel.addCondition("perform credit check","([start] OR (CredibilityAssessment IN {undecided})) AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		testModel.addCondition("perform BKR check","[start] AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		testModel.addCondition("perform EVA check","[start] AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		testModel.addCondition("perform credibility check","([start] OR (CredibilityAssessment IN {undecided})) AND (BKRAssessment IN {accepted,rejected}) AND (EVAAssessment IN {accepted,rejected}) AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		testModel.addCondition("confirm credibility check", "[start] AND CredibilityAssessment IN {accepted,rejected}");
		testModel.addTransitionTouches("perform credit check", "CredibilityAssessment");
		testModel.addTransitionTouches("perform credit check", "BKRAssessment");
		testModel.addTransitionTouches("perform credit check", "EVAAssessment");
		testModel.addTransitionTouches("perform BKR check", "BKRAssessment");
		testModel.addTransitionTouches("perform EVA check", "EVAAssessment");
		testModel.addTransitionTouches("perform credibility check", "CredibilityAssessment");
		testModel.addTransitionTouches("confirm credibility check", "CheckedCredibilityAssessment");
		
		//send rejection letter
		testModel.addCondition("send rejection letter", "[start] AND CheckedCredibilityAssessment IN {rejected}");
		testModel.addTransitionTouches("send rejection letter", "RejectionLetter");
		
		//call client to complement information
		testModel.addCondition("after one hour 1", "[start] AND CheckedCredibilityAssessment IN {accepted}"); //Only enable from checked to prevent too large statespace		
		testModel.addCondition("after one hour 2", "[start] AND CheckedCredibilityAssessment IN {accepted}"); //Only enable from checked to prevent too large statespace		
		testModel.addCondition("call client to complement information", "[start] AND CheckedCredibilityAssessment IN {accepted}"
				                                                      + " OR "
				                                                      + "PickedUp IN {notpickedup} AND _Waited IN {1,2}");		
		testModel.addTransitionTouches("call client to complement information", "PickedUp");
		testModel.addTransitionTouches("after one hour 1", "_Waited");
		testModel.addTransitionTouches("after one hour 2", "_Waited");
		
		//create loan offer, send loan offer
		testModel.addCondition("create loan offer", "[start] AND PickedUp IN {pickedup}");
		testModel.addCondition("send loan offer", "['create loan offer']");
		testModel.addTransitionTouches("create loan offer", "LoanOffer");
		testModel.addTransitionTouches("send loan offer", "_Responded");
		
		//remind client, receive accepted offer
		testModel.addCondition("remind client", "['after one hour 1'] OR ['after one hour 2']");		
		testModel.addCondition("receive accepted offer", "['send loan offer'] AND _Responded IN {responded}");		
		testModel.addTransitionTouches("remind client", "_Responded");
		testModel.addTransitionTouches("receive accepted offer", "AcceptedOffer");		
		
		//send welcome letter, activate loan application, execute initial payment, return documents to client
		testModel.addCondition("send welcome letter", "['receive accepted offer']");
		testModel.addCondition("activate loan application", "['receive accepted offer']");
		testModel.addCondition("execute initial payment", "['activate loan application']");
		testModel.addCondition("return documents to client", "['execute initial payment']");
		testModel.addTransitionTouches("activate loan application", "LoanActivation");
		testModel.addTransitionTouches("execute initial payment", "InitialPayment");
		
		//end
		testModel.addCondition("end", "['send welcome letter'] AND ['return documents to client'] OR ['send rejection letter']");
		
		DeclarativeStateSpace ss = testModel.computeStateSpace(c);
		assertTrue(!ss.finalStates().isEmpty());
		for (State s: ss.finalStates()) {
			assertTrue(s.transitionOccurrences("end") > 0);
			assertTrue(s.transitionOccurrences("call client to complement information") > 1);
			assertTrue(s.transitionOccurrences("remind client") > 0);
			assertTrue(s.transitionOccurrences("send welcome letter") > 0);
			assertTrue(s.transitionOccurrences("return documents to client") > 0);
			assertTrue(s.dataItemTouches("PickedUp") > 1);
			assertTrue(s.dataItemTouches("_Responded") > 1);
		}
	}
}
