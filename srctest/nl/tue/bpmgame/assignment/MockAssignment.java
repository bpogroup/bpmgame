package nl.tue.bpmgame.assignment;

import nl.tue.bpmgame.declarative.DeclarativeModel;

public class MockAssignment extends Assignment {

	@Override
	protected void createAssignment() {
		behavior = new DeclarativeModel();
		addResource(new Resource(Assignment.GENERAL_MANAGER, new String[] {}, true, 1));
	}

	@Override
	public long getPenaltyTime() {
		return 0;
	}

	@Override
	public boolean executeSkip() {
		return false;
	}

	@Override
	public boolean performUnskilled() {
		return false;
	}
	
	public void setBehavior(DeclarativeModel declarativeModel) {
		this.behavior = declarativeModel;
	}

	@Override
	public int getPenaltySatisfaction() {
		return 0;
	}
}
