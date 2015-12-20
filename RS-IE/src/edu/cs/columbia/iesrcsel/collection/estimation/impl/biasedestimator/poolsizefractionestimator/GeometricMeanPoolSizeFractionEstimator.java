package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.poolsizefractionestimator;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class GeometricMeanPoolSizeFractionEstimator extends
		PoolSizeFractionEstimator {

	private long maxNumberOfQueries;
	private MersenneTwister rg;

	public GeometricMeanPoolSizeFractionEstimator(Map<String, String> params,
			long maxNumberOfQueries) {
		super(params);
		this.rg = new MersenneTwister();
		this.maxNumberOfQueries = maxNumberOfQueries;
	}

	@Override
	public double calculateFraction(List<String> queries,
			TextCollection collection, InformationExtractionSystem ie, int maxDocsPerQuery) {
		
		if (Double.isNaN(fraction)){
			
			int total = 0;
			
			for (int i = 0; i < maxNumberOfQueries; i++) {
			
				String query = queries.get(rg.nextInt(queries.size()));
				
				if (!collection.search(new Query(query),maxDocsPerQuery).isEmpty())
					total +=1;
			}
			
			fraction = (double)total / (double)maxNumberOfQueries; 
			
		}
		
		return fraction;
		
	}

	@Override
	public void informQuery(boolean incident) {
		;
	}


}
