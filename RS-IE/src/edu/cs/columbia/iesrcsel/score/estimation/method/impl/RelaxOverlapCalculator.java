package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import java.util.List;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

public class RelaxOverlapCalculator<S extends Sample> extends ScoreCalculator<PairUnordered<S,S>,S> {

	private ScoreCalculator<S, S> itsSampleRelevanceSizeEstimator;
	private CollectionSizeEstimator itsCollectionSizeEstimator;
	private ScoreCalculator<PairUnordered<S, S>, S> itsSampleOverlapCalculator;
	
	/**
	 * This class will calculate common relevant "items"; where items can be
	 * e.g., documents or tuples (that can be extracted from the documents)
	 * @param sampleRelevantSubsetSizeEstimator  ScoreCalculator that returns a score which is an
	 *    estimate of the number of relevant items in the respective *entire* text collection;
	 *    this can be ReDDE or the Relax paper's estimate.
	 * @param collectionSizeEstimator  To estimate the total number of items in the entire collection
	 * @param sampleOverlapCalculator  Should return the number of common items among a pair of samples
	 */
	public RelaxOverlapCalculator(ScoreCalculator<S, S> sampleRelevantSubsetSizeEstimator,
			CollectionSizeEstimator collectionSizeEstimator,
			ScoreCalculator<PairUnordered<S, S>, S> sampleOverlapCalculator) {
		itsSampleRelevanceSizeEstimator = sampleRelevantSubsetSizeEstimator;
		itsCollectionSizeEstimator = collectionSizeEstimator;
		itsSampleOverlapCalculator = sampleOverlapCalculator;
	}

	@Override
	// FIXME: does this really make sense? Queries for both samples will not be the same?
	/**
	 * @param	samplePair
	 * @return	The number of overlapping *relevant* items between the pair of collections
	 *     corresponding to the samples
	 */
	protected Double _estimateScore(PairUnordered<S, S> samplePair, QueryGenerator queryGenerator) {
		final S sample1 = samplePair.getFirst();
		final S sample2 = samplePair.getSecond();
		
		final double s1 = sample1.size();
		final double s2 = sample2.size();
		final double c1 = itsCollectionSizeEstimator.getCollectionSizeEstimate(sample1.getCollection());
		final double c2 = itsCollectionSizeEstimator.getCollectionSizeEstimate(sample2.getCollection());
		final double r1 = itsSampleRelevanceSizeEstimator.estimateScore(sample1,queryGenerator);
		final double r2 = itsSampleRelevanceSizeEstimator.estimateScore(sample2,queryGenerator);
		final double s1IntersectS2 = itsSampleOverlapCalculator.estimateScore(samplePair,queryGenerator);
		// FIXME implement overlapcalculator class, i.e., just find number of documents in intersection of two samples
		
		final double score = ((r1 + r2)/(c1+c2)) * c1/s1 * c2/s2 * s1IntersectS2;
		return score;
	}

	@Override
	public void initialize(List<S> samples, QueryGenerator queryGenerator) {
		// nothing todo; we assume ReDDE is initialized ready to score samples
	}

	@Override
	// FIXME implement?
	public List<PairUnordered<Query, Double>> estimateQueryScores(
			PairUnordered<S, S> samplePair, QueryGenerator queryGenerator) {
		// throw new IllegalAccessException("Unimplemented method");
		assert false : "Unimplemented method!";
		return null;
	}

	@Override
	protected void _reset() {
		itsSampleOverlapCalculator.reset();
		itsSampleRelevanceSizeEstimator.reset();		
	}	
}
