package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.ideestimator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;

import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySampler;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class PredictedInverseDocumentDegreeEstimator extends
		InverseDocumentDegreeEstimator {

	private Set<String> queriespool;


	public PredictedInverseDocumentDegreeEstimator(Map<String,String> params) {
		super(params);
		this.queriespool = new HashSet<String>();
	}
	
	@Override
	public double _estimateInverseDocumentDegree(Document doc) {
		
		List<String> queries = new ArrayList<String>(doc.getTerms());
	
		queries.retainAll(queriespool);
	
		return 1.0 / (double)queries.size(); 	
	
	}


	@Override
	public void _reset() {
		if (queriespool != null)
			queriespool.clear();
	}

	@Override
	public void initalize(InformationExtractionSystem ie, QuerySampler qs) {
		queriespool = new HashSet<String>(qs.getQueries());
	}
	
}
