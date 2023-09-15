package nl.tue.bpmgame.assignment;

import nl.tue.bpmgame.declarative.DeclarativeModel;

/**
 * The specification of a concrete assignment: tasks, events, resources, ...
 * The assignment can be obtained through Assignment.getAssignment();
 * Assignment.getAssignment() also contains the invocation of this assignment.
 * This is where the active assignment can be chosen.
 *
 */
public class Assignment00 extends Assignment{

	@Override
	protected void createAssignment() {
		addSkill("SkillA");
		addTask(new Task("TaskA", 60000, 60000, "SkillA", 0));
		addEvent(new Event("Start", 0, 0));
		addEvent(new Event("End", 0, 0));
		addResource(new Resource("ResourceA", new String[] {"SkillA"}, true, 1));
		addResource(new Resource(Assignment.GENERAL_MANAGER, new String[] {"SkillA"}, true, 1));

		DeclarativeModel behavior = new DeclarativeModel();
		behavior.addCondition("Start", "");
		behavior.addCondition("TaskA", "[Start]");
		behavior.addCondition("End", "[TaskA]");
		addBehavior(behavior);
		
		CaseType caseType = new CaseType(30000, 30000, "[End]", 1000000, new String[] {"Start","End"});
		addCaseType(caseType);
	}

	@Override
	public long getPenaltyTime() {
		//return 30000 + RandomGenerator.generateUniform(600000); //Penalty time is 30-90 seconds
		return 30000;
	}

	@Override
	public boolean executeSkip() {
		//return RandomGenerator.generateUniform(100) <= 80; //There is an 80% chance
		return false;
	}

	@Override
	public boolean performUnskilled() {
		//return RandomGenerator.generateUniform(100) <= 80; //There is an 80% chance
		return false;
	}

	@Override
	public int getPenaltySatisfaction() {
		//return 1;
		return 1;
	}
}
