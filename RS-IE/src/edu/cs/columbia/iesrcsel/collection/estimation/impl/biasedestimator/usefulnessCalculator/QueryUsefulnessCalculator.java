package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.usefulnessCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySampler;
import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;

public abstract class QueryUsefulnessCalculator implements Parametrizable{

	private Map<String, String> params;
	private Map<Query,Double> cache;

	public QueryUsefulnessCalculator(Map<String, String> params){
		this.params = new HashMap<String, String>();
		this.params.putAll(params);
		this.params.put("query.usefulness.calculator", this.getClass().getSimpleName());
		reset();
	}
	
	public double calculateQueryUsefulness(QuerySampler qs, Query query,
			List<ScoredDocument> docs, InformationExtractionSystem ie){
		
		Double ret = cache.get(query);
		
		if (ret == null){
			ret = _calculateQueryUsefulness(qs, query, docs, ie);
			cache.put(query, ret);
		}
		
		return ret;
		
	}

	public void reset(){
		
		if (cache == null)
			cache = new HashMap<Query,Double>();
		
		cache.clear();
		_reset();
	}
	
	public abstract double _calculateQueryUsefulness(QuerySampler qs, Query query,
			List<ScoredDocument> docs, InformationExtractionSystem ie);

	public abstract void _reset();

	public Map<String,String> getParams(){
		return params;
	}
	
}
