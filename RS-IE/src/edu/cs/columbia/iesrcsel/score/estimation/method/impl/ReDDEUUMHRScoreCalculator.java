package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.BinaryRelevanceJudge;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;

/**
 * ReDDE-UUM-HR implementation, as per [1], i.e., the Unified Utility Maximization (UUM) variant of
 * ReDDE, optimizing for high recall (HR)
 * An extension of ReDDE-UUM-HR to multiple queries.
 * ReDDE-UUM-HR is the Unified Utility Maximization (UUM) variant of ReDDE as per [1],
 * optimizing for high recall (HR).
 * The score for a Sample is actually the estimated number of relevant documents among the
 * (entire!) text collection for each of the queries.
 * Important note: We do NOT correct for possible document overlap among the query results.
 * 
 * [1]  M. Shokouhi and J. Zobel, "Federated text retrieval from uncooperative overlapped collections",
 * in Proc. 30th Int. ACM SIGIR Conf. Research and Development in Information Retrieval (SIGIR 2007),
 * Amsterdam, The Netherlands, ACM, 23-27 Jul. 2007, pp. 495-502.
 * 
 * @author cdvelder
 */
public class ReDDEUUMHRScoreCalculator extends ReDDEUUMHPFLScoreCalculator {

	public ReDDEUUMHRScoreCalculator(
			CollectionSizeEstimator collectionSizeEstimator,
			Map<Query, Set<ScoredDocument>> relevanceTrainingQueries,
			BinaryRelevanceJudge relevanceJudge,
			Map<Query, Set<Sample>>  topScoreTrainingQueries,
			QueryTopScoreProvider queryTopScoreProvider
			) throws Exception {
		super(Long.MAX_VALUE,
				collectionSizeEstimator, relevanceTrainingQueries, relevanceJudge, topScoreTrainingQueries, queryTopScoreProvider);
	}
}
