package edu.cs.columbia.iesrcsel.ranking.generator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

public abstract class PairWiseScoreBasedSampleBasedRankingGenerator extends
		ScoreBasedSampleBasedRankingGenerator<PairUnordered<Sample, Sample>> {

	public PairWiseScoreBasedSampleBasedRankingGenerator(
			SampleGenerator sampleGenerator,
			ScoreCalculator<PairUnordered<Sample, Sample>, Sample> scoreCalculator) {
		super(sampleGenerator, scoreCalculator);
	}

	@Override
	protected Iterable<PairUnordered<Sample, Sample>> getScoreSources(
			List<Sample> samples) {
		List<PairUnordered<Sample,Sample>> pairs = new ArrayList<PairUnordered<Sample,Sample>>();
		
		for (int i = 0; i < samples.size()-1; i++) {
			for (int j = i+1; j < samples.size(); j++) {

				PairUnordered<Sample,Sample> p = new PairUnordered<Sample, Sample>(samples.get(i), samples.get(j));
				
				pairs.add(p);
					
			}
		}

		return pairs;
		
	}

}
