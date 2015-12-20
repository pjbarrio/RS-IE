package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.SampleCollection;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

/**
* An extension of the original ReDDE relevance score calculator to multiple queries WITHOUT 
* any consideration of overlaps between query result lists for the same (sample) 
* collection.
* As per [1], the score for a document collection C (represented by its sample) for a given
* query is the (estimated) percentage of all relevant documents (across the union of all
* collections) that can be found in that particular collection C.
* However, since we will be comparing across different queries (w/ same or other collections),
* we keep the unnormalized score, being the absolute number of relevant documents for
* the given query within collection C. 
* Note that this score is <i>recall</i>-oriented, since all relevant documents are counted 
* for a given query q, not just the top-K.
* 
* [1] L. Si and J. Callan, "Relevant document distribution estimation method for resource
* selection," in Proc. 26th Int. ACM SIGIR Conf. Research and Development in Information
* Retrieval (SIGIR 2003), Toronto, Canada, 2003, pp. 298-305.
*  
* @author Chris Develder
*/
public class ReDDEOriginalScoreCalculator extends ScoreCalculator<Sample, Sample> {

	public final static double DEFAULT_RELEVANCE_RATIO = 0.003; // the value used in [1] 
	
	protected double itsRelevanceRatio = 0.0d;
	protected CollectionSizeEstimator itsCollectionSizeEstimator = null;
	protected SampleCollection itsSampleCollection;
	protected long itsTotalDocumentCount = 0L;
	protected long itsTotalSampleDocumentCount = 0L;
	
	protected Map<TextCollection, Double> itsCollectionScalingFactors = new HashMap<TextCollection, Double>();

	protected double itsMinScalingFactor;
	
	/**
	 * 
	 * @param queryGeneratorForScoring
	 * @param collectionSizeEstimator
	 * @param relevanceRatio  The fraction of all documents that is considered relevant to
	 *   a given query: if relevanceRatio == R, and the sum of all (estimated) collection
	 *   sizes (as given by collectionSizeEstimator) is C, then the top R*C documents 
	 *   returned for a query will be considered relevant.
	 *   A default value is predefined by {@link #DEFAULT_RELEVANCE_RATIO} = {@value #DEFAULT_RELEVANCE_RATIO}.
	 * @param strategy  The strategy to combine query scores:
	 *   <ul>
	 *   <li> UNIFORM simply takes the average of the scores as traditional ReDDE would calculate for each
	 *          of the queries.
	 *   <li> PROPORTIONAL_TO_QUERY_RESULTSET_SIZE will return the fraction of documents relevant to any 
	 *          query (however, without overlap correction: a document relevant to multiple queries will
	 *          be counted multiple times!)
	 *   </ul>
	 */
	public ReDDEOriginalScoreCalculator(Map<String,String> params, double relevanceRatio,
			CollectionSizeEstimator collectionSizeEstimator) {
		super(params);
		itsRelevanceRatio = relevanceRatio;
		itsCollectionSizeEstimator = collectionSizeEstimator;
		//assert itsRelevanceRatio > 0.0d : "Relevance ratio should be >0 (e.g., use DEFAULT_RELEVANCE_RATIO)";
		if (itsRelevanceRatio <= 0.0d) {
			throw new IllegalArgumentException("Relevance ratio should be >0 (you used "
					+ relevanceRatio + ")");
		}
	}

	@Override
	/**
	 * @return The estimated *absolute* number of relevant documents that can be found in the 
	 *   sample's corresponding collection, summed over all its queries.
	 *   Note that documents that occur in the relevant result list for multiple queries will
	 *   be counted multiple times.
	 *   Please use {@link ReDDEOriginalScoreCalculatorWithOverlapCorrection} if you want to 
	 *   correctly (i.e., uniquely) count the documents across multiple queries.
	 */
	protected Double _estimateScore(Sample sample, QueryGenerator queryGenerator) {
		final List<PairUnordered<Query, Double>> queryScores = estimateQueryScores(sample, queryGenerator);
		Double sum = 0.0d;
		for (PairUnordered<Query, Double> queryScorePair : queryScores) {
			sum += queryScorePair.getSecond();
		}
		return sum; 
	}
	
	@Override
	/**
	 * Note that we DO NOT test whether all samples are in fact for different collections!
	 * @param samples
	 * @param queryGenerator   Not used!
	 */
	public void initialize(List<Sample> samples, QueryGenerator queryGenerator) {
		itsCollectionScalingFactors.clear();
		if (itsSampleCollection != null)
			itsSampleCollection.clear();
		itsSampleCollection = new SampleCollection();
		itsTotalDocumentCount = 0L;
		itsTotalSampleDocumentCount = 0L;
		itsMinScalingFactor = Double.POSITIVE_INFINITY;
		try {
			for (Sample s : samples) {
				itsSampleCollection.addSampleDocuments(s);
				final TextCollection c = s.getCollection();
				final long collectionSize =  itsCollectionSizeEstimator.getCollectionSizeEstimate(c);
				final long sampleSize = s.size();
				final double scalingFactor = collectionSize/(double) sampleSize;
				itsTotalDocumentCount += collectionSize;
				itsTotalSampleDocumentCount += sampleSize;
				itsCollectionScalingFactors.put(c, scalingFactor);
				if (scalingFactor < itsMinScalingFactor) {
					itsMinScalingFactor = scalingFactor;
				}
			}
		} catch (Exception e) {
			itsSampleCollection.clear(); // since we don't know which sample failed to add, we give up altogether
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * We calculate the scores for every query generated by queryGenerator for the
	 * given sample. 
	 * 
	 * Note: For sample S, and query q, the original ReDDE score is defined as
	 *   the estimated percentage of all relevant documents (across all collections)
	 *   for that particular query q that can be found in the collection
	 *   sample.getCollection().
	 *   Note that this is NOT comparable across queries. Thus, we keep the
	 *   (estimated) absolute number of relevant documents as score.
	 *   Then scores are comparable cross-collection, and cross-query, and
	 *   we can, e.g., sum over all the sample/collection's queries to obtain
	 *   a multi-query score for the collection.
	 *
	 * @return The score of a sample for a given query q will be the estimated 
	 *   *absolute* number of relevant documents that can be found in the sample's
	 *   corresponding collection 
	 */
	public List<PairUnordered<Query, Double>> estimateQueryScores(
			Sample sample, QueryGenerator queryGenerator) {
		if (!itsSampleCollection.contains(sample)) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() 
					+ " does not contain the given sample (from collection "
					+ sample.getCollection().getId() + "). Make sure to call "
					+ this.getClass().getSimpleName() + ".initialize(...) first.");
		}
		
		assert itsRelevanceRatio > 0.0d : "Relevance ratio should be >0 (e.g., use DEFAULT_RELEVANCE_RATIO)";

		final TextCollection sampleCollection = sample.getCollection();
		final long maxNumberOfRelevantDocuments = Math.round(itsTotalDocumentCount * itsRelevanceRatio);
		final long totalSampleDocumentsToRetrieve = (long) Math.ceil(itsTotalDocumentCount * itsRelevanceRatio / itsMinScalingFactor);
		
		assert totalSampleDocumentsToRetrieve > 0;
		assert maxNumberOfRelevantDocuments > 0;
		
		
		final List<Query> queryList = queryGenerator.generateQueries(sample);
		List<PairUnordered<Query, Double>> resultScoreList = new ArrayList<PairUnordered<Query,Double>>(queryList.size());
		for (Query q : queryList) {
			final List<ScoredDocument> centralQueryResult = itsSampleCollection.search(q, totalSampleDocumentsToRetrieve);
			double queryScore = 0.0d; // after the next loop, this will be the number of relevant documents
                                      // (with rank < maxRelevantRank) in the sample's collection
			if (!centralQueryResult.isEmpty()) {
				long numberOfRelevantDocumentsFound = 0L;
				// double queryScoreNormalization = 0.0d; // this will be the number of relevant documents across *all* collections
				for (Document d : centralQueryResult) {
					final TextCollection docCollection = d.getCollection();
					final double scalingFactor = itsCollectionScalingFactors.get(docCollection);
					if (numberOfRelevantDocumentsFound < maxNumberOfRelevantDocuments) {
						final double estimatedRelevantDocumentsInDocCollection
							= (numberOfRelevantDocumentsFound + scalingFactor < maxNumberOfRelevantDocuments)
							? scalingFactor
							: (maxNumberOfRelevantDocuments - numberOfRelevantDocumentsFound);
						// queryScoreNormalization += estimatedRelevantDocumentsInCollection;
						if (docCollection.equals(sampleCollection)) {
							queryScore += estimatedRelevantDocumentsInDocCollection;
						}
						numberOfRelevantDocumentsFound += estimatedRelevantDocumentsInDocCollection;
					} else {
						// all the next documents will have higher rankEstimate and thus
						// will be considered irrelevant (higher rank than maxRelevantRank)
						break;
					}
				}
				// Original ReDDE as per [1] would be:
				// if (queryScoreNormalization > 0.0d) {
				//	queryScore /= queryScoreNormalization;
				//  // now this is the percentage of q-relevant documents in sample.getCollection()
				//}
				// Since we will compare scores for (possibly) different queries, it does not make sense
				// to normalize with a factor that will thus be different (for other queries, w/ same or
				// other collections --> we just keep the absolute number of relevant documents as score.
			}
			resultScoreList.add(new PairUnordered<Query, Double>(q, queryScore));
		}
		return resultScoreList;
	}

	@Override
	protected void _reset() {
		itsCollectionScalingFactors.clear();
		itsSampleCollection.clear();		
	}
	
}
