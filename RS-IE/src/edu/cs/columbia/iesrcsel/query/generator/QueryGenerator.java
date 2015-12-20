package edu.cs.columbia.iesrcsel.query.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;

public abstract class QueryGenerator implements Parametrizable{

	private Map<Sample,List<Query>> queriesMap = new HashMap<Sample,List<Query>>();	
	
	private Map<String, String> params;
	
	public QueryGenerator(Map<String,String> params){
		this.params = new HashMap<String, String>();
		this.params.putAll(params);
		this.params.put("query.generator", this.getClass().getSimpleName());
	}
	
	public List<Query> generateQueries(Sample sample){
		
		List<Query> queries = queriesMap.get(sample);
		
		if (queries == null){
			queries = _generateQueries(sample);
			queriesMap.put(sample,queries);
		}
		
		return queries;
		
	}

	protected abstract List<Query> _generateQueries(Sample sample);
	
	@Override
	public Map<String, String> getParams() {
		return params;
	}
	
}
