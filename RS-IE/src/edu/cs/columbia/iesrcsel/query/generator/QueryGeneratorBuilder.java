package edu.cs.columbia.iesrcsel.query.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.query.generator.impl.CachedQueryGenerator;
import edu.cs.columbia.iesrcsel.utils.Builder;

public class QueryGeneratorBuilder extends Builder{

	private static Boolean[] tasw;
	private static List<Integer> splits;

	public static Collection<? extends QueryGenerator> create(
			QueryGeneratorEnum generator, Configuration config,
			InformationExtractionSystem ie) {
		
		List<QueryGenerator> ret = new ArrayList<QueryGenerator>();
		
		switch (generator) {
		case CachedQueryGenerator:
			
			List<Integer> splits = getSplits(config);
						
			Boolean[] tasws = getTuplesAsStopWords(config);
						
			Integer[] numQueries = ToInteger(config.getString("relation.specific.queries.limit").split(SEPARATOR));
			
			for (Integer split : splits) {
				for (Boolean tasw : tasws) {
					for (Integer numberOfQueries : numQueries) {
						Map<String,String> params = new HashMap<String,String>();
						params.put("ie.initialQueries.tupleAsStopWord", tasw.toString());
						params.put("ie.initialQueries.split", split.toString());
						params.put("relation.specific.queries.limit", numberOfQueries.toString());
						
						ret.add(new CachedQueryGenerator(params, "data/queries/" + ie.getRelation() + "." + ie.getExtractor() + "." + split + "." + tasw + ".ser",numberOfQueries));
					}
				}
			}

		default:
			break;
		}
		
		return ret;		
		
	}

	private static Boolean[] getTuplesAsStopWords(Configuration config) {
		
		if (tasw == null){
			tasw = ToBoolean(config.getString("ie.initialQueries.tupleAsStopWord").split(SEPARATOR)); //ie.initialQueries.tupleAsStopWord=true,false
		}
		
		return tasw;
	}

	private static List<Integer> getSplits(Configuration config) {

		if (splits == null){
			splits = createList(ToInteger(config.getString("ie.initialQueries.split").split(SEPARATOR))); //ie.initialQueries.split=1,5,1 
		}
		
		return splits;
		
		
		
	}
	
	
}
