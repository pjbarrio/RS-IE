package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.poolsizefractionestimator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public abstract class PoolSizeFractionEstimator  implements Parametrizable{

	private Map<String, String> params;
	protected double fraction;

	public PoolSizeFractionEstimator(Map<String, String> params) {
		this.params = new HashMap<String, String>();
		this.params.putAll(params);
		this.params.put("pool.size.fraction.estimator", this.getClass().getSimpleName());
		fraction = Double.NaN;
	}

	@Override
	public Map<String, String> getParams() {
		return params;
	}

	public abstract double calculateFraction(List<String> queries,
			TextCollection collection, InformationExtractionSystem ie, int maxDocsPerQuery);

	public void reset(){
		fraction = Double.NaN;
	}

	public  abstract void informQuery(boolean incident);

}
