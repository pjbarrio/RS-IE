package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.usefulnessCalculator;

import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySampler;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;

public class PlainQueryUsefulnessCalculator extends QueryUsefulnessCalculator {

	public PlainQueryUsefulnessCalculator(Map<String,String> params){
		super(params);
	}
	
	@Override
	public double _calculateQueryUsefulness(QuerySampler qs, Query query,
			List<ScoredDocument> docs, InformationExtractionSystem ie) {
		
		double sum = 0.0;
		
		for (int i = 0; i < docs.size(); i++) {
			
			if (ie.isUseful(docs.get(i)))
				sum += 1.0;
			
		}
		
		return sum;
	}

	@Override
	public void _reset() {
		;
	}

}
