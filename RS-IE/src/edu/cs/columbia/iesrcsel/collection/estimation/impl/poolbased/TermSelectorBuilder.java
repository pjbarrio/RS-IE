package edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.Builder;

public class TermSelectorBuilder extends Builder {

	public static List<TermSelector> create(TermSelectorEnum termSelector, Configuration config, InformationExtractionSystem ie) {

		Float[] pCriticalBiasUseful = ToFloat(config.getString("pCriticalBiasUseful").split(SEPARATOR));

		Integer[] minCollectionDF = ToInteger(config.getString("minCollectionDF").split(SEPARATOR));

		Integer[] maxDocsPerQuery = ToInteger(config.getString("max.docs.per.query").split(SEPARATOR));
		
		List<TermSelector> ret = new ArrayList<TermSelector>();

		Map<String, String> params = new HashMap<String,String>();
		
		for (Float pCriticalBiasUsef : pCriticalBiasUseful) {
			
			params.put("pCriticalBiasUsef", Float.toString(pCriticalBiasUsef));
			
			for (Integer minCollection : minCollectionDF) {
				
				params.put("minCollectionDF", Integer.toString(minCollection));
				
				for (int i = 0; i < maxDocsPerQuery.length; i++) {
					
					params.put("max.docs.per.query", Integer.toString(minCollection));
					
					switch (termSelector) {
					case TermSelectorUseful:

						params.put("pCriticalBiasSample", "NA");
						
						ret.add(new TermSelectorUseful(ie, pCriticalBiasUsef, minCollection, maxDocsPerQuery[i], params));

						break;

					case TermSelectorBiasedUseful:

						Float[] pCriticalBiasSample = ToFloat(config.getString("pCriticalBiasSample").split(SEPARATOR));

						for (Float pCriticalBiasSampl : pCriticalBiasSample) {

							params.put("pCriticalBiasSample", Float.toString(pCriticalBiasSampl));

							ret.add(new TermSelectorBiasedUseful(ie, pCriticalBiasSampl, pCriticalBiasUsef, minCollection, maxDocsPerQuery[i], params));

						}

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
