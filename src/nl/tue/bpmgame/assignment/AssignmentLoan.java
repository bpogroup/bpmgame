package nl.tue.bpmgame.assignment;

import java.util.Arrays;
import java.util.HashSet;

import nl.tue.bpmgame.declarative.DeclarativeModel;
import nl.tue.util.RandomGenerator;
import nl.tue.util.TimeUtils;

public class AssignmentLoan extends Assignment{

	@Override
	protected void createAssignment() {
		
		addSkill("risk management");
		addSkill("senior risk management");
		addSkill("administration");
		addSkill("customer contact");
		addSkill("finance");
		addSkill(Assignment.SYSTEM_SKILL);

		addTask(new Task("perform credit check", TimeUtils.MINUTES_05, TimeUtils.MINUTES_25, "risk management", 0));
		addTask(new Task("perform BKR check", TimeUtils.MINUTES_025, TimeUtils.MINUTES_075, "risk management", 0));
		addTask(new Task("perform EVA check", TimeUtils.MINUTES_025, TimeUtils.MINUTES_075, "risk management", 0));
		addTask(new Task("perform credibility check", TimeUtils.MINUTES_025, TimeUtils.MINUTES_075, "risk management", 0));
		
		addTask(new Task("confirm credibility check", TimeUtils.MINUTES_10, TimeUtils.MINUTES_20, "senior risk management", 0));
		
		addTask(new Task("send rejection letter", TimeUtils.MINUTES_05, TimeUtils.MINUTES_15, "administration", 0));
		addTask(new Task("call client to complement information", TimeUtils.MINUTES_025, TimeUtils.MINUTES_075, "customer contact", 0));
		addTask(new Task("create loan offer", TimeUtils.MINUTES_10, TimeUtils.MINUTES_30, "customer contact", 0));
		addTask(new Task("check loan offer", TimeUtils.MINUTES_025, TimeUtils.MINUTES_05, "customer contact", 1));
		addTask(new Task("send loan offer", TimeUtils.MINUTES_025, TimeUtils.MINUTES_075, "customer contact", 0));
		addTask(new Task("remind client", TimeUtils.MINUTES_025, TimeUtils.MINUTES_075, "customer contact", 0));
		addTask(new Task("send welcome letter", TimeUtils.MINUTES_05, TimeUtils.MINUTES_15, "administration", 0));
		addTask(new Task("activate loan application", TimeUtils.MINUTES_05, TimeUtils.MINUTES_15, "finance", 0));
		addTask(new Task("validate loan activation", TimeUtils.MINUTES_025, TimeUtils.MINUTES_05, "finance", 0));
		addTask(new Task("execute initial payment", TimeUtils.MINUTES_05, TimeUtils.MINUTES_15, "finance", 0));
		addTask(new Task("return documents to client", TimeUtils.MINUTES_05, TimeUtils.MINUTES_15, "administration", 0));	
				
		addEvent(new Event("start", 0, 0));
		addEvent(new Event("end", 0, 0));
		addEvent(new Event("after one hour 1", TimeUtils.HOUR, TimeUtils.HOUR));
		addEvent(new Event("after one hour 2", TimeUtils.HOUR, TimeUtils.HOUR));
		addEvent(new Event("receive accepted offer", 0, TimeUtils.HOUR));		
		
		addResource(new Resource("Jerome", new String[] {"administration"}, false, 15));
		addResource(new Resource("Rafael", new String[] {"administration"}, false, 15));
		addResource(new Resource("Cecily", new String[] {"risk management","senior risk management","finance"}, false, 22));
		addResource(new Resource("Minh", new String[] {"risk management","finance"}, false, 17));
		addResource(new Resource("Frank", new String[] {"risk management"}, false, 17));
		addResource(new Resource("Madeline", new String[] {"customer contact"}, false, 15));
		addResource(new Resource("Murray", new String[] {"risk management","senior risk management","customer contact","finance"}, false, 22));
		addResource(new Resource("Ismael", new String[] {"customer contact"}, false, 15));
		addResource(new Resource("Mose", new String[] {"risk management","senior risk management", "administration","customer contact","finance"}, false, 25));
		addResource(new Resource("Marisela", new String[] {"administration"}, true, 40));
		addResource(new Resource(Assignment.GENERAL_MANAGER, new String[] {"risk management","senior risk management","administration","customer contact","finance"}, true, 60));
		addResource(new Resource(Assignment.INFORMATION_SYSTEM, new String[] {Assignment.SYSTEM_SKILL}, true, 0));

		addDataItem("LoanDetails", new String[]{"received"});
		addDataItem("BKRAssessment", new String[]{"accepted","rejected"});
		addDataItem("EVAAssessment", new String[]{"accepted","rejected"});
		addDataItem("CredibilityAssessment", new String[]{"accepted","rejected","undecided"});
		addDataItem("CheckedCredibilityAssessment", new String[]{"accepted","rejected"});
		addDataItem("RejectionLetter", new String[]{"sent"});
		addDataItem("PickedUp", new String[]{"pickedup","notpickedup"});
		addDataItem("LoanOffer", new String[]{"created"});
		addDataItem("AcceptedOffer", new String[]{"received"});
		addDataItem("LoanActivation", new String[]{"completed"});
		addDataItem("InitialPayment", new String[]{"completed"});

		DeclarativeModel behavior = new DeclarativeModel();
		
		//Start
		behavior.addCondition("start", "");
		behavior.addTransitionTouches("start", "LoanDetails");
		
		//Do all checks
		behavior.addCondition("perform credit check","([start] OR (CredibilityAssessment IN {undecided})) AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		behavior.addCondition("perform BKR check","[start] AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		behavior.addCondition("perform EVA check","[start] AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		behavior.addCondition("perform credibility check","([start] OR (CredibilityAssessment IN {undecided})) AND (BKRAssessment IN {accepted,rejected}) AND (EVAAssessment IN {accepted,rejected}) AND NOT (CheckedCredibilityAssessment IN {accepted,rejected})");
		behavior.addCondition("confirm credibility check", "[start] AND CredibilityAssessment IN {accepted,rejected}");
		behavior.addTransitionTouches("perform credit check", "CredibilityAssessment");
		behavior.addTransitionTouches("perform credit check", "BKRAssessment");
		behavior.addTransitionTouches("perform credit check", "EVAAssessment");
		behavior.addTransitionTouches("perform BKR check", "BKRAssessment");
		behavior.addTransitionTouches("perform EVA check", "EVAAssessment");
		behavior.addTransitionTouches("perform credibility check", "CredibilityAssessment");
		behavior.addTransitionTouches("confirm credibility check", "CheckedCredibilityAssessment");
		
		//send rejection letter
		behavior.addCondition("send rejection letter", "[start] AND CheckedCredibilityAssessment IN {rejected}");
		behavior.addTransitionTouches("send rejection letter", "RejectionLetter");
		
		//call client to complement information
		behavior.addCondition("after one hour 1", "[start] AND CheckedCredibilityAssessment IN {accepted}"); //Only enable from checked to prevent too large statespace		
		behavior.addCondition("after one hour 2", "[start] AND CheckedCredibilityAssessment IN {accepted}"); //Only enable from checked to prevent too large statespace		
		behavior.addCondition("call client to complement information", "[start] AND CheckedCredibilityAssessment IN {accepted}"
				                                                      + " OR "
				                                                      + "PickedUp IN {notpickedup} AND _Waited IN {1,2}");		
		behavior.addTransitionTouches("call client to complement information", "PickedUp");
		behavior.addTransitionTouches("after one hour 1", "_Waited");
		behavior.addTransitionTouches("after one hour 2", "_Waited");
		
		//create loan offer, check loan offer, send loan offer
		behavior.addCondition("create loan offer", "[start] AND PickedUp IN {pickedup}");
		behavior.addCondition("check loan offer", "['create loan offer'] AND NOT ['send loan offer']");
		behavior.addCondition("send loan offer", "['create loan offer']");
		behavior.addTransitionTouches("create loan offer", "LoanOffer");
		behavior.addTransitionTouches("send loan offer", "_Responded");
		
		//remind client, receive accepted offer
		behavior.addCondition("remind client", "['after one hour 1'] OR ['after one hour 2']");		
		behavior.addCondition("receive accepted offer", "['send loan offer'] AND _Responded IN {responded}");		
		behavior.addTransitionTouches("remind client", "_Responded");
		behavior.addTransitionTouches("receive accepted offer", "AcceptedOffer");		
		
		//send welcome letter, activate loan application, execute initial payment, return documents to client
		behavior.addCondition("send welcome letter", "['receive accepted offer']");
		behavior.addCondition("activate loan application", "['receive accepted offer']");
		behavior.addCondition("validate loan activation", "['activate loan application'] AND NOT ['execute initial payment']");
		behavior.addCondition("execute initial payment", "['activate loan application']");
		behavior.addCondition("return documents to client", "['execute initial payment']");
		behavior.addTransitionTouches("activate loan application", "LoanActivation");
		behavior.addTransitionTouches("execute initial payment", "InitialPayment");
		
		//end
		behavior.addCondition("end", "['send welcome letter'] AND ['return documents to client'] OR ['send rejection letter']");
		
		addBehavior(behavior);

		/* CASE TYPES (arrival, duration)
		 * happy flow	1 p/h		100 min.
		 * reject BKR	0.125 p/h	40 min.
		 * reject EVA	0.125 p/h	40 min.
		 * reject cred	0.125 p/h	40 min.
		 * reject conf	0.125 p/h	40 min.
		 * unresponsive	0.25 p/h	105 min.
		 * remind		0.25 p/h	105 min.
		 * TOTAL		2 p/h = 172.5 min./h
		 */
		CaseType happyFlow = new CaseType((long) (0*TimeUtils.HOUR), (long) (2*TimeUtils.HOUR), "[end]", 5*TimeUtils.HOUR, new String[] {"Start","after one hour 1", "after one hour 2", "receive accepted offer", "End"});
		happyFlow.setName("Happy Flow");
		happyFlow.addAttribute("LoanDetails", "[{anytime, [{received, 100%}]}]");
		happyFlow.addAttribute("BKRAssessment", "[{anytime, [{accepted, 100%}]}]");
		happyFlow.addAttribute("EVAAssessment", "[{anytime, [{accepted, 100%}]}]");
		happyFlow.addAttribute("CredibilityAssessment", "[{firsttime, [{undecided, 10%},{accepted, 90%}]},{secondtime, [{accepted, 100%}]}]");
		happyFlow.addAttribute("CheckedCredibilityAssessment", "[{anytime, [{accepted, 100%}]}]");
		happyFlow.addAttribute("RejectionLetter", "[{anytime, [{sent, 100%}]}]");
		happyFlow.addAttribute("PickedUp", "[{anytime, [{pickedup, 100%}]}]");
		happyFlow.addAttribute("LoanOffer", "[{anytime, [{created, 100%}]}]");
		happyFlow.addAttribute("AcceptedOffer", "[{anytime, [{received, 100%}]}]");
		happyFlow.addAttribute("LoanActivation", "[{anytime, [{completed, 100%}]}]");
		happyFlow.addAttribute("InitialPayment", "[{anytime, [{completed, 100%}]}]");
		happyFlow.addAttribute("_Waited", "[{firsttime, N(1,0)}, {secondtime, N(2,0)}]");
		happyFlow.addAttribute("_Responded", "[{anytime, [{responded, 100%}]}]");
		happyFlow.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform credit check", "perform BKR check")));
		happyFlow.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform EVA check")));
		happyFlow.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("create loan offer", "send loan offer")));
		happyFlow.addRedoElephantTrail(10, new HashSet<String>(Arrays.asList("activate loan application")));
		happyFlow.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("confirm credibility check")));
		happyFlow.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("execute initial payment")));
		happyFlow.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("send rejection letter")));
		happyFlow.addRepeatElephantTrail(10, "perform credit check", "confirm credibility check");
		happyFlow.addRepeatElephantTrail(10, "perform BKR check", "confirm credibility check");
		happyFlow.addRepeatElephantTrail(10, "create loan offer", "send welcome letter");
		happyFlow.addRepeatElephantTrail(10, "activate loan application", "return documents to client");
		addCaseType(happyFlow);
		
		CaseType rejectBKR = new CaseType((long) (0*TimeUtils.HOUR), (long) (10*TimeUtils.HOUR), "[end]", 3*TimeUtils.HOUR, new String[] {"Start","after one hour 1", "after one hour 2", "receive accepted offer", "End"});
		rejectBKR.setName("Reject BKR");
		rejectBKR.addAttribute("LoanDetails", "[{anytime, [{received, 100%}]}]");
		rejectBKR.addAttribute("BKRAssessment", "[{anytime, [{rejected, 100%}]}]");
		rejectBKR.addAttribute("EVAAssessment", "[{anytime, [{accepted, 100%}]}]");
		rejectBKR.addAttribute("CredibilityAssessment", "[{firsttime, [{undecided, 10%},{rejected, 90%}]},{secondtime, [{rejected, 100%}]}]");
		rejectBKR.addAttribute("CheckedCredibilityAssessment", "[{anytime, [{rejected, 100%}]}]");
		rejectBKR.addAttribute("RejectionLetter", "[{anytime, [{sent, 100%}]}]");
		rejectBKR.addAttribute("PickedUp", "[{anytime, [{pickedup, 100%}]}]");
		rejectBKR.addAttribute("LoanOffer", "[{anytime, [{created, 100%}]}]");
		rejectBKR.addAttribute("AcceptedOffer", "[{anytime, [{received, 100%}]}]");
		rejectBKR.addAttribute("LoanActivation", "[{anytime, [{completed, 100%}]}]");
		rejectBKR.addAttribute("InitialPayment", "[{anytime, [{completed, 100%}]}]");
		rejectBKR.addAttribute("_Waited", "[{firsttime, N(1,0)}, {secondtime, N(2,0)}]");
		rejectBKR.addAttribute("_Responded", "[{anytime, [{responded, 100%}]}]");
		rejectBKR.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform credit check", "perform BKR check")));
		rejectBKR.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform EVA check")));
		rejectBKR.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("confirm credibility check")));
		rejectBKR.addRepeatElephantTrail(10, "perform credit check", "confirm credibility check");
		rejectBKR.addRepeatElephantTrail(10, "perform BKR check", "confirm credibility check");
		rejectBKR.addRepeatElephantTrail(10, "confirm credibility check", "send rejection letter");
		addCaseType(rejectBKR);

		CaseType rejectEVA = new CaseType((long) (1*TimeUtils.HOUR), (long) (9*TimeUtils.HOUR), "[end]", 3*TimeUtils.HOUR, new String[] {"Start","after one hour 1", "after one hour 2", "receive accepted offer", "End"});
		rejectEVA.setName("Reject EVA");
		rejectEVA.addAttribute("LoanDetails", "[{anytime, [{received, 100%}]}]");
		rejectEVA.addAttribute("BKRAssessment", "[{anytime, [{accepted, 100%}]}]");
		rejectEVA.addAttribute("EVAAssessment", "[{anytime, [{rejected, 100%}]}]");
		rejectEVA.addAttribute("CredibilityAssessment", "[{firsttime, [{undecided, 10%},{rejected, 90%}]},{secondtime, [{rejected, 100%}]}]");
		rejectEVA.addAttribute("CheckedCredibilityAssessment", "[{anytime, [{rejected, 100%}]}]");
		rejectEVA.addAttribute("RejectionLetter", "[{anytime, [{sent, 100%}]}]");
		rejectEVA.addAttribute("PickedUp", "[{anytime, [{pickedup, 100%}]}]");
		rejectEVA.addAttribute("LoanOffer", "[{anytime, [{created, 100%}]}]");
		rejectEVA.addAttribute("AcceptedOffer", "[{anytime, [{received, 100%}]}]");
		rejectEVA.addAttribute("LoanActivation", "[{anytime, [{completed, 100%}]}]");
		rejectEVA.addAttribute("InitialPayment", "[{anytime, [{completed, 100%}]}]");
		rejectEVA.addAttribute("_Waited", "[{firsttime, N(1,0)}, {secondtime, N(2,0)}]");
		rejectEVA.addAttribute("_Responded", "[{anytime, [{responded, 100%}]}]");
		rejectEVA.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform credit check", "perform BKR check")));
		rejectEVA.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform EVA check")));
		rejectEVA.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("confirm credibility check")));
		rejectEVA.addRepeatElephantTrail(10, "perform credit check", "confirm credibility check");
		rejectEVA.addRepeatElephantTrail(10, "perform BKR check", "confirm credibility check");
		rejectEVA.addRepeatElephantTrail(10, "confirm credibility check", "send rejection letter");
		addCaseType(rejectEVA);

		CaseType rejectCredibility = new CaseType((long) (5*TimeUtils.HOUR), (long) (8*TimeUtils.HOUR), "[end]", 3*TimeUtils.HOUR, new String[] {"Start","after one hour 1", "after one hour 2", "receive accepted offer", "End"});
		rejectCredibility.setName("Reject Credibility");
		rejectCredibility.addAttribute("LoanDetails", "[{anytime, [{received, 100%}]}]");
		rejectCredibility.addAttribute("BKRAssessment", "[{anytime, [{accepted, 100%}]}]");
		rejectCredibility.addAttribute("EVAAssessment", "[{anytime, [{accepted, 100%}]}]");
		rejectCredibility.addAttribute("CredibilityAssessment", "[{firsttime, [{undecided, 10%},{rejected, 90%}]},{secondtime, [{rejected, 100%}]}]");
		rejectCredibility.addAttribute("CheckedCredibilityAssessment", "[{anytime, [{rejected, 100%}]}]");
		rejectCredibility.addAttribute("RejectionLetter", "[{anytime, [{sent, 100%}]}]");
		rejectCredibility.addAttribute("PickedUp", "[{anytime, [{pickedup, 100%}]}]");
		rejectCredibility.addAttribute("LoanOffer", "[{anytime, [{created, 100%}]}]");
		rejectCredibility.addAttribute("AcceptedOffer", "[{anytime, [{received, 100%}]}]");
		rejectCredibility.addAttribute("LoanActivation", "[{anytime, [{completed, 100%}]}]");
		rejectCredibility.addAttribute("InitialPayment", "[{anytime, [{completed, 100%}]}]");
		rejectCredibility.addAttribute("_Waited", "[{firsttime, N(1,0)}, {secondtime, N(2,0)}]");
		rejectCredibility.addAttribute("_Responded", "[{anytime, [{responded, 100%}]}]");
		rejectCredibility.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform credit check", "perform BKR check")));
		rejectCredibility.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform EVA check")));
		rejectCredibility.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("confirm credibility check")));
		rejectCredibility.addRepeatElephantTrail(10, "perform credit check", "confirm credibility check");
		rejectCredibility.addRepeatElephantTrail(10, "perform BKR check", "confirm credibility check");
		rejectCredibility.addRepeatElephantTrail(10, "confirm credibility check", "send rejection letter");
		addCaseType(rejectCredibility);

		CaseType rejectConfirm = new CaseType((long) (0*TimeUtils.HOUR), (long) (16*TimeUtils.HOUR), "[end]", 3*TimeUtils.HOUR, new String[] {"Start","after one hour 1", "after one hour 2", "receive accepted offer", "End"});
		rejectConfirm.setName("Reject Confirm");
		rejectConfirm.addAttribute("LoanDetails", "[{anytime, [{received, 100%}]}]");
		rejectConfirm.addAttribute("BKRAssessment", "[{anytime, [{accepted, 100%}]}]");
		rejectConfirm.addAttribute("EVAAssessment", "[{anytime, [{accepted, 100%}]}]");
		rejectConfirm.addAttribute("CredibilityAssessment", "[{firsttime, [{undecided, 10%},{accepted, 90%}]},{secondtime, [{accepted, 100%}]}]");
		rejectConfirm.addAttribute("CheckedCredibilityAssessment", "[{anytime, [{rejected, 100%}]}]");
		rejectConfirm.addAttribute("RejectionLetter", "[{anytime, [{sent, 100%}]}]");
		rejectConfirm.addAttribute("PickedUp", "[{anytime, [{pickedup, 100%}]}]");
		rejectConfirm.addAttribute("LoanOffer", "[{anytime, [{created, 100%}]}]");
		rejectConfirm.addAttribute("AcceptedOffer", "[{anytime, [{received, 100%}]}]");
		rejectConfirm.addAttribute("LoanActivation", "[{anytime, [{completed, 100%}]}]");
		rejectConfirm.addAttribute("InitialPayment", "[{anytime, [{completed, 100%}]}]");
		rejectConfirm.addAttribute("_Waited", "[{firsttime, N(1,0)}, {secondtime, N(2,0)}]");
		rejectConfirm.addAttribute("_Responded", "[{anytime, [{responded, 100%}]}]");
		rejectConfirm.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform credit check", "perform BKR check")));
		rejectConfirm.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform EVA check")));
		rejectConfirm.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("confirm credibility check")));
		rejectConfirm.addRepeatElephantTrail(10, "perform credit check", "confirm credibility check");
		rejectConfirm.addRepeatElephantTrail(10, "perform BKR check", "confirm credibility check");
		rejectConfirm.addRepeatElephantTrail(10, "confirm credibility check", "send rejection letter");
		addCaseType(rejectConfirm);

		CaseType unresponsive = new CaseType((long) (2*TimeUtils.HOUR), (long) (6*TimeUtils.HOUR), "[end]", 6*TimeUtils.HOUR, new String[] {"Start","after one hour 1", "after one hour 2", "receive accepted offer", "End"});
		unresponsive.setName("Unresponsive");
		unresponsive.addAttribute("LoanDetails", "[{anytime, [{received, 100%}]}]");
		unresponsive.addAttribute("BKRAssessment", "[{anytime, [{accepted, 100%}]}]");
		unresponsive.addAttribute("EVAAssessment", "[{anytime, [{accepted, 100%}]}]");
		unresponsive.addAttribute("CredibilityAssessment", "[{firsttime, [{undecided, 10%},{accepted, 90%}]},{secondtime, [{accepted, 100%}]}]");
		unresponsive.addAttribute("CheckedCredibilityAssessment", "[{anytime, [{accepted, 100%}]}]");
		unresponsive.addAttribute("RejectionLetter", "[{anytime, [{sent, 100%}]}]");
		unresponsive.addAttribute("PickedUp", "[{firsttime, [{notpickedup, 100%}]},{secondtime, [{pickedup, 100%}]}]");
		unresponsive.addAttribute("LoanOffer", "[{anytime, [{created, 100%}]}]");
		unresponsive.addAttribute("AcceptedOffer", "[{anytime, [{received, 100%}]}]");
		unresponsive.addAttribute("LoanActivation", "[{anytime, [{completed, 100%}]}]");
		unresponsive.addAttribute("InitialPayment", "[{anytime, [{completed, 100%}]}]");
		unresponsive.addAttribute("_Waited", "[{firsttime, N(1,0)}, {secondtime, N(2,0)}]");
		unresponsive.addAttribute("_Responded", "[{anytime, [{responded, 100%}]}]");
		unresponsive.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform credit check", "perform BKR check")));
		unresponsive.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform EVA check")));
		unresponsive.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("create loan offer", "send loan offer")));
		unresponsive.addRedoElephantTrail(10, new HashSet<String>(Arrays.asList("activate loan application")));
		unresponsive.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("confirm credibility check")));
		unresponsive.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("execute initial payment")));
		unresponsive.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("send rejection letter")));
		unresponsive.addRepeatElephantTrail(10, "perform credit check", "confirm credibility check");
		unresponsive.addRepeatElephantTrail(10, "perform BKR check", "confirm credibility check");
		unresponsive.addRepeatElephantTrail(10, "create loan offer", "send welcome letter");
		unresponsive.addRepeatElephantTrail(10, "activate loan application", "return documents to client");
		addCaseType(unresponsive);

		CaseType reminder = new CaseType((long) (1*TimeUtils.HOUR), (long) (4*TimeUtils.HOUR), "[end]", 6*TimeUtils.HOUR, new String[] {"Start","after one hour 1", "after one hour 2", "receive accepted offer", "End"});
		reminder.setName("Reminder");
		reminder.addAttribute("LoanDetails", "[{anytime, [{received, 100%}]}]");
		reminder.addAttribute("BKRAssessment", "[{anytime, [{accepted, 100%}]}]");
		reminder.addAttribute("EVAAssessment", "[{anytime, [{accepted, 100%}]}]");
		reminder.addAttribute("CredibilityAssessment", "[{firsttime, [{undecided, 10%},{accepted, 90%}]},{secondtime, [{accepted, 100%}]}]");
		reminder.addAttribute("CheckedCredibilityAssessment", "[{anytime, [{accepted, 100%}]}]");
		reminder.addAttribute("RejectionLetter", "[{anytime, [{sent, 100%}]}]");
		reminder.addAttribute("PickedUp", "[{anytime, [{pickedup, 100%}]}]");
		reminder.addAttribute("LoanOffer", "[{anytime, [{created, 100%}]}]");
		reminder.addAttribute("AcceptedOffer", "[{anytime, [{received, 100%}]}]");
		reminder.addAttribute("LoanActivation", "[{anytime, [{completed, 100%}]}]");
		reminder.addAttribute("InitialPayment", "[{anytime, [{completed, 100%}]}]");
		reminder.addAttribute("_Waited", "[{firsttime, N(1,0)}, {secondtime, N(2,0)}]");
		reminder.addAttribute("_Responded", "[{firsttime, [{notresponded, 100%}]},{secondtime, [{responded, 100%}]}]");
		reminder.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform credit check", "perform BKR check")));
		reminder.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("perform EVA check")));
		reminder.addSkipElephantTrail(10, new HashSet<String>(Arrays.asList("create loan offer", "send loan offer")));
		reminder.addRedoElephantTrail(10, new HashSet<String>(Arrays.asList("activate loan application")));
		reminder.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("confirm credibility check")));
		reminder.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("execute initial payment")));
		reminder.addRedoElephantTrail(20, new HashSet<String>(Arrays.asList("send rejection letter")));
		reminder.addRepeatElephantTrail(10, "perform credit check", "confirm credibility check");
		reminder.addRepeatElephantTrail(10, "perform BKR check", "confirm credibility check");
		reminder.addRepeatElephantTrail(10, "create loan offer", "send welcome letter");
		reminder.addRepeatElephantTrail(10, "activate loan application", "return documents to client");
		addCaseType(reminder);
	}

	@Override
	public long getPenaltyTime() {
		return TimeUtils.MINUTES_025 + RandomGenerator.generateUniform(TimeUtils.MINUTES_025);
	}

	@Override
	public boolean executeSkip() {
		return RandomGenerator.generateUniform(100) < 50; //50% chance
	}

	@Override
	public boolean performUnskilled() {
		return RandomGenerator.generateUniform(100) < 50; //50% chance;
	}

	@Override
	public int getPenaltySatisfaction() {
		return 1;
	}

}