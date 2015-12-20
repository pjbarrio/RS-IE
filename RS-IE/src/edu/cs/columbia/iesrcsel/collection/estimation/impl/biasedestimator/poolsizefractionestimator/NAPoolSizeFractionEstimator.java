package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.poolsizefractionestimator;

import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class NAPoolSizeFractionEstimator extends PoolSizeFractionEstimator {

	public NAPoolSizeFractionEstimator(Map<String, String> params) {
		super(params);
	}

	@Override
	public double calculateFraction(List<String> queries,
			TextCollection collection, InformationExtractionSystem ie,
			int maxDocsPerQuery) {
		return 0;
	}

	@Override
	public void informQuery(boolean incident) {
		;
	}

}
