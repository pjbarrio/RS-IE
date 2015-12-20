package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.usefulnessCalculator;

import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySampler;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;

public class NAQueryUsefulnessCalculator extends QueryUsefulnessCalculator {

	public NAQueryUsefulnessCalculator(Map<String, String> params) {
		super(params);
	}

	@Override
	public double _calculateQueryUsefulness(QuerySampler qs, Query query,
			List<ScoredDocument> docs, InformationExtractionSystem ie) {
		return 0;
	}

	@Override
	public void _reset() {
		;
	}

}
