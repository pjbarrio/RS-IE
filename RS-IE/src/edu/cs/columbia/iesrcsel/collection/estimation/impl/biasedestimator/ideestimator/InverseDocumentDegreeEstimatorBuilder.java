package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.ideestimator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.Builder;

public class InverseDocumentDegreeEstimatorBuilder extends Builder{

	public static List<InverseDocumentDegreeEstimator> create(
			InverseDocumentDegreeEstimatorEnum inverseDocumentDegreeEstimator, Configuration config,
			InformationExtractionSystem ie, long maxNumberOfDocuments) {
		
		List<InverseDocumentDegreeEstimator> ret = new ArrayList<InverseDocumentDegreeEstimator>();
		
		Map<String,String> params = new HashMap<String, String>();
		
		switch (inverseDocumentDegreeEstimator) {
		case GeometricMeanInverseDocumentDegreeEstimator:
			
			ret.add(new GeometricMeanInverseDocumentDegreeEstimator(params,maxNumberOfDocuments));
			
			break;

		case PredictedInverseDocumentDegreeEstimator:
			
			
			ret.add(new PredictedInverseDocumentDegreeEstimator(params));

			break;

		default:
			break;
		}
		
		return ret;
	}

}
