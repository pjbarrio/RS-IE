package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import java.util.HashSet;
import java.util.Set;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;

/**
 * An extension of the Relax relevance score calculator to multiple queries WITH 
 * any consideration of overlaps between query result lists for the same (sample) 
 * collection. See {@link RelaxRelevanceScoreCalculator} for the basic Relax 
 * relevance scoring. The current class only adds a correction for overlap
 * among document result lists of different queries for the same sample.
 * The relevance model is the same as in [1], i.e., the top-lambda documents
 * gathered as a result of issuing the query against a centralized index 
 * collecting all samples are considered relevant.
 * 
 * [1] M. Shokouhi and J. Zobel, "Federated text retrieval from uncooperative
 * overlapped collections," in Proc. 30th Int. ACM SIGIR Conf. Research and
 * Development in Information Retrieval (SIGIR 2007), Amsterdam,
 * The Netherlands, 2007, pp. 495-502.
 * 
 * @author Chris Develder
 */
public class RelaxRelevanceScoreCalculatorWithOverlapCorrection extends RelaxRelevanceScoreCalculator {

	public RelaxRelevanceScoreCalculatorWithOverlapCorrection(
			long lambda,
			CollectionSizeEstimator collectionSizeEstimator) {
		super(lambda, collectionSizeEstimator);
	}
	
	@Override
	/**
	 * @return  The estimated number of relevant documents (over all queries
	 *   as provided by the {@link edu.cs.columbia.iesrcsel.query.generator.QueryGenerator}
	 *   in the entire collection that the Sample represents.
	 */
	protected Double _estimateScore(Sample sample, QueryGenerator queryGenerator) {
		final TextCollection collection = sample.getCollection();
		// TODO could be more efficient to just keep track of the document IDs
		// rather than the complete Document object
		Set<Document> unionSet = new HashSet<Document>();
		for (Query q : queryGenerator.generateQueries(sample)) {
			for (Document d : itsSampleCollection.search(q, itsLambda)) {
				if (d.getCollection().equals(collection)) {
					unionSet.add(d);
				}
			}
		}
		final double scalingFactor = itsCollectionSizeEstimator.getCollectionSizeEstimate(collection) / (double) sample.size();
		return unionSet.size() * scalingFactor;
	}

}
