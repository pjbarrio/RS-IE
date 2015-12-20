package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.usefulnessCalculator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySampler;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;

public class GeometricMeanQueryUsefulnessCalculator extends QueryUsefulnessCalculator {

	private MersenneTwister rg;

	public GeometricMeanQueryUsefulnessCalculator(Map<String,String> params){
		super(params);
		rg = new MersenneTwister();
	}

	@Override
	public double _calculateQueryUsefulness(QuerySampler qs, Query query,
			List<ScoredDocument> docs, InformationExtractionSystem ie) {

		Set<Integer> chosen = new HashSet<Integer>();

		int val;

		do{

			val = rg.nextInt(docs.size());

			chosen.add(val);

		}while(!ie.isUseful(docs.get(val))); //Guaranteed because we get in here knowing that docs include.

		double usefuls = (double)docs.size() / (double)chosen.size();

		return usefuls;
		
//		return Math.round(w * usefuls + ((double)docs.size() - usefuls));
	}

	@Override
	public void _reset() {
		;
	}

}
