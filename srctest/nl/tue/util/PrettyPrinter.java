package nl.tue.util;

import java.util.List;

import nl.tue.bpmgame.simulator.LogEvent;

public class PrettyPrinter {

	public static String prettyLog(List<LogEvent> log) {
		String result = "";
		for (LogEvent le: log) {
			result += le.getCaseID() + "\t" + le.getNodeLabel() + "\t" + le.getStartTime() + "\t" + le.getCompletionTime() + "\t" + le.getResourceName() + "\t";
			for (String attribute: le.getAttributes()) {
				result += attribute + "=" + le.getAttributeValue(attribute) + "\t";
			}
			result += "\n";
		}
		return result;
	}
}
