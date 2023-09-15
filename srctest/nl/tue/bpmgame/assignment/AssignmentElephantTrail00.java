package nl.tue.bpmgame.assignment;

import java.util.Arrays;
import java.util.HashSet;

import nl.tue.bpmgame.declarative.DeclarativeModel;

/**
 * The specification of a concrete assignment: tasks, events, resources, ...
 * The assignment can be obtained through Assignment.getAssignment();
 * Assignment.getAssignment() also contains the invocation of this assignment.
 * This is where the active assignment can be chosen.
 *
 */
public class AssignmentElephantTrail00 extends Assignment{

	@Override
	protected void createAssignment() {
		addSkill("SkillA");
		addSkill("SkillB");
		addTask(new Task("TaskA", 60000, 60000, "SkillA", 0));
		addTask(new Task("TaskB", 60000, 60000, "SkillB", 0));
		addEvent(new Event("Start", 0, 0));
		addEvent(new Event("End", 0, 0));
		addResource(new Resource("ResourceA", new String[] {"SkillA"}, true, 1));
		addResource(new Resource("ResourceB", new String[] {"SkillB"}, true, 1));
		addResource(new Resource(Assignment.GENERAL_MANAGER, new String[] {"SkillA","SkillB"}, true, 1));

		DeclarativeModel behavior = new DeclarativeModel();
		behavior.addCondition("Start", "");
		behavior.addCondition("TaskA", "[Start]");
		behavior.addCondition("TaskB", "[TaskA]");
		behavior.addCondition("End", "[TaskB]");
		addBehavior(behavior);
		
		CaseType caseType = new CaseType(30000, 30000, "[End]", 1000000, new String[] {"Start","End"});
		caseType.addSkipElephantTrail(100, new HashSet<String>(Arrays.asList("TaskB")));
		addCaseType(caseType);
	}

	@Override
	public long getPenaltyTime() {
		return 30000;
	}

	@Override
	public boolean executeSkip() {
		return false;
	}

	@Override
	public boolean performUnskilled() {
		return false;
	}

	@Override
	public int getPenaltySatisfaction() {
		return 1;
	}
}
