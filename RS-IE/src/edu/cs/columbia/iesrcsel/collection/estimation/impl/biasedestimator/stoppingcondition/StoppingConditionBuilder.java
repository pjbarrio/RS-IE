package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.stoppingcondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.Builder;

public class StoppingConditionBuilder extends Builder {

	public static List<StoppingCondition> create(
			StoppingConditionEnum stoppingCondition, Configuration config,
			InformationExtractionSystem ie) {
		
		Integer[] maxIters = ToInteger(config.getString("maximum.iterations").split(SEPARATOR));
		
		List<StoppingCondition> ret = new ArrayList<StoppingCondition>();

		for (int i = 0; i < maxIters.length; i++) {
			int maxIter = maxIters[i];
			
			Map<String,String> params = new HashMap<String, String>();
			params.put("maximum.iterations", Integer.toString(maxIter));
			
			switch (stoppingCondition) {
			case FixedIterationsStoppingCondition:
				
				ret.add(new FixedIterationsStoppingCondition(params, maxIter));
				
				break;

			default:
				break;
			}
			
		}
		
		return ret;
	}

}
