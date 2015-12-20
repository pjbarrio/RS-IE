package edu.cs.columbia.iesrcsel.sample.generator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;

public class CyclicQuerySampleGeneration extends
		SampleGenerator {

	private int documentsPerQuery;
	private List<Query> initialQueries;
	private int sampleSize;
	private int MaxDocsPerQuery;

	public CyclicQuerySampleGeneration(
			int sampleSize, int numberOfQueries, int documentsPerQuery, int MaxDocsPerQuery, List<Query> initialQueries, Map<String, String> params) {
		super(params);
		this.sampleSize = sampleSize;
		assert sampleSize > 0 : "Target sample size should be > 0";
		this.documentsPerQuery = documentsPerQuery;
		this.MaxDocsPerQuery = MaxDocsPerQuery;
		assert documentsPerQuery > 0 : "Documents retrieved per query iteration should be > 0";
		this.initialQueries = initialQueries.subList(0, numberOfQueries);
	}

	@Override
	protected Sample _generateSample(TextCollection textCollection) {
		Sample sample = new Sample(textCollection, this);
		int initialIndex = 0;
		boolean atLeastOneQueryStillHasResults = true;
		boolean sampleIncomplete = true;
		do {
			atLeastOneQueryStillHasResults = false;
			for (Query q : initialQueries) {
				// TODO Why do we recalculate the result list each time?
				// Is it because we have too many queries to keep all result lists in memory?
				List<ScoredDocument> result = textCollection.search(q,MaxDocsPerQuery);
				final int resultLen = result.size();
				final int iterationLen = initialIndex + documentsPerQuery;
				atLeastOneQueryStillHasResults = atLeastOneQueryStillHasResults ? true : (resultLen > iterationLen);
				final int maxIndex = resultLen > iterationLen ? iterationLen : resultLen;
				for (int j = initialIndex; sampleIncomplete && (j < maxIndex); j++) {
					sample.addDocument(result.get(j));
					sampleIncomplete = sample.size() < sampleSize;					
				}
				sampleIncomplete = sampleIncomplete && (sample.size() < sampleSize);
				if (!sampleIncomplete) {
					break;
				}
			}
			initialIndex += documentsPerQuery;
		} while (sampleIncomplete && atLeastOneQueryStillHasResults);
		return sample;
	}

}
