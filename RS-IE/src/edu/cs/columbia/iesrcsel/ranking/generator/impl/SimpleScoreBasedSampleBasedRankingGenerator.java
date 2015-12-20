package edu.cs.columbia.iesrcsel.ranking.generator.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;
import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

public class SimpleScoreBasedSampleBasedRankingGenerator extends ScoreBasedSampleBasedRankingGenerator<Sample>{

	public SimpleScoreBasedSampleBasedRankingGenerator(
			SampleGenerator sampleGenerator,
			ScoreCalculator<Sample, Sample> scoreCalculator) {
		super(sampleGenerator, scoreCalculator);
	}

	@Override
	protected Map<PairUnordered<Query, Sample>, Double> _generateScores(
			List<Sample> samples,
			Map<PairUnordered<Query, Sample>, Double> scores,
			QueryGenerator queryGenerator) {
		return scores;
	}

	@Override
	protected Iterable<Sample> getScoreSources(List<Sample> samples) {
		return samples;
	}



	

}
