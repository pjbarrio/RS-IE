package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.Builder;

public class QuerySamplerBuilder extends Builder {

	public static List<QuerySampler> create(
			QuerySamplerEnum querySampler, Configuration config,
			InformationExtractionSystem ie) {

		Double[] biasedWeights = ToDouble(config.getString("bias.weight").split(SEPARATOR));
		String[] trainingCollections = config.getString("training.collection").split(SEPARATOR);
		String[] prefixFolders = config.getString("prefix.folder").split(SEPARATOR);

		Boolean[] relationSpecific = ToBoolean(config.getString("random.relationSpecific").split(SEPARATOR));
		List<Integer> limitSizes = createList(ToInteger(config.getString("index.sampler.limit").split(SEPARATOR)));

		List<QuerySampler> ret = new ArrayList<QuerySampler>();

		for (int j = 0; j < trainingCollections.length; j++) {
			String trainingCollection = trainingCollections[j]; //"TREC";
			for (int k = 0; k < prefixFolders.length; k++) {
				String prefixFolder = prefixFolders[k];//"/proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/data/biasedestimator/";

					for (int i = 0; i < relationSpecific.length; i++) {
				Map<String,String> params = new HashMap<String, String>();
				params.put("training.collection", trainingCollection);
				params.put("prefix.folder", prefixFolder);

				switch (querySampler) {
				case PlainExternalCollectionQuerySampler:

					params.put("index.sampler.limit", "NA");
					params.put("random.relationSpecific", Boolean.toString(relationSpecific[i]));
					for (int b = 0; b < biasedWeights.length; b++) {
						double biasedWeight = biasedWeights[b]; //100
						params.put("bias.weight", Double.toString(biasedWeight));
						ret.add(new PlainExternalCollectionQuerySampler(params, prefixFolder , trainingCollection , biasedWeight,relationSpecific[i] ));
					}
					break;

				case IndexedexternalCollectionQuerySampler:
					
					params.put("random.relationSpecific", Boolean.toString(relationSpecific[i]));
					
					for (Integer limit : limitSizes) {
						params.put("index.sampler.limit", limit.toString());

						for (int b = 0; b < biasedWeights.length; b++) {
							double biasedWeight = biasedWeights[b]; //100
							params.put("bias.weight", Double.toString(biasedWeight));
							ret.add(new IndexedexternalCollectionQuerySampler(params, prefixFolder , trainingCollection , biasedWeight, limit,relationSpecific[i]));
						}

					}

					break;

				case RandomExternalCollectionQuerySampler:
					
					params.put("index.sampler.limit", "NA");
					params.put("bias.weight", "1");
					
					params.put("random.relationSpecific", Boolean.toString(relationSpecific[i]));
					
					ret.add(new RandomExternalCollectionQuerySampler(params, prefixFolder , trainingCollection, relationSpecific[i]));
					
					
					
					break;
				default:
					break;
				}

		
					}
	}

		}

		return ret;

	}

}
