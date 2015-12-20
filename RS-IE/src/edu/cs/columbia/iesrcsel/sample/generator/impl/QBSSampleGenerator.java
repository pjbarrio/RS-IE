package edu.cs.columbia.iesrcsel.sample.generator.impl;

import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.utils.QuerySelectionStrategy;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

import java.util.List;
import java.util.Map;

public class QBSSampleGenerator extends SampleGenerator{

	private List<Query> initialQueries;
	private int sampleSize;
	private int maxNumberOfQueries;
	private int documentsPerQuery;
	private QuerySelectionStrategy querySelectionStrategy;

	public QBSSampleGenerator(List<Query> initialQueries, int sampleSize, int maxNumberOfQueries, int documentsPerQuery, QuerySelectionStrategy querySelectionStrategy, Map<String, String> params){
		super(params);
		this.initialQueries = initialQueries;
		this.sampleSize = sampleSize;
		this.maxNumberOfQueries = maxNumberOfQueries;
		this.documentsPerQuery = documentsPerQuery;
		this.querySelectionStrategy = querySelectionStrategy;
	}

	public QBSSampleGenerator(String serializedQueriesFile, int sampleSize, int maxNumberOfQueries, int documentsPerQuery, QuerySelectionStrategy querySelectionStrategy, Map<String, String> params){
		this((List<Query>)SerializationHelper.deserialize(serializedQueriesFile),sampleSize,maxNumberOfQueries,documentsPerQuery,querySelectionStrategy, params);
	}
	
	@Override
	protected Sample _generateSample(TextCollection textCollection) {
		
		Sample sample = new Sample(textCollection, this);
		
		querySelectionStrategy.initialize(initialQueries);
		
		int issuedQueries = 0;
		
		while (sample.size() < sampleSize && querySelectionStrategy.hasMoreQueries() && issuedQueries < maxNumberOfQueries){
			
			List<ScoredDocument> docs = textCollection.search(querySelectionStrategy.getNextQuery(), documentsPerQuery);
				
			for (ScoredDocument document : docs) {
				if (sample.size() >= sampleSize)
					break;
				querySelectionStrategy.update(document);
				sample.addDocument(document);
			}
			
			issuedQueries++;
		}
		
		return sample;
		
	}
	
}
