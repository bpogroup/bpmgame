package nl.tue.bpmgame.assignment;

import nl.tue.bpmgame.declarative.DeclarativeModel;
import nl.tue.util.RandomGenerator;

/**
 * The specification of a concrete assignment: tasks, events, resources, ...
 * The assignment can be obtained through Assignment.getAssignment();
 * Assignment.getAssignment() also contains the invocation of this assignment.
 * This is where the active assignment can be chosen.
 *
 */
public class IntegrationTestAssignment extends Assignment{

	@Override
	protected void createAssignment() {
		addSkill("SkillA");
		addSkill("SkillB");
		addTask(new Task("TaskA", 1000000, 2600000, "SkillA", 0));
		addTask(new Task("TaskB", 1500000, 2100000, "SkillB", 0));
		addEvent(new Event("Start", 0, 0));
		addEvent(new Event("End", 0, 0));
		addResource(new Resource("ResourceA", new String[] {"SkillA"}, true, 1));
		addResource(new Resource("ResourceB", new String[] {"SkillB"}, true, 1));
		addResource(new Resource(Assignment.GENERAL_MANAGER, new String[] {"SkillA","SkillB"}, true, 1));
		addSkill(Assignment.SYSTEM_SKILL);
		addResource(new Resource(Assignment.INFORMATION_SYSTEM, new String[] {Assignment.SYSTEM_SKILL}, true, 0));
		
		DeclarativeModel behavior = new DeclarativeModel();
		behavior.addCondition("Start", "");
		behavior.addCondition("TaskA", "[Start]");
		behavior.addCondition("TaskB", "[TaskA]");
		behavior.addCondition("End", "[TaskB]");
		addBehavior(behavior);
		
		CaseType caseType = new CaseType(2000000, 3400000, "[End]", 7200000, new String[] {"Start","End"});
		addCaseType(caseType);
	}

	@Override
	public long getPenaltyTime() {
		return 300000 + RandomGenerator.generateUniform(900000); //Penalty time is 5-15 minutes
	}

	@Override
	public boolean executeSkip() {
		return RandomGenerator.generateUniform(100) <= 20; //There is an 20% chance
	}

	@Override
	public boolean performUnskilled() {
		return RandomGenerator.generateUniform(100) <= 40; //There is an 40% chance
	}

	@Override
	public int getPenaltySatisfaction() {
		return 1;
	}
}
