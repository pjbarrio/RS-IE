package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.poolsizefractionestimator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.Builder;

public class PoolSizeFractionEstimatorBuilder extends Builder{

	public static List<PoolSizeFractionEstimator> create(
			PoolSizeFractionEstimatorEnum estimator, Configuration config,
			InformationExtractionSystem ie) {

		

		List<PoolSizeFractionEstimator> ret = new ArrayList<PoolSizeFractionEstimator>();

		switch (estimator) {
		case GeometricMeanPoolSizeFractionEstimator:

			List<Long> maxNumberOfQueriesforPoolSize = createList(ToLong(config.getString("max.number.of.queries.for.pool.size").split(SEPARATOR)));
			
			for (Long maxNumberOfQueries : maxNumberOfQueriesforPoolSize) {

				Map<String,String> params = new HashMap<String, String>();
				params.put("max.number.of.queries.for.pool.size", Long.toString(maxNumberOfQueries));

				ret.add(new GeometricMeanPoolSizeFractionEstimator(params,maxNumberOfQueries));

			}

			break;

		case OnlineUsefulnessBasedPoolSizeFractionEstimator:

			Map<String,String> params = new HashMap<String, String>();
			params.put("max.number.of.queries.for.pool.size", "NA");
			
			ret.add(new OnlineUsefulnessBasedPoolSizeFractionEstimator(params));

			break;

		case NAPoolSizeFractionEstimator:
			
			params = new HashMap<String, String>();
			
			params.put("max.number.of.queries.for.pool.size", "NA");
			
			ret.add(new NAPoolSizeFractionEstimator(params));
			
			break;
		default:
			break;
		}

		return ret;
		
	}

}
