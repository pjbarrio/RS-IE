package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;

/**
 * An extension of the original ReDDE relevance score calculator to multiple
 * queries WITH consideration of overlaps between query result lists for the
 * same (sample) collection. See {@link ReDDEOriginalScoreCalculator} for the
 * basic ReDDE relevance scoring.
 * 
 * @author Chris Develder
 * 
 */
public class ReDDEOriginalScoreCalculatorWithOverlapCorrection extends
		ReDDEOriginalScoreCalculator {

	public ReDDEOriginalScoreCalculatorWithOverlapCorrection(
			Map<String,String> params,
			double relevanceRatio,
			CollectionSizeEstimator collectionSizeEstimator) {
		// QueryCombinationStrategy does NOT matter, since we override _estimateScore(...)
		super(params,relevanceRatio, collectionSizeEstimator);
	}
	
	@Override
	/**
	 * @return The estimated absolute number of relevant documents that can be found
	 *         in the collection sample.getCollection().
	 */
	protected Double _estimateScore(Sample sample, QueryGenerator queryGenerator) {
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
		final Set<Document> totalRelevantDocSet = new HashSet<Document>();
		final Set<Document> sampleRelevantDocSet = new HashSet<Document>();
		for (Query q : queryList) {
			final List<ScoredDocument> centralQueryResult = itsSampleCollection.search(q, totalSampleDocumentsToRetrieve);
			long numberOfRelevantDocumentsFound = 0L;
			for (Document d : centralQueryResult) {
				final TextCollection docCollection = d.getCollection();
				final double scalingFactor = itsCollectionScalingFactors.get(docCollection); 
				if (numberOfRelevantDocumentsFound < maxNumberOfRelevantDocuments) {
					final double estimatedRelevantDocumentsInDocCollection
						= (numberOfRelevantDocumentsFound + scalingFactor < maxNumberOfRelevantDocuments)
						? scalingFactor
						: (maxNumberOfRelevantDocuments - numberOfRelevantDocumentsFound);
					totalRelevantDocSet.add(d);
					if (docCollection.equals(sampleCollection)) {
						sampleRelevantDocSet.add(d);
					}
					numberOfRelevantDocumentsFound += estimatedRelevantDocumentsInDocCollection;
				} else {
					// all the next documents will have higher rankEstimate and thus
					// will be considered irrelevant (higher rank than maxRelevantRank)
					break;
				}
			}
		}
//		double scoreNormalization = 0.0;
//		for (Document d : totalRelevantDocSet) {
//			 // FIXME what if a document is contained in multiple collections?
//			scoreNormalization += itsCollectionScalingFactors.get(d.getCollection());
//		}
		final double score = sampleRelevantDocSet.size() * itsCollectionScalingFactors.get(sampleCollection);
//		return score/scoreNormalization;
		return score;
	}
}
