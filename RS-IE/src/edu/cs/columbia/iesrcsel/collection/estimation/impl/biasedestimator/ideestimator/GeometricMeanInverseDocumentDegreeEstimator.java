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

public class GeometricMeanInverseDocumentDegreeEstimator extends
		InverseDocumentDegreeEstimator {

	private MersenneTwister rg;
	private long maxNumberOfDocuments;
	private Set<String> queriespool;


	public GeometricMeanInverseDocumentDegreeEstimator(Map<String,String> params,long maxNumberOfDocuments) {
		super(params);
		this.rg = new MersenneTwister();
		this.maxNumberOfDocuments = maxNumberOfDocuments;
		this.queriespool = new HashSet<String>();
	}
	
	@Override
	public double _estimateInverseDocumentDegree(Document doc) {
		
		List<String> queries = new ArrayList<String>(doc.getTerms());
		
		queries.retainAll(queriespool);
		
		int i = 1;
		
		while (true){
			
			String query = queries.get(rg.nextInt(queries.size()));
			
			List<ScoredDocument> docs = doc.getCollection().search(new Query(query), maxNumberOfDocuments);
			
			if (docs.contains(doc)){
				return (double)i / (double)queries.size();
			}
			
			i += 1;
			
		}
		
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
