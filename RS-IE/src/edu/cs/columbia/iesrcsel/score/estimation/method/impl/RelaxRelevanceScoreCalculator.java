package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import java.util.List;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.SampleCollection;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

/**
 * An extension of the Relax relevance score calculator to multiple queries WITHOUT 
 * any consideration of overlaps between query result lists for the same (sample) 
 * collection.
 * As per [1], the initial score (before correction based on overlap estimation, see 
 * {@link edu.cs.columbia.iesrcsel.ranking.generator.impl.RelaxRankingGenerator}) is
 * an estimate of the number of relevant documents for the query.
 * 
 * [1] M. Shokouhi and J. Zobel, "Federated text retrieval from uncooperative
 * overlapped collections," in Proc. 30th Int. ACM SIGIR Conf. Research and
 * Development in Information Retrieval (SIGIR 2007), Amsterdam,
 * The Netherlands, 2007, pp. 495-502.
 *  
 * @author Chris Develder
 */
public class RelaxRelevanceScoreCalculator extends ScoreCalculator<Sample, Sample> {
	
	public final long DEFAULT_LAMBDA = 150; // the value used in [1] 

	protected long itsLambda = 0; 
	protected SampleCollection itsSampleCollection = null;
	protected CollectionSizeEstimator itsCollectionSizeEstimator = null;
	
	/**
	 * 
	 * @param lambda  The number of top-ranked documents considered relevant
	 *   when issuing a query to a centralized index (collecting all samples).
	 *   Default value is {@link #DEFAULT_LAMBDA} = {@value #DEFAULT_LAMBDA}.
	 */
	public RelaxRelevanceScoreCalculator(
			long lambda,
			CollectionSizeEstimator collectionSizeEstimator) {
		itsLambda = lambda;
		assert itsLambda >= 1 : "Lambda should be at least 1";
		itsSampleCollection = new SampleCollection();
		itsCollectionSizeEstimator = collectionSizeEstimator;
	}
	
	@Override
	/**
	 * @return  The estimated number of relevant documents (over all queries
	 *   as provided by the {@link edu.cs.columbia.iesrcsel.query.generator.QueryGenerator}
	 *   in the entire collection that the Sample represents.
	 */
	protected Double _estimateScore(Sample sample, QueryGenerator queryGenerator) {
		final TextCollection collection = sample.getCollection();
		long count = 0L;
		for (Query q : queryGenerator.generateQueries(sample)) {
			for (Document d : itsSampleCollection.search(q, itsLambda)) {
				if (d.getCollection().equals(collection)) {
					count++;
				}
			}
		}
		final double scalingFactor = itsCollectionSizeEstimator.getCollectionSizeEstimate(collection) / (double) sample.size();
		return count * scalingFactor;
	}

	@Override
	public void initialize(List<Sample> samples, QueryGenerator queryGenerator) {
		for (Sample s : samples) {
			itsSampleCollection.addSampleDocuments(s);
		}
	}

	/* (non-Javadoc)
	 * @see edu.cs.columbia.iesrcsel.ranking.estimation.method.ScoreCalculator#estimateQueryScores(java.lang.Object, edu.cs.columbia.iesrcsel.query.generator.QueryGenerator)
	 */
	@Override
	public List<PairUnordered<Query, Double>> estimateQueryScores(
			Sample source, QueryGenerator queryGenerator) {
		// TODO Auto-generated method stub -- NOT IMPLEMENTED YET???
		return null;
	}

	@Override
	protected void _reset() {
		itsSampleCollection.clear();
	}

}
