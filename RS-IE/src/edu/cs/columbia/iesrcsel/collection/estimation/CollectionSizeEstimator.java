package edu.cs.columbia.iesrcsel.collection.estimation;

import java.util.HashMap;
import java.util.Map;

import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public abstract class CollectionSizeEstimator implements Parametrizable{

	private Map<String, String> params;
	
	public CollectionSizeEstimator(Map<String,String> params){
		this.params = new HashMap<String, String>();
		this.params.putAll(params);
		this.params.put("collection.size.estimator", this.getClass().getSimpleName());

	}
	
	public abstract long getCollectionSizeEstimate(TextCollection collection);

	
	@Override
	public Map<String, String> getParams() {
		return params;
	}
}
