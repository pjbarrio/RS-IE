package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.BinaryRelevanceJudge;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;

/**
 * An extension of ReDDE-UUM-HR to multiple queries WITH correction for non-disjoint query result lists
 * 
 * @author Chris Develder
 */
public class ReDDEUUMHRScoreCalculatorWithOverlapCorrection extends
		ReDDEUUMHPFLScoreCalculatorWithOverlapCorrection {

	public ReDDEUUMHRScoreCalculatorWithOverlapCorrection(
			QueryGenerator queryGeneratorForScoring,
			CollectionSizeEstimator collectionSizeEstimator,
			Map<Query, Set<ScoredDocument>> relevanceTrainingQueries,
			BinaryRelevanceJudge relevanceJudge,
			Map<Query, Set<Sample>> topScoreTrainingQueries,
			QueryTopScoreProvider queryTopScoreProvider) throws Exception {
		super(queryGeneratorForScoring, Long.MAX_VALUE,
				collectionSizeEstimator, relevanceTrainingQueries,
				relevanceJudge, topScoreTrainingQueries, queryTopScoreProvider);
	}

}
