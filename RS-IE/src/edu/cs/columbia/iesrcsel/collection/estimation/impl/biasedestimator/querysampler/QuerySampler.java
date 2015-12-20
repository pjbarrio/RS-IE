package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public abstract class QuerySampler implements Parametrizable{

	private Map<String, String> params;

	public QuerySampler(Map<String, String> params){
		this.params = new HashMap<String, String>();
		this.params.putAll(params);
		this.params.put("query.sampler", this.getClass().getSimpleName());
	}
	
	public abstract void initialize(TextCollection collection, InformationExtractionSystem ie);

	public abstract Query sampleQuery();

	public abstract double normalizationValue();

	public abstract double getBiasWeight();

	public abstract double getNotNormalizedProbability(Query query);

	public abstract void reset();

	public Map<String,String> getParams(){
		return params;
	}

	public abstract List<String> getQueries();
	
}
