package edu.cs.columbia.iesrcsel.sample.generator.impl;

import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.model.impl.Tuple;
import edu.cs.columbia.iesrcsel.sample.generator.InformationExtractionBasedSampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.utils.ScheduleQueryFunction;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class ReScheduleSampleGenerator extends
		InformationExtractionBasedSampleGenerator {

	private int sampleSize;
	private int documentsPerQuery;
	private int maxDocsPerQuery;
	private List<Query> initialQueries;
	private ScheduleQueryFunction scheduleFunction;

	public ReScheduleSampleGenerator(
			InformationExtractionSystem extractionSystem, int sampleSize, int documentsPerQuery, int maxDocsPerQuery, int numberOfQueries, ScheduleQueryFunction scheduleQueryFunction, List<Query> initialQueries, Map<String, String> params) {
		super(extractionSystem,params);
		this.sampleSize = sampleSize;
		assert sampleSize > 0 : "Target sample size should be > 0";
		this.documentsPerQuery = documentsPerQuery;
		this.maxDocsPerQuery = maxDocsPerQuery;
		this.initialQueries = initialQueries.subList(0, numberOfQueries);
		this.scheduleFunction = scheduleQueryFunction;
	}

	@Override
	protected Sample _generateSample(TextCollection textCollection,
			InformationExtractionSystem extractionSystem) {
		
		Sample sample = new Sample(textCollection, this);
		int initialIndex = 0;
		boolean atLeastOneQueryStillHasResults = true;
		boolean sampleIncomplete = true;
		
		do {
			atLeastOneQueryStillHasResults = false;
			for (Query q : initialQueries) {
				// TODO Why do we recalculate the result list each time?
				// Is it because we have too many queries to keep all result lists in memory?
				List<ScoredDocument> result = textCollection.search(q,maxDocsPerQuery);
				final int resultLen = result.size();
				final int iterationLen = initialIndex + documentsPerQuery;
				atLeastOneQueryStillHasResults = atLeastOneQueryStillHasResults ? true : (resultLen > iterationLen);
				final int maxIndex = resultLen > iterationLen ? iterationLen : resultLen;
				for (int j = initialIndex; sampleIncomplete && (j < maxIndex); j++) {
					sample.addDocument(result.get(j));
					final List<Tuple> tuples = extractionSystem.extract(result.get(j));
					scheduleFunction.update(q, tuples.size());
					sampleIncomplete = sample.size() < sampleSize;					
				}
				sampleIncomplete = sampleIncomplete && (sample.size() < sampleSize);
				if (!sampleIncomplete) {
					break;
				}
			}
			sampleIncomplete = sampleIncomplete && (sample.size() < sampleSize);
			if (sampleIncomplete) {
				initialIndex += documentsPerQuery;
				scheduleFunction.reSchedule(initialQueries);
			}
		} while (sampleIncomplete && atLeastOneQueryStillHasResults);
		return sample;
	}

}
