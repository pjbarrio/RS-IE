package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.poolsizefractionestimator;

import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class OnlineUsefulnessBasedPoolSizeFractionEstimator extends
		PoolSizeFractionEstimator {

	private double inc;
	private double ninc;
	
	public OnlineUsefulnessBasedPoolSizeFractionEstimator(
			Map<String, String> params) {
		super(params);
		reset();
	}

	@Override
	public double calculateFraction(List<String> queries,
			TextCollection collection, InformationExtractionSystem ie, int maxDocsPerQuery) {
		return fraction;
	}

	@Override
	public void reset() {
		super.reset();
		inc=ninc=0;
	}

	@Override
	public void informQuery(boolean incident) {
		if (incident)
			inc++;
		else
			ninc++;
		
		fraction = inc / (inc+ninc);
		
	}

}
