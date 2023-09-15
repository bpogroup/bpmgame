package util;

import nl.tue.bpmn.parser.DistributionEvaluator;

public class TryDistribution {

	public static void main(String[] args) {
		DistributionEvaluator.getInstance().evaluate("[{firsttime, N(1,0)}, {secondtime, N(2,0)}]");
		System.out.println(DistributionEvaluator.getInstance().getValueFirstTime());
		System.out.println(DistributionEvaluator.getInstance().getValueSecondTime());		
	}

}
