package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.BinaryRelevanceJudge;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;

/**
 * An extension of ReDDE-UUM-HP-FL to multiple queries WITH correction for non-disjoint query result lists
 * 
 * @author Chris Develder
 */
public class ReDDEUUMHPFLScoreCalculatorWithOverlapCorrection extends ReDDEUUMHPFLScoreCalculator {

	/**
	 * See ReDDEUUMHPFLScoreCalculator for parameters
	 * @param relevanceJudge  for training + judging sample documents (for overlap correction factor)
	 */
	public ReDDEUUMHPFLScoreCalculatorWithOverlapCorrection(
			QueryGenerator queryGeneratorForScoring,
			long numberOfDocumentsToRetrievePerQuery,
			CollectionSizeEstimator collectionSizeEstimator,
			Map<Query, Set<ScoredDocument>> relevanceTrainingQueries,
			BinaryRelevanceJudge relevanceJudge,
			Map<Query, Set<Sample>> topScoreTrainingQueries,
			QueryTopScoreProvider queryTopScoreProvider) throws Exception {
		super(numberOfDocumentsToRetrievePerQuery,
				collectionSizeEstimator, relevanceTrainingQueries, relevanceJudge,
				topScoreTrainingQueries, queryTopScoreProvider);
	}

	/**
	 * @return   The estimated number of *relevant* documents in the union of all top-K lists
	 *   of each query q, issued against the *complete* document collection.
	 *   (Where K == itsNumberOfDocumentsPerQuery)
	 */
	@Override
	protected Double _estimateScore(Sample sample, QueryGenerator queryGenerator) {
		// The original score, without overlap correction, is the sum of the estimated
		// number of relevant documents for each query, summed over all queries
		final double score = super._estimateScore(sample, queryGenerator);
		
		// We now calculate the correction factor as:
		// 
		//                        # unique relevant documents in sample retrieved by any query 
		//    correctionFactor = --------------------------------------------------------------
		//                         sum of relevant document count in sample over all queries
		//
		// Thus, if we find X as the original score (= estimate of sum of relevant document count over 
		// all queries, from complete collection), then the number of unique documents across all
		// queries will indeed be estimated by X * correctionFactor.
		final double collectionSize = itsCollectionSizeEstimator.getCollectionSizeEstimate(sample.getCollection());
		final double scalingFactor = collectionSize/((double) sample.size());
		final long sampleDocumentsPerQuery = Math.round(itsNumberOfDocumentsPerQuery/scalingFactor);
		assert (itsNumberOfDocumentsPerQuery != Long.MAX_VALUE) || (sampleDocumentsPerQuery == Long.MAX_VALUE)
			: "If number of documents is unrestricted, then the number of sample documents should be as well";
		
		Set<Document> unionSet = new HashSet<Document>();
		double correctionFactor = 0.0d;
		for (Query q : queryGenerator.generateQueries(sample)) {
			List<ScoredDocument> qresult = itsSampleCollection.search(q, sample, sampleDocumentsPerQuery);
			assert qresult.size() <= sampleDocumentsPerQuery
					: "Result list should not contain more than sampleDocumentsPerQuery documents";
			double numberOfRelevantInTopKlist = 0;
			for (ScoredDocument d : qresult) {
				if (itsRelevanceJudge.isRelevant(d, q)) {
					numberOfRelevantInTopKlist++;
					unionSet.add(d);
				}
			}
			correctionFactor += numberOfRelevantInTopKlist; // note that it may be < itsNumberOfDocumentsPerQuery(!)
		}
		correctionFactor /= (double) unionSet.size();
		return score * correctionFactor;
	}
}
