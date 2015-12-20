package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.usefulnessCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.Builder;

public class QueryUsefulnessCalculatorBuilder extends Builder{

	public static List<QueryUsefulnessCalculator> create(
			QueryUsefulnessCalculatorEnum queryUsefulnessCalculator, Configuration config,
			InformationExtractionSystem ie) {
		
		List<QueryUsefulnessCalculator> ret = new ArrayList<QueryUsefulnessCalculator>();
		
		Map<String,String> params = new HashMap<String, String>();
		
		switch (queryUsefulnessCalculator) {
		case PlainQueryUsefulnessCalculator:
			
			ret.add(new PlainQueryUsefulnessCalculator(params));
			
			break;

		case GeometricMeanQueryUsefulnessCalculator:
			
			ret.add(new GeometricMeanQueryUsefulnessCalculator(params));
			
			break;
			
		case NAQueryUsefulnessCalculator:
			
			ret.add(new NAQueryUsefulnessCalculator(params));
			
			break;
			
		default:
			break;
		}
		
		return ret;
	}

}
