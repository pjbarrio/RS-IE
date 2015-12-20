package edu.cs.columbia.iesrcsel.score.estimation.method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimatorBuilder;
import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimatorEnum;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.ExactCollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.score.estimation.method.impl.ReDDEOriginalScoreCalculatorWithOverlapCorrection;
import edu.cs.columbia.iesrcsel.utils.Builder;

public class ScoreCalculatorBuilder extends Builder{

	public static List<ScoreCalculator> create(
			ScoreCalculatorEnum value, Configuration config,
			InformationExtractionSystem ie) {
		
		List<ScoreCalculator> ret = new ArrayList<ScoreCalculator>();
		
		switch (value) {
		case ReDDEOriginalScoreCalculatorWithOverlapCorrection:
			
			String[] collectionSizeEstimators = config.getString("collection.size.estimator").split(SEPARATOR);
			
			List<CollectionSizeEstimator> colSizeEstimators = new ArrayList<CollectionSizeEstimator>();
			
			for (int i = 0; i < collectionSizeEstimators.length; i++) {
				colSizeEstimators.addAll(CollectionSizeEstimatorBuilder.create(CollectionSizeEstimatorEnum.valueOf(collectionSizeEstimators[i]), config));
			}
			
			Double[] relRation = ToDouble(config.getString("relevance.ratio").split(SEPARATOR));

			for (CollectionSizeEstimator cl : colSizeEstimators) {
				
				for (Double relratio : relRation) {
				
					double relevanceRatio = relratio;

					Map<String,String> params = new HashMap<String,String>();
					
					params.put("relevance.ratio", Double.toString(relevanceRatio));
					params.putAll(cl.getParams());
					
					ret.add(new ReDDEOriginalScoreCalculatorWithOverlapCorrection(params, relevanceRatio, cl));
				
				}

			}
			
			break;

		default:
			break;
		}
		
		return ret;	
		
	}

}
