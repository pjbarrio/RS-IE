package edu.cs.columbia.iesrcsel.ranking.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

public abstract class SampleBasedRankingGenerator extends RankingGenerator {

	private SampleGenerator sampleGenerator;

	public SampleBasedRankingGenerator(SampleGenerator sampleGenerator){
		this.sampleGenerator = sampleGenerator;
	}
	
	@Override
	public Map<PairUnordered<Query, TextCollection>, Double> _estimateQueryCollectionScores(
			Set<TextCollection> collections,
			InformationExtractionSystem extractionSystem, QueryGenerator queryGenerator) {
		
		List<Sample> samples = new ArrayList<Sample>(collections.size());
		for (TextCollection collection: collections) {
			samples.add(sampleGenerator.generateSample(collection));
		}

		initialize(samples,queryGenerator);
		
		Map<PairUnordered<Query, Sample>, Double> scoredSamplesQueries = _getSampleQueriesScores(samples, extractionSystem, queryGenerator);

		Map<PairUnordered<Query, TextCollection>,Double> scoredCollectionQueries = new HashMap<PairUnordered<Query, TextCollection>,Double>(scoredSamplesQueries.size());
		
		for (Entry<PairUnordered<Query, Sample>, Double> scores : scoredSamplesQueries.entrySet()) {
			
			scoredCollectionQueries.put(new PairUnordered<Query, TextCollection>(scores.getKey().getFirst(), scores.getKey().getSecond().getCollection()), scores.getValue());
			
		}

		return scoredCollectionQueries;
		
	}

	/**
	 * Gets called by public List<TextCollection> getRankedList(
	 * 			Set<TextCollection> collections,
	 * 			InformationExtractionSystem extractionSystem) 
	 * @param samples
	 */
	protected abstract void initialize(List<Sample> samples, QueryGenerator queryGenerator);

	protected abstract Map<PairUnordered<Query, Sample>, Double> _getSampleQueriesScores(List<Sample> samples,
			InformationExtractionSystem extractionSystem, QueryGenerator queryGenerator);

	

}
