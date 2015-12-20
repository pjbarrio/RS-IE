package edu.cs.columbia.iesrcsel.ranking.generator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.ranking.generator.SampleBasedRankingGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

/**
 * @param <T>  Class of items that get scored
 * @param <S>  "Sample" class, i.e., a representation of the collection to derive the ranking from
 */
public abstract class ScoreBasedSampleBasedRankingGenerator<T> extends
		SampleBasedRankingGenerator {
	private ScoreCalculator<T,Sample> itsScoreCalculator;

	public ScoreBasedSampleBasedRankingGenerator(SampleGenerator sampleGenerator, ScoreCalculator<T,Sample> scoreCalculator){
		super(sampleGenerator);
		this.itsScoreCalculator = scoreCalculator;
	}
	
	@Override
	protected Map<PairUnordered<Query, Sample>, Double> _getSampleQueriesScores(List<Sample> samples,
			InformationExtractionSystem extractionSystem, QueryGenerator queryGenerator) {
		
		Map<PairUnordered<Query, T>,Double> scores = new HashMap<PairUnordered<Query,T>, Double>();
		
		for (T source : getScoreSources(samples)) {
			List<PairUnordered<Query, Double>> scoresSource = itsScoreCalculator.estimateQueryScores(source, queryGenerator);
			for (int i = 0; i < scoresSource.size(); i++) {
				scores.put(new PairUnordered<Query, T>(scoresSource.get(i).getFirst(), source), scoresSource.get(i).getSecond());
			}
		}
		
		return _generateScores(samples, scores, queryGenerator);
		
	}

	protected abstract Map<PairUnordered<Query, Sample>, Double> _generateScores(List<Sample> samples, Map<PairUnordered<Query, T>,Double> scores, QueryGenerator queryGenerator);

	/**
	 * Should figure out all T objects we need to eventually be able to generate the Sample ranking
	 * @param samples
	 * @return collection of all T objects for which scores should be calculated 
	 */
	protected abstract Iterable<T> getScoreSources(List<Sample> samples);

	@Override
	protected void initialize(List<Sample> samples, QueryGenerator queryGenerator) {
		itsScoreCalculator.initialize(samples, queryGenerator);
	}

}
