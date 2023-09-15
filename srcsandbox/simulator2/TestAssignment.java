package simulator2;

import java.lang.reflect.InvocationTargetException;

import nl.tue.bpmgame.assignment.Assignment;

public class TestAssignment {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		Assignment.initializeAssignment("nl.tue.bpmgame.assignment.MyAssignment");
		System.out.println(Assignment.getAssignment().executeSkip());
	}

}
