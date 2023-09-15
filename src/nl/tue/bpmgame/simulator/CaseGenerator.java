package nl.tue.bpmgame.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.assignment.CaseType;
import nl.tue.bpmgame.executionmodel.Case;

/**
 * Generate Case objects according to CaseType objects. These cases are the same for all simulator 
 * models to ensure fairness of evaluation between models. The cases are generated between a
 * specified start and end time. They can be requested in order of their arrival.
 */
public class CaseGenerator {

	List<Case> cases;
	
	public CaseGenerator() {
		cases = new ArrayList<Case>();
	}
	
	/**
	 * Randomly generates cases that start between the given startTime and endTime
	 * 
	 * @param startTime a startTime in Java time (milliseconds since 1970)
	 * @param endTime an endTime in Java time (milliseconds since 1970)
	 */
	public void generateCases(long startTime, long endTime) {
		//Generate a case for each case type
		Map<CaseType,Case> generatedCases = new HashMap<CaseType,Case>();
		for (CaseType caseType: Assignment.getAssignment().getCaseTypes()) {
			generatedCases.put(caseType, caseType.generateCase(startTime));
		}
		//Repeat as long as there are generated cases
		//(I.e. until there are cases between the specified startTime and endTime, 
		// because cases will be removed when their start time exceeds the end time.)
		while (!generatedCases.isEmpty()) {
			//Find the case that starts earliest
			long earliestStartTime = Long.MAX_VALUE;					
			Map.Entry<CaseType, Case> earliestCase = null;
			for (Map.Entry<CaseType, Case> c: generatedCases.entrySet()) {
				if (c.getValue().getStartTime() < earliestStartTime) {
					earliestStartTime = c.getValue().getStartTime();
					earliestCase = c;
				}
			}
			//If the case happens after the end time, end the loop
			//(because there will be no more cases before the end time)
			if (earliestStartTime > endTime) {
				return;
			}
			//Add the case to the list, remove it from the generated cases
			cases.add(earliestCase.getValue());
			generatedCases.remove(earliestCase.getKey());
			//Generate a new case in its place, but only if it starts before the end time
			Case generatedCase = earliestCase.getKey().generateCase(earliestCase.getValue().getStartTime());
			if (generatedCase.getStartTime() <= endTime) {
				generatedCases.put(earliestCase.getKey(), generatedCase);
			}
		}
	}
	
	public List<Case> getCases(){
		return cases;
	}
}
