package edu.cs.columbia.iesrcsel.ranking.generator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;
import edu.cs.columbia.iesrcsel.score.estimation.method.impl.RelaxOverlapCalculator;
import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

/**
 * Ranking as per the Relax algorithm described in [1]. It is implemented 
 * generically, and relies on 1) a calculator to estimate the number of relevant
 * documents for a collection, 2) an estimator of the overlap among a pair
 * of collections.
 * 
 * Examples of 1) are {@link edu.cs.columbia.iesrcsel.score.estimation.method.impl.RelaxRelevanceScoreCalculator} 
 * and {@link edu.cs.columbia.iesrcsel.score.estimation.method.impl.ReDDEUUMHPFLScoreCalculator}
 * For 2), see {@link edu.cs.columbia.iesrcsel.score.estimation.method.impl.RelaxOverlapCalculator}
 *  
 * [1] M. Shokouhi and J. Zobel, "Federated text retrieval from uncooperative
 * overlapped collections," in Proc. 30th Int. ACM SIGIR Conf. Research and
 * Development in Information Retrieval (SIGIR 2007), Amsterdam,
 * The Netherlands, 2007, pp. 495-502.
 * 
 * @author Chris Develder
 *
 * @param <S>  "Sample" class
 */
public class RelaxRankingGenerator extends
		SimpleScoreBasedSampleBasedRankingGenerator {

	private RelaxOverlapCalculator<Sample> itsRelaxOverlapCalculator;
	private int itsNumberOfCollectionsToReturn;

	/**
	 * Class to generate rankings using Relax
	 * @param sampleGenerator
	 * @param sampleRelevantSubsetSizeEstimator  ScoreCalculator that returns (an estimate of) 
	 *    the number of relevant items as a score (e.g., ReDDE or the Relax paper's formula)
	 * @param numberOfResourcesToReturn   set this to Integer.MAX_VALUE if you want to rank
	 *    all resources
	 */
	public RelaxRankingGenerator(SampleGenerator sampleGenerator,
			ScoreCalculator<Sample, Sample> sampleRelevantSubsetSizeEstimator,
			CollectionSizeEstimator collectionSizeEstimator,
			ScoreCalculator<PairUnordered<Sample, Sample>, Sample> sampleOverlapCalculator,
			int numberOfResourcesToReturn) {
		super(sampleGenerator, sampleRelevantSubsetSizeEstimator);
		this.itsRelaxOverlapCalculator
			= new RelaxOverlapCalculator<Sample>(sampleRelevantSubsetSizeEstimator,
					collectionSizeEstimator,
					sampleOverlapCalculator);
		this.itsNumberOfCollectionsToReturn = numberOfResourcesToReturn;
	}

	@Override
	protected Map<PairUnordered<Query, Sample>, Double> _generateScores(
			List<Sample> samples,
			Map<PairUnordered<Query, Sample>, Double> scores,
			QueryGenerator queryGenerator) {
		// TODO Replace generateRanking in this method.
		return null;
	}
	
	protected List<Sample> generateRanking(List<Sample> samples,
			Map<Sample, Double> sampleRelevantSubsetSizeEstimates, QueryGenerator queryGenerator) {
		
		Map<Sample,Double> updatedScores = sampleRelevantSubsetSizeEstimates;
		List<Sample> result = new ArrayList<Sample>();
		List<Sample> leftToRank = new ArrayList<Sample>(samples);
		
		while (!leftToRank.isEmpty() && (result.size() < itsNumberOfCollectionsToReturn)) { 
			Collections.sort(leftToRank, new MapBasedComparator<Sample>(updatedScores, true));
			Sample lastAdded = leftToRank.remove(0);
			result.add(lastAdded); //add the top sample
			for (Sample x : leftToRank) {
				updatedScores.put(x,
						updatedScores.get(x)
						- itsRelaxOverlapCalculator.estimateScore(new PairUnordered<Sample, Sample>(lastAdded, x), queryGenerator));
			}
		}
		
		return result;
	}

}
