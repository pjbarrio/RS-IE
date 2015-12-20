package edu.cs.columbia.iesrcsel.collection.estimation.impl;

import java.util.Map;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class ExactCollectionSizeEstimator extends CollectionSizeEstimator {

	public ExactCollectionSizeEstimator(Map<String,String> params) {
		super(params);
	}
	
	@Override
	public long getCollectionSizeEstimate(TextCollection collection) {
		return collection.size();
	}


}
